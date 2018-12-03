package no.dossier.buttonserver.types;

public final class ButtonEvent {

    private final EventType eventType;
    private final ButtonMessage message;

    public ButtonEvent(
            EventType eventType,
            ButtonMessage message) {

        this.eventType = eventType;
        this.message = message;
    }

    public EventType getEventType() {
        return eventType;
    }

    public ButtonMessage getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("ButtonEvent(%s, %s)", eventType, message);
    }

    @Override
    public int hashCode() {
        return (1347315559 * eventType.hashCode()) + (1035942157 * message.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (this == obj) {
            result = true;
        } else if (obj instanceof ButtonEvent) {
            ButtonEvent that = (ButtonEvent) obj;
            result = (eventType == that.eventType) && message.equals(that.message);
        } else {
            result = false;
        }
        return result;
    }

}
