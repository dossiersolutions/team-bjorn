package no.dossier.buttonserver;

import no.dossier.buttonserver.types.Config;
import no.dossier.buttonserver.types.ConfigVersion;
import no.dossier.buttonserver.util.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static no.dossier.buttonserver.ConfigReader.readConfig;
import static no.dossier.buttonserver.util.ExceptionToStringConverter.convertException;

public final class ConfigFileWatcher implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(ConfigFileWatcher.class);

    private static final long QUIT_POLL_INTERVAL_MILLISECONDS = 1000L;

    private final AtomicReference<ConfigVersion> configVersionRef;
    private final AtomicBoolean stopFlag;

    public ConfigFileWatcher(AtomicReference<ConfigVersion> configVersionRef, AtomicBoolean stopFlag) {
        this.configVersionRef = configVersionRef;
        this.stopFlag = stopFlag;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Starting ConfigFileWatcher");

            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path directoryPath = new File(".").getCanonicalFile().toPath();
            LOGGER.info("Watching modified files in {}", directoryPath);
            directoryPath.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY);
            Path filePath = directoryPath.resolve(ConfigReader.CONFIG_FILE_NAME);
            while (!stopFlag.get()) {
                WatchKey key = watcher.poll(QUIT_POLL_INTERVAL_MILLISECONDS, TimeUnit.MILLISECONDS);
                if (key != null) {
                    for (WatchEvent<?> watchEvent : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = watchEvent.kind();
                        if (kind.equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                            //noinspection unchecked
                            WatchEvent<Path> pathEvent = (WatchEvent<Path>) watchEvent;
                            Path eventFilePath = directoryPath.resolve(pathEvent.context());
                            if (eventFilePath.equals(filePath)) {
                                configVersionRef.updateAndGet(currConfigVersion -> {
                                    ConfigVersion newConfigVersion;
                                    try {
                                        Result<String, Config> newConfigResult = readConfig();
                                        newConfigVersion = newConfigResult.unwrap(
                                                newConfig -> {
                                                    LOGGER.info("Reloaded configuration");
                                                    return currConfigVersion.setNewConfig(newConfig);
                                                },
                                                decodeFailure -> {
                                                    LOGGER.error("{}, still using last valid configuration", decodeFailure);
                                                    return currConfigVersion;
                                                }
                                        );
                                    } catch (IOException ex) {
                                        LOGGER.error("Failed to reload configuration: {}", convertException(ex));
                                        newConfigVersion = currConfigVersion;
                                    }
                                    return newConfigVersion;
                                });
                            }
                        }
                    }
                    key.reset();
                }
            }

            LOGGER.info("ConfigFileWatcher completed");
        } catch (IOException | InterruptedException ex) {
            LOGGER.fatal("ConfigFileWatcher crashed with exception {}", convertException(ex));
        }
    }

}
