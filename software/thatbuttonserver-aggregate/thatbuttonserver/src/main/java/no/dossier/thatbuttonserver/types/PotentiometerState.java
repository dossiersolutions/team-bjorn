package no.dossier.thatbuttonserver.types;

import no.dossier.thatbuttonserver.util.Result;

public final class PotentiometerState implements Comparable<PotentiometerState> {

    public static Result<String, PotentiometerState> potentiometerStateResult(int value) {
        return Result.okIf(
                (value >= 0) && (value <= 1023),
                () -> new PotentiometerState(value),
                () -> String.format(
                        "Invalid potentiometerState value (%d), must be between 0 and 1023, inclusive",
                        value));
    }

    private final int value;

    private PotentiometerState(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int compareTo(PotentiometerState that) {
        return Integer.compare(value, that.value);
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
        } else if (obj instanceof PotentiometerState) {
            PotentiometerState that = (PotentiometerState) obj;
            result = value == that.value;
        } else {
            result = false;
        }
        return result;
    }

}
