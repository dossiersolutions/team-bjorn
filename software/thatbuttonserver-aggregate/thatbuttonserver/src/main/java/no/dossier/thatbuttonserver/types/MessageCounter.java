package no.dossier.thatbuttonserver.types;

import no.dossier.thatbuttonserver.util.Result;

public final class MessageCounter {

    public static Result<String, MessageCounter> messageCounterResult(int value) {
        return Result.okIf(
                (value >= 0) && (value <= 65535),
                () -> new MessageCounter(value),
                () -> String.format(
                        "Invalid messageCounter value (%d), must be between 0 and 65535, inclusive",
                        value));
    }

    private final int value;

    private MessageCounter(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public MessageCounter incr() {
        return new MessageCounter((value + 1) & 0xffff);
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
        } else if (obj instanceof MessageCounter) {
            MessageCounter that = (MessageCounter) obj;
            result = value == that.value;
        } else {
            result = false;
        }
        return result;
    }

}
