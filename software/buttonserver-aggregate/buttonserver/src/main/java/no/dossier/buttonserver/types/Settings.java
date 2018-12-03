package no.dossier.buttonserver.types;

public final class Settings {

    private final int port;

    public Settings(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return String.format("Settings(port = %d)",port);
    }

    @Override
    public int hashCode() {
        return 1132632979*port;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (this == obj) {
            result = true;
        } else if (obj instanceof Settings) {
            Settings that = (Settings)obj;
            result = port == that.port;
        } else {
            result = false;
        }
        return result;
    }

}
