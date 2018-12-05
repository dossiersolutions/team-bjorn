package no.dossier.thatbuttonserver;

import no.dossier.thatbuttonserver.types.ButtonMessage;
import no.dossier.thatbuttonserver.types.Config;
import no.dossier.thatbuttonserver.types.ConfigVersion;
import no.dossier.thatbuttonserver.util.CurrDirectoryWatcher;
import no.dossier.thatbuttonserver.util.FileWatchSpec;
import no.dossier.thatbuttonserver.util.Result;
import no.dossier.thatbuttonserver.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.StandardWatchEventKinds;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static no.dossier.thatbuttonserver.util.List.list;
import static no.dossier.thatbuttonserver.util.ThrowableToStringConverter.convertThrowable;
import static no.dossier.thatbuttonserver.util.Unit.unit;

public final class ThatButtonServer {

    private static final Logger LOGGER = LogManager.getLogger(ThatButtonServer.class);

    private static final String RUNNING_FILE_NAME = "thatbuttonserver.running";

    public static void run(ShellRunner shellRunner) throws IOException {
        LOGGER.info("Starting ThatButtonServer");

        ExecutorService executorService = Executors.newCachedThreadPool();

        Result<String, Config> configResult = ConfigReader.readConfig();
        configResult.branch(
                config -> {
                    AtomicReference<ConfigVersion> configVersionRef = new AtomicReference<>(new ConfigVersion(config));
                    BlockingQueue<Unit> stopSignal = new LinkedBlockingQueue<>();

                    BlockingQueue<ButtonMessage> messageQueue = new LinkedBlockingQueue<>();

                    AtomicBoolean stopFlag = new AtomicBoolean(false);

                    executorService.submit(new CurrDirectoryWatcher(
                            list(
                                    new FileWatchSpec(
                                            ConfigReader.CONFIG_FILE_NAME,
                                            kind -> {
                                                if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                                                    handleConfigFileEvent(configVersionRef);
                                                }
                                            }),
                                    new FileWatchSpec(
                                            RUNNING_FILE_NAME,
                                            kind -> {
                                                if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                                                    stopSignal.offer(unit());

                                                    LOGGER.info("ThatButtonServer completed");
                                                }
                                            })),
                            stopFlag));

                    executorService.submit(new SocketListener(messageQueue, configVersionRef, stopFlag));
                    executorService.submit(new MessageRouter(shellRunner, messageQueue, executorService, configVersionRef, stopFlag));

                    try {
                        stopSignal.take();
                        stopFlag.set(true);
                        executorService.shutdown();
                        executorService.awaitTermination(10L, TimeUnit.SECONDS);
                    } catch (InterruptedException ex) {
                        // Ignore, since we're quitting anyway
                    }
                },
                LOGGER::error);
    }

    private static void handleConfigFileEvent(AtomicReference<ConfigVersion> configVersionRef) {
        try {
            Result<String, Config> newConfigResult = ConfigReader.readConfig();
            newConfigResult.branch(
                    newConfig -> {
                        configVersionRef.updateAndGet(currConfigVersion ->
                                newConfig.equals(currConfigVersion.getConfig()) ?
                                        currConfigVersion :
                                        currConfigVersion.setNewConfig(newConfig));
                        LOGGER.info("Reloaded configuration");
                    },
                    decodeError -> LOGGER.error(
                            "{}, still using last valid configuration",
                            decodeError)
            );
        } catch (Throwable ex) {
            LOGGER.error(
                    "Failed to reload configuration: {}",
                    convertThrowable(ex));
        }
    }

    private ThatButtonServer() {
    }

}
