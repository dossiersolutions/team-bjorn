package no.dossier.thatbuttonserver;

import no.dossier.thatbuttonserver.types.ButtonId;
import no.dossier.thatbuttonserver.types.ButtonState;
import no.dossier.thatbuttonserver.types.MessageCounter;
import no.dossier.thatbuttonserver.types.PotentiometerState;
import no.dossier.thatbuttonserver.types.PotentiometerStep;
import no.dossier.thatbuttonserver.types.ThatButtonState;
import no.dossier.thatbuttonserver.util.Result;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static no.dossier.thatbuttonserver.types.ButtonId.buttonIdResult;
import static no.dossier.thatbuttonserver.types.MessageCounter.messageCounterResult;
import static no.dossier.thatbuttonserver.types.PotentiometerState.potentiometerStateResult;
import static no.dossier.thatbuttonserver.types.PotentiometerStep.potentiometerStepResult;

public final class ThatButtonStateCodec {

    public static byte[] encodeThatButtonState(ThatButtonState thatButtonState) {
        byte[] bytes = new byte[10];
        encodeUnsignedShort(thatButtonState.getButtonId().getValue(), bytes, 0);
        encodeUnsignedShort(thatButtonState.getMessageCounter().getValue(), bytes, 2);
        encodeButtonState(thatButtonState.getButtonState(), bytes, 4);
        encodeUnsignedShort(thatButtonState.getPotentiometerState().getValue(), bytes, 6);
        encodeUnsignedShort(thatButtonState.getPotentiometerStep().getValue(), bytes, 8);
        return bytes;
    }

    public static Result<String, ThatButtonState> decodeThatButtonState(InputStream input) throws IOException {
        byte[] buf = new byte[11];
        int offset = 0;
        int numBytesRead = input.read(buf);
        while (numBytesRead > 0) {
            offset += numBytesRead;
            numBytesRead = input.read(buf,offset,buf.length-offset);
        }
        int sumNumBytesRead = offset;

        Result<String, ThatButtonState> result = Result.failUnless(
                sumNumBytesRead == 10,
                () -> {
                    ButtonId buttonId = buttonIdResult(decodeUnsignedShort(buf, 0)).unsafeGet();
                    MessageCounter messageCounter = messageCounterResult(decodeUnsignedShort(buf, 2)).unsafeGet();
                    Result<String, ButtonState> buttonStateResult = decodeButtonState(buf, 4);
                    Result<String, PotentiometerState> potentiometerStateResult =
                            potentiometerStateResult(decodeUnsignedShort(buf, 6));
                    Result<String, PotentiometerStep> potentiometerStepResult =
                            potentiometerStepResult(decodeUnsignedShort(buf, 8));

                    return buttonStateResult.map3(
                            potentiometerStateResult,
                            potentiometerStepResult,
                            buttonState -> potentiometerState -> potentiometerStep -> new ThatButtonState(
                                    buttonId,
                                    messageCounter,
                                    buttonState,
                                    potentiometerState,
                                    potentiometerStep));
                },
                () -> String.format("Invalid input length (%d)", sumNumBytesRead));

        return result.mapFail(error -> String.format(
                "%s: input = %s",
                error,
                Arrays.toString(Arrays.copyOf(buf, 10))));
    }

    private static Result<String, ButtonState> decodeButtonState(byte[] bytes, int offset) {
        Result<String, ButtonState> result;
        int buttonStateValue = decodeUnsignedShort(bytes, offset);
        switch (buttonStateValue) {
            case 0:
                result = Result.ok(ButtonState.BUTTON_UP);
                break;
            case 1:
                result = Result.ok(ButtonState.BUTTON_DOWN);
                break;
            default:
                result = Result.fail(String.format("Invalid buttonState value(%d)", buttonStateValue));
        }
        return result;
    }

    private static void encodeButtonState(ButtonState buttonState, byte[] bytes, int offset) {
        encodeUnsignedShort(
                (buttonState == ButtonState.BUTTON_UP) ? 0 : 1,
                bytes,
                offset);
    }

    private static int decodeUnsignedShort(byte[] bytes, int offset) {
        return ((bytes[offset] & 0xff) << 8) | (bytes[offset + 1] & 0xff);
    }

    private static void encodeUnsignedShort(int value, byte[] bytes, int offset) {
        bytes[offset] = (byte) (value >>> 8);
        bytes[offset + 1] = (byte) value;
    }

    private ThatButtonStateCodec() {
    }

}
