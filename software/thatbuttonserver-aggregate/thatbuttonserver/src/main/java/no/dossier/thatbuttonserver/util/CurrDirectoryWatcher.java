package no.dossier.thatbuttonserver.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static no.dossier.thatbuttonserver.util.ThrowableToStringConverter.convertThrowable;

public final class CurrDirectoryWatcher implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(CurrDirectoryWatcher.class);

    private static final long QUIT_POLL_INTERVAL_MILLISECONDS = 10000L;

    private final List<FileWatchSpec> watchSpecs;
    private final AtomicBoolean stopFlag;

    public CurrDirectoryWatcher(List<FileWatchSpec> watchSpecs, AtomicBoolean stopFlag) {
        this.watchSpecs = watchSpecs;
        this.stopFlag = stopFlag;
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Starting CurrDirectoryWatcher for files: {}", watchSpecs.map(FileWatchSpec::getFileName));

            WatchService watcher = FileSystems.getDefault().newWatchService();
            Path directoryPath = new File(".").getCanonicalFile().toPath();
            LOGGER.info("Watching modified files in {}", directoryPath);
            directoryPath.register(watcher, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);

            List<Tuple2<FileWatchSpec, Path>> watchSpecPaths = watchSpecs.map(spec ->
                    new Tuple2<>(spec, directoryPath.resolve(spec.getFileName())));

            while (!stopFlag.get()) {
                WatchKey key = watcher.poll(QUIT_POLL_INTERVAL_MILLISECONDS, TimeUnit.MILLISECONDS);
                if (key != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind.equals(StandardWatchEventKinds.ENTRY_CREATE) ||
                                kind.equals(StandardWatchEventKinds.ENTRY_MODIFY) ||
                                kind.equals(StandardWatchEventKinds.ENTRY_DELETE)) {

                            //noinspection unchecked
                            WatchEvent<Path> pathEvent = (WatchEvent<Path>) event;
                            Path eventFilePath = directoryPath.resolve(pathEvent.context());

                            watchSpecPaths.forEach(watchSpecPath -> {
                                FileWatchSpec watchSpec = watchSpecPath.getFirst();
                                Path watchPath = watchSpecPath.getSecond();
                                if (eventFilePath.equals(watchPath)) {
                                    watchSpec.getHandler().accept(pathEvent.kind());
                                }
                            });
                        }
                    }
                    key.reset();
                }
            }

            LOGGER.info("CurrDirectoryWatcher completed");
        } catch (Throwable ex) {
            LOGGER.fatal("CurrDirectoryWatcher crashed with exception {}", convertThrowable(ex));
        }
    }

}
