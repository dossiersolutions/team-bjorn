package no.dossier.thatbuttonserver;

import no.dossier.thatbuttonserver.types.ButtonMessage;
import no.dossier.thatbuttonserver.types.Config;
import no.dossier.thatbuttonserver.types.ConfigVersion;
import no.dossier.thatbuttonserver.util.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public final class ThatButtonServer {

    private static final Logger LOGGER = LogManager.getLogger(ThatButtonServer.class);

    public static void run(ShellRunner shellRunner) throws IOException {
        LOGGER.info("Starting ThatButtonServer");

        ExecutorService executorService = Executors.newCachedThreadPool();
        AtomicBoolean stopFlag = new AtomicBoolean(false);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            /*
             * Notice that this part will not be executed if the ThatButtonServer is stopped with
             * a SIGKILL signal or Process.destroy() (which is what happens if you stop it from IDEA)
             */
            try {
                stopFlag.set(true);

                executorService.shutdown();
                executorService.awaitTermination(10L, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                // Ignore, since we're exiting anyway
            }

            LOGGER.info("ThatButtonServer completed");
        }));

        Result<String, Config> configResult = ConfigReader.readConfig();
        configResult.branch(
                config -> {
                    AtomicReference<ConfigVersion> configVersionRef = new AtomicReference<>(new ConfigVersion(config));

                    BlockingQueue<ButtonMessage> messageQueue = new LinkedBlockingQueue<>();

                    executorService.submit(new ConfigFileWatcher(configVersionRef, stopFlag));
                    executorService.submit(new SocketListener(messageQueue, configVersionRef, stopFlag));
                    executorService.submit(new MessageRouter(shellRunner, messageQueue, executorService, configVersionRef, stopFlag));

                    // The program doesn't stop running here! The above threads are still running.
                },
                LOGGER::error);
    }

    private ThatButtonServer() {
    }

}
