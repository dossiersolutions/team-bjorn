package no.dossier.thatbuttonserver.types;

import java.time.Instant;

public final class ButtonMessage {

    private final Instant messageInstant;
    private final ThatButtonState thatButtonState;

    public ButtonMessage(Instant messageInstant, ThatButtonState thatButtonState) {
        this.messageInstant = messageInstant;
        this.thatButtonState = thatButtonState;
    }

    public Instant getMessageInstant() {
        return messageInstant;
    }

    public ThatButtonState getThatButtonState() {
        return thatButtonState;
    }

    @Override
    public String toString() {
        return String.format("ButtonMessage(%s, %s)", messageInstant, thatButtonState);
    }

    @Override
    public int hashCode() {
        return (1124425891 * messageInstant.hashCode()) +
                (1908397429 * thatButtonState.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (this == obj) {
            result = true;
        } else if (obj instanceof ButtonMessage) {
            ButtonMessage that = (ButtonMessage) obj;
            result = messageInstant.equals(that.messageInstant) &&
                    thatButtonState.equals(that.thatButtonState);
        } else {
            result = false;
        }
        return result;
    }

}
