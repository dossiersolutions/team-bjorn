package no.dossier.buttonserver.types;

import no.dossier.buttonserver.util.List;

public final class Config {

    private final Settings settings;
    private final List<Trigger> triggers;

    public Config(Settings settings, List<Trigger> triggers) {
        this.settings = settings;
        this.triggers = triggers;
    }

    public Settings getSettings() {
        return settings;
    }

    public List<Trigger> getTriggers() {
        return triggers;
    }

    @Override
    public String toString() {
        return String.format("Config(%s, %s)", settings, triggers);
    }

    @Override
    public int hashCode() {
        return (854318021 * settings.hashCode()) +
                (1243589911 * triggers.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (this == obj) {
            result = true;
        } else if (obj instanceof Config) {
            Config that = (Config) obj;
            result = settings.equals(that.settings) && triggers.equals(that.triggers);
        } else {
            result = false;
        }
        return result;
    }

}
