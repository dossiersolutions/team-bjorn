package no.dossier.buttonserver;

import no.dossier.buttonserver.types.ButtonEvent;
import no.dossier.buttonserver.types.ButtonId;
import no.dossier.buttonserver.types.ButtonMessage;
import no.dossier.buttonserver.types.ConfigVersion;
import no.dossier.buttonserver.types.EventType;
import no.dossier.buttonserver.types.PotentiometerState;
import no.dossier.buttonserver.types.PotentiometerStep;
import no.dossier.buttonserver.types.ThatButtonState;
import no.dossier.buttonserver.types.Trigger;
import no.dossier.buttonserver.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

import static no.dossier.buttonserver.ThatButtonStateCodec.encodeThatButtonState;
import static no.dossier.buttonserver.util.ExceptionToStringConverter.convertException;

public final class MessageHandler implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(MessageHandler.class);

    private static final long CLICK_INTERVAL_MILLIS = 200L;
    private static final long QUIT_POLL_INTERVAL_MILLIS = 1000L;

    private final ShellRunner shellRunner;
    private final ButtonId buttonId;
    private final BlockingQueue<ButtonMessage> messageQueue;
    private final AtomicReference<ConfigVersion> configVersionRef;
    private final AtomicBoolean stopFlag;
    private ConfigVersion currConfigVersion;

    public MessageHandler(
            ShellRunner shellRunner,
            ButtonId buttonId,
            BlockingQueue<ButtonMessage> messageQueue,
            AtomicReference<ConfigVersion> configVersionRef,
            AtomicBoolean stopFlag) {

        this.shellRunner = shellRunner;
        this.buttonId = buttonId;
        this.messageQueue = messageQueue;
        this.configVersionRef = configVersionRef;
        this.stopFlag = stopFlag;
        currConfigVersion = configVersionRef.get();
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Starting MessageHandler for button {}", buttonId.getValue());

            State state = new InitState();
            while (!state.isStopState()) {
                ConfigVersion newConfigVersion = configVersionRef.get();
                if (newConfigVersion.getVersion() != currConfigVersion.getVersion()) {
                    state = state.resetClickHandling();
                    currConfigVersion = newConfigVersion;
                }
                state = state.next();
            }

            LOGGER.info("MessageHandler for button {} completed", buttonId.getValue());
        } catch (InterruptedException ex) {
            LOGGER.fatal(
                    "MessageHandler for button {} crashed with exception {}",
                    buttonId.getValue(),
                    convertException(ex));
        }
    }


    private void handlePotentiometerChange(
            PotentiometerState prevPotentiometerState,
            PotentiometerStep prevPotentiometerStep,
            ButtonMessage message) {

        ThatButtonState thatButtonState = message.getThatButtonState();

        PotentiometerState newPotentiometerState = thatButtonState.getPotentiometerState();
        if (!newPotentiometerState.equals(prevPotentiometerState)) {
            handleEvent(new ButtonEvent(EventType.POTENTIOMETER_STATE_CHANGE, message));
        }

        PotentiometerStep newPotentiometerStep = thatButtonState.getPotentiometerStep();
        if (!newPotentiometerStep.equals(prevPotentiometerStep)) {
            handleEvent(new ButtonEvent(EventType.POTENTIOMETER_STEP_CHANGE, message));
        }
    }

    private void handleEvent(EventType eventType, ButtonMessage message) {
        handleEvent(new ButtonEvent(eventType, message));
    }

    private void handleEvent(ButtonEvent event) {
        handleEvent(event, currConfigVersion.getConfig().getTriggers());
    }

    private void handleEvent(ButtonEvent event, List<Trigger> triggers) {
        triggers.forEach(trigger -> {
            if (trigger.getCondition().eval(event)) {
                trigger.getActions().forEach(action -> action.branch(
                        commandTemplate -> {
                            // It seems that we don't need to create a separate thread for this
                            // At least not as long as ShellRunner uses Runtime.getRuntime().exec
                            shellRunner.run(commandTemplate, event);
                        },
                        (hostName, port) -> {
                            try (
                                    Socket socket = new Socket(hostName, port);
                                    OutputStream out = socket.getOutputStream()) {

                                out.write(encodeThatButtonState(event.getMessage().getThatButtonState()));
                            } catch (IOException ex) {
                                LOGGER.error(
                                        "Failed to forward message to {}:{} with exception {}",
                                        hostName,
                                        port,
                                        ex);
                            }
                        },
                        () -> LOGGER.info(event),
                        childTriggers -> handleEvent(event, childTriggers)));
            }
        });
    }


    private abstract static class State {

        abstract boolean isStopState();

        abstract State next() throws InterruptedException;

        abstract State resetClickHandling();

    }


    private final class InitState extends State {

        @Override
        boolean isStopState() {
            return false;
        }

        @Override
        State next() throws InterruptedException {
            /*
             * Assume that the button initially is in the UP position.
             *
             * The implication is that we produce a BUTTON_DOWN event if the first message has ButtonState BUTTON_DOWN,
             * but no event if the first message has ButtonState BUTTON_UP.
             */

            return nextState(
                    QUIT_POLL_INTERVAL_MILLIS,
                    message -> {
                        handleEvent(EventType.INIT, message);

                        ThatButtonState thatButtonState = message.getThatButtonState();

                        return new Up(
                                thatButtonState.getPotentiometerState(),
                                thatButtonState.getPotentiometerStep());
                    },
                    message -> {
                        handleEvent(EventType.INIT, message);

                        ThatButtonState thatButtonState = message.getThatButtonState();

                        return new DownMaybeClick(
                                thatButtonState.getPotentiometerState(),
                                thatButtonState.getPotentiometerStep());
                    },
                    () -> this);
        }

        @Override
        State resetClickHandling() {
            return this;
        }

    }


    private final class Up extends State {

        private final PotentiometerState potentiometerState;
        private final PotentiometerStep potentiometerStep;

        private Up(PotentiometerState potentiometerState, PotentiometerStep potentiometerStep) {
            this.potentiometerState = potentiometerState;
            this.potentiometerStep = potentiometerStep;
        }

        @Override
        boolean isStopState() {
            return false;
        }

        @Override
        State next() throws InterruptedException {
            return nextState(
                    QUIT_POLL_INTERVAL_MILLIS,
                    message -> {
                        handlePotentiometerChange(potentiometerState, potentiometerStep, message);

                        ThatButtonState thatButtonState = message.getThatButtonState();
                        return new Up(thatButtonState.getPotentiometerState(), thatButtonState.getPotentiometerStep());
                    },
                    message -> {
                        handlePotentiometerChange(potentiometerState, potentiometerStep, message);
                        handleEvent(EventType.BUTTON_DOWN, message);

                        ThatButtonState thatButtonState = message.getThatButtonState();

                        return new DownMaybeClick(
                                thatButtonState.getPotentiometerState(),
                                thatButtonState.getPotentiometerStep());
                    },
                    () -> this);
        }

        @Override
        State resetClickHandling() {
            return this;
        }

    }


    private final class Down extends State {

        private final PotentiometerState potentiometerState;
        private final PotentiometerStep potentiometerStep;

        private Down(PotentiometerState potentiometerState, PotentiometerStep potentiometerStep) {
            this.potentiometerState = potentiometerState;
            this.potentiometerStep = potentiometerStep;
        }

        @Override
        boolean isStopState() {
            return false;
        }

        @Override
        State next() throws InterruptedException {
            return nextState(
                    QUIT_POLL_INTERVAL_MILLIS,
                    message -> {
                        handlePotentiometerChange(potentiometerState, potentiometerStep, message);
                        handleEvent(EventType.BUTTON_UP, message);

                        ThatButtonState thatButtonState = message.getThatButtonState();

                        return new Up(thatButtonState.getPotentiometerState(), thatButtonState.getPotentiometerStep());
                    },
                    message -> {
                        handlePotentiometerChange(potentiometerState, potentiometerStep, message);

                        ThatButtonState thatButtonState = message.getThatButtonState();

                        return new Down(
                                thatButtonState.getPotentiometerState(),
                                thatButtonState.getPotentiometerStep());
                    },
                    () -> this);
        }

        @Override
        State resetClickHandling() {
            return this;
        }

    }


    private final class DownMaybeClick extends State {

        private final PotentiometerState potentiometerState;
        private final PotentiometerStep potentiometerStep;

        private DownMaybeClick(PotentiometerState potentiometerState, PotentiometerStep potentiometerStep) {
            this.potentiometerState = potentiometerState;
            this.potentiometerStep = potentiometerStep;
        }

        @Override
        boolean isStopState() {
            return false;
        }

        @Override
        State next() throws InterruptedException {
            return nextState(
                    CLICK_INTERVAL_MILLIS,
                    message -> {
                        handlePotentiometerChange(potentiometerState, potentiometerStep, message);
                        handleEvent(EventType.BUTTON_UP, message);

                        ThatButtonState thatButtonState = message.getThatButtonState();

                        return new UpAfterClick(
                                message,
                                thatButtonState.getPotentiometerState(),
                                thatButtonState.getPotentiometerStep());
                    },
                    message -> {
                        handlePotentiometerChange(potentiometerState, potentiometerStep, message);

                        ThatButtonState thatButtonState = message.getThatButtonState();

                        return new DownMaybeClick(
                                thatButtonState.getPotentiometerState(),
                                thatButtonState.getPotentiometerStep());
                    },
                    () -> new Down(potentiometerState, potentiometerStep));
        }

        @Override
        State resetClickHandling() {
            return new Down(potentiometerState, potentiometerStep);
        }

    }


    private final class UpAfterClick extends State {

        private final ButtonMessage releaseMessage;
        private final PotentiometerState potentiometerState;
        private final PotentiometerStep potentiometerStep;

        private UpAfterClick(
                ButtonMessage releaseMessage,
                PotentiometerState potentiometerState,
                PotentiometerStep potentiometerStep) {

            this.releaseMessage = releaseMessage;
            this.potentiometerState = potentiometerState;
            this.potentiometerStep = potentiometerStep;
        }

        @Override
        boolean isStopState() {
            return false;
        }

        @Override
        State next() throws InterruptedException {
            return nextState(
                    CLICK_INTERVAL_MILLIS,
                    message -> {
                        handlePotentiometerChange(potentiometerState, potentiometerStep, message);

                        ThatButtonState thatButtonState = message.getThatButtonState();

                        return new UpAfterClick(
                                releaseMessage,
                                thatButtonState.getPotentiometerState(),
                                thatButtonState.getPotentiometerStep());
                    },
                    message -> {
                        handlePotentiometerChange(potentiometerState, potentiometerStep, message);
                        handleEvent(EventType.BUTTON_DOWN, message);

                        ThatButtonState thatButtonState = message.getThatButtonState();

                        return new DownMaybeDoubleClick(
                                releaseMessage,
                                thatButtonState.getPotentiometerState(),
                                thatButtonState.getPotentiometerStep());
                    },
                    () -> {
                        handleEvent(EventType.CLICK, releaseMessage);

                        return new Up(potentiometerState, potentiometerStep);
                    });
        }

        @Override
        State resetClickHandling() {
            return new Up(potentiometerState, potentiometerStep);
        }

    }


    private final class DownMaybeDoubleClick extends State {

        private final ButtonMessage releaseMessage;
        private final PotentiometerState potentiometerState;
        private final PotentiometerStep potentiometerStep;

        private DownMaybeDoubleClick(
                ButtonMessage releaseMessage,
                PotentiometerState potentiometerState,
                PotentiometerStep potentiometerStep) {

            this.releaseMessage = releaseMessage;
            this.potentiometerState = potentiometerState;
            this.potentiometerStep = potentiometerStep;
        }

        @Override
        boolean isStopState() {
            return false;
        }

        @Override
        State next() throws InterruptedException {
            return nextState(
                    CLICK_INTERVAL_MILLIS,
                    message -> {
                        handlePotentiometerChange(potentiometerState, potentiometerStep, message);

                        handleEvent(EventType.BUTTON_UP, message);
                        handleEvent(EventType.DOUBLE_CLICK, message);

                        ThatButtonState thatButtonState = message.getThatButtonState();

                        return new Up(thatButtonState.getPotentiometerState(), thatButtonState.getPotentiometerStep());
                    },
                    message -> {
                        handlePotentiometerChange(potentiometerState, potentiometerStep, message);

                        ThatButtonState thatButtonState = message.getThatButtonState();

                        return new DownMaybeDoubleClick(
                                releaseMessage,
                                thatButtonState.getPotentiometerState(),
                                thatButtonState.getPotentiometerStep());
                    },
                    () -> {
                        handleEvent(EventType.CLICK, releaseMessage);

                        return new Down(potentiometerState, potentiometerStep);
                    });
        }

        @Override
        State resetClickHandling() {
            return new Down(potentiometerState, potentiometerStep);
        }

    }


    private static final class StopState extends State {

        @Override
        boolean isStopState() {
            return true;
        }

        @Override
        State next() {
            return this;
        }

        @Override
        State resetClickHandling() {
            return this;
        }

    }


    private State nextState(
            long timeoutMillis,
            Function<ButtonMessage, State> onButtonUp,
            Function<ButtonMessage, State> onButtonDown,
            Supplier<State> onTimeout) throws InterruptedException {

        State nextState;
        ButtonMessage message = messageQueue.poll(timeoutMillis, TimeUnit.MILLISECONDS);
        if (message != null) {
            ThatButtonState thatButtonState = message.getThatButtonState();
            switch (thatButtonState.getButtonState()) {
                case BUTTON_UP:
                    nextState = onButtonUp.apply(message);
                    break;
                case BUTTON_DOWN:
                    nextState = onButtonDown.apply(message);
                    break;
                default:
                    throw new AssertionError();
            }
        } else {
            nextState = stopFlag.get() ?
                    new StopState() :
                    onTimeout.get();
        }
        return nextState;
    }

}
