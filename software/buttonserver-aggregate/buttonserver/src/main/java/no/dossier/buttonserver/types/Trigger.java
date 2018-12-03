package no.dossier.buttonserver.types;

import no.dossier.buttonserver.util.List;

public final class Trigger {

    private final Condition condition;
    private final List<Action> actions;

    public Trigger(Condition condition, List<Action> actions) {
        this.condition = condition;
        this.actions = actions;
    }

    public Condition getCondition() {
        return condition;
    }

    public List<Action> getActions() {
        return actions;
    }

    @Override
    public String toString() {
        return String.format("Trigger(%s, %s)", condition, actions);
    }

    @Override
    public int hashCode() {
        return (1077074689 * condition.hashCode()) +
                (643747241 * actions.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (this == obj) {
            result = true;
        } else if (obj instanceof Trigger) {
            Trigger that = (Trigger) obj;
            result = condition.equals(that.condition) && actions.equals(that.actions);
        } else {
            result = false;
        }
        return result;
    }

}
