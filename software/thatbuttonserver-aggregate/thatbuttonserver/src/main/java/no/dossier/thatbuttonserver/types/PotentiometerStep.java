package no.dossier.thatbuttonserver.types;

import no.dossier.thatbuttonserver.util.Result;

public final class PotentiometerStep implements Comparable<PotentiometerStep> {

    public static Result<String, PotentiometerStep> potentiometerStepResult(int value) {
        return Result.okIf(
                (value >= 0) && (value <= 8),
                () -> new PotentiometerStep(value),
                () -> String.format(
                        "Invalid potentiometerStep value (%d), must be between 0 and 8, inclusive",
                        value));
    }

    private final int value;

    private PotentiometerStep(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public int compareTo(PotentiometerStep that) {
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
        } else if (obj instanceof PotentiometerStep) {
            PotentiometerStep that = (PotentiometerStep) obj;
            result = value == that.value;
        } else {
            result = false;
        }
        return result;
    }

}
