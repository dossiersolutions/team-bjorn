package no.dossier.thatbuttonserver;

import no.dossier.thatbuttonserver.types.ButtonEvent;
import no.dossier.thatbuttonserver.types.ButtonMessage;
import no.dossier.thatbuttonserver.types.ButtonState;
import no.dossier.thatbuttonserver.types.EventType;
import no.dossier.thatbuttonserver.types.ThatButtonState;

public abstract class ShellRunner {

    private static String expandCommandTemplate(String commandTemplate, ButtonEvent event) {
        ButtonMessage message = event.getMessage();
        ThatButtonState thatButtonState = message.getThatButtonState();

        String command = commandTemplate
                .replaceAll("\\$EventType", encodeEventType(event.getEventType()))
                .replaceAll("\\$Timestamp", message.getMessageInstant().toString())
                .replaceAll("\\$ButtonId", Integer.toString(thatButtonState.getButtonId().getValue()))
                .replaceAll("\\$ButtonState", encodeButtonState(thatButtonState.getButtonState()))
                .replaceAll("\\$PotentiometerState", Integer.toString(thatButtonState.getPotentiometerState().getValue()))
                .replaceAll("\\$PotentiometerStep", Integer.toString(thatButtonState.getPotentiometerStep().getValue()))
                .replaceAll("\\$\\$", "$");

        return command;
    }

    private static String encodeEventType(EventType eventType) {
        String result;
        switch (eventType) {
            case INIT:
                result = "Init";
                break;
            case BUTTON_DOWN:
                result = "ButtonDown";
                break;
            case BUTTON_UP:
                result = "ButtonUp";
                break;
            case CLICK:
                result = "Click";
                break;
            case DOUBLE_CLICK:
                result = "DoubleClick";
                break;
            case POTENTIOMETER_STATE_CHANGE:
                result = "PotentiometerState";
                break;
            case POTENTIOMETER_STEP_CHANGE:
                result = "PotentiometerStep";
                break;
            default:
                throw new AssertionError();
        }
        return result;
    }

    private static String encodeButtonState(ButtonState buttonState) {
        String result;
        switch (buttonState) {
            case BUTTON_UP:
                result = "Up";
                break;
            case BUTTON_DOWN:
                result = "Down";
                break;
            default:
                throw new AssertionError();
        }
        return result;
    }

    public final void run(String commandTemplate, ButtonEvent event) {
        String command = expandCommandTemplate(commandTemplate, event);
        runCommand(command);
    }

    abstract void runCommand(String command);

}
