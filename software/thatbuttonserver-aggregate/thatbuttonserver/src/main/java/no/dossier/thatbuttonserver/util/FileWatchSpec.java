package no.dossier.thatbuttonserver.util;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.function.Consumer;

public final class FileWatchSpec {

    private final String fileName;
    private final Consumer<WatchEvent.Kind<Path>> handler;

    public FileWatchSpec(String fileName, Consumer<WatchEvent.Kind<Path>> handler) {
        this.fileName = fileName;
        this.handler = handler;
    }

    public String getFileName() {
        return fileName;
    }

    public Consumer<WatchEvent.Kind<Path>> getHandler() {
        return handler;
    }

    @Override
    public String toString() {
        return String.format("FileWatchSpec(%s)", fileName);
    }

    @Override
    public int hashCode() {
        return (7757861 * fileName.hashCode()) +
                (526396747 * System.identityHashCode(handler));
    }

    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (this == obj) {
            result = true;
        } else if (obj instanceof FileWatchSpec) {
            FileWatchSpec that = (FileWatchSpec) obj;
            result = fileName.equals(that.fileName) && (handler == that.handler);
        } else {
            result = false;
        }
        return result;
    }

}
