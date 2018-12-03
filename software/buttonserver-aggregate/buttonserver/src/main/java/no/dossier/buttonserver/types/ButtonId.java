package no.dossier.buttonserver.types;

import no.dossier.buttonserver.util.Result;

public final class ButtonId {

    public static Result<String, ButtonId> buttonIdResult(int value) {
        return Result.okIf(
                (value >= 0) && (value <= 65535),
                () -> new ButtonId(value),
                () -> String.format("Invalid buttonId value (%d), must be between 0 and 65535, inclusive", value));
    }

    private final int value;

    private ButtonId(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (this == obj) {
            result = true;
        } else if (obj instanceof ButtonId) {
            ButtonId that = (ButtonId) obj;
            result = value == that.value;
        } else {
            result = false;
        }
        return result;
    }

}
