package no.dossier.buttonserver.types;

public final class ConfigVersion {

    private final int version;
    private final Config config;

    public ConfigVersion(Config config) {
        this(0, config);
    }

    private ConfigVersion(int version, Config config) {
        this.version = version;
        this.config = config;
    }

    public ConfigVersion setNewConfig(Config newConfig) {
        return new ConfigVersion(version + 1, newConfig);
    }

    public int getVersion() {
        return version;
    }

    public Config getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return String.format("ConfigVersion(%s, %s)", version, config);
    }

    @Override
    public int hashCode() {
        return (964420033 * version) +
                (350309203 * config.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (this == obj) {
            result = true;
        } else if (obj instanceof ConfigVersion) {
            ConfigVersion that = (ConfigVersion) obj;
            result = (version == that.version) &&
                    config.equals(that.config);
        } else {
            result = false;
        }
        return result;
    }

}
