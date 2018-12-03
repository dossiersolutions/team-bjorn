package no.dossier.buttonserver.types;

import java.time.Instant;
import java.util.function.Function;

public final class PropertyType<A> {

    public static final PropertyType<EventType> EVENT_TYPE = new PropertyType<>(
            "EVENT_TYPE",
            ButtonEvent::getEventType);

    public static final PropertyType<Instant> TIMESTAMP = new PropertyType<>(
            "TIMESTAMP",
            event -> event.getMessage().getMessageInstant());

    public static final PropertyType<ButtonId> BUTTON_ID = new PropertyType<>(
            "BUTTON_ID",
            event -> event.getMessage().getThatButtonState().getButtonId());

    public static final PropertyType<MessageCounter> MESSAGE_COUNTER = new PropertyType<>(
            "MESSAGE_COUNTER",
            event -> event.getMessage().getThatButtonState().getMessageCounter());

    public static final PropertyType<ButtonState> BUTTON_STATE = new PropertyType<>(
            "BUTTON_STATE",
            event -> event.getMessage().getThatButtonState().getButtonState());

    public static final PropertyType<PotentiometerState> POTENTIOMETER_STATE = new PropertyType<>(
            "POTENTIOMETER_STATE",
            event -> event.getMessage().getThatButtonState().getPotentiometerState());

    public static final PropertyType<PotentiometerStep> POTENTIOMETER_STEP = new PropertyType<>(
            "POTENTIOMETER_STEP",
            event -> event.getMessage().getThatButtonState().getPotentiometerStep());

    private final String name;
    private final Function<ButtonEvent, A> extractFunc;

    private PropertyType(String name, Function<ButtonEvent, A> extractFunc) {
        this.name = name;
        this.extractFunc = extractFunc;
    }

    public A extractEventValue(ButtonEvent buttonEvent) {
        return extractFunc.apply(buttonEvent);
    }

    @Override
    public String toString() {
        return name;
    }

}
