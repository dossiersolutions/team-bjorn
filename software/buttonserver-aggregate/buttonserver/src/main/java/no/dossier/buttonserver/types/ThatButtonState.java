package no.dossier.buttonserver.types;

public final class ThatButtonState {

    private final ButtonId buttonId;
    private final MessageCounter messageCounter;
    private final ButtonState buttonState;
    private final PotentiometerState potentiometerState;
    private final PotentiometerStep potentiometerStep;

    public ThatButtonState(
            ButtonId buttonId,
            MessageCounter messageCounter,
            ButtonState buttonState,
            PotentiometerState potentiometerState, PotentiometerStep potentiometerStep) {

        this.buttonId = buttonId;
        this.messageCounter = messageCounter;
        this.buttonState = buttonState;
        this.potentiometerState = potentiometerState;
        this.potentiometerStep = potentiometerStep;
    }

    public ButtonId getButtonId() {
        return buttonId;
    }

    public MessageCounter getMessageCounter() {
        return messageCounter;
    }

    public ButtonState getButtonState() {
        return buttonState;
    }

    public PotentiometerState getPotentiometerState() {
        return potentiometerState;
    }

    public PotentiometerStep getPotentiometerStep() {
        return potentiometerStep;
    }

    @Override
    public String toString() {
        return String.format(
                "ThatButtonState(%s, %s, %s, %s, %s)",
                buttonId,
                messageCounter,
                buttonState,
                potentiometerState,
                potentiometerStep);
    }

    @Override
    public int hashCode() {
        return (261716179 * buttonId.hashCode()) +
                (1985186993 * messageCounter.hashCode()) +
                (1903723643 * buttonState.hashCode()) +
                (1581644807 * potentiometerState.hashCode()) +
                (2014512653 * potentiometerStep.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (this == obj) {
            result = true;
        } else if (obj instanceof ThatButtonState) {
            ThatButtonState that = (ThatButtonState) obj;
            result = buttonId.equals(that.buttonId) &&
                    messageCounter.equals(that.messageCounter) &&
                    (buttonState == that.buttonState) &&
                    potentiometerState.equals(that.potentiometerState) &&
                    potentiometerStep.equals(that.potentiometerStep);
        } else {
            result = false;
        }
        return result;
    }

}
