package no.dossier.thatbuttonserver;

import no.dossier.thatbuttonserver.types.ButtonId;
import no.dossier.thatbuttonserver.types.ButtonMessage;
import no.dossier.thatbuttonserver.types.ConfigVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static no.dossier.thatbuttonserver.util.ExceptionToStringConverter.convertException;

public final class MessageRouter implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(MessageRouter.class);

    private static final long QUIT_POLL_INTERVAL_MILLIS = 1000L;

    private final ShellRunner shellRunner;
    private final BlockingQueue<ButtonMessage> messageQueue;
    private final ExecutorService executorService;
    private final AtomicReference<ConfigVersion> configVersionRef;
    private final AtomicBoolean stopFlag;
    private final ConcurrentMap<ButtonId, BlockingQueue<ButtonMessage>> buttonMessageQueues;

    public MessageRouter(
            ShellRunner shellRunner,
            BlockingQueue<ButtonMessage> messageQueue,
            ExecutorService executorService,
            AtomicReference<ConfigVersion> configVersionRef, AtomicBoolean stopFlag) {

        this.shellRunner = shellRunner;
        this.messageQueue = messageQueue;
        this.executorService = executorService;
        this.configVersionRef = configVersionRef;
        this.stopFlag = stopFlag;
        buttonMessageQueues = new ConcurrentHashMap<>();
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Starting MessageRouter");

            AtomicBoolean messageHandlerStopFlag = new AtomicBoolean(false);

            // Always continue if there are more messages to process
            while (!stopFlag.get() || (messageQueue.peek() != null)) {
                ButtonMessage message = messageQueue.poll(QUIT_POLL_INTERVAL_MILLIS, TimeUnit.MILLISECONDS);
                if (message != null) {
                    ButtonId buttonId = message.getThatButtonState().getButtonId();
                    BlockingQueue<ButtonMessage> buttonMessageQueue = buttonMessageQueues.computeIfAbsent(
                            buttonId,
                            absentButtonId -> {
                                BlockingQueue<ButtonMessage> newButtonMessageQueue = new LinkedBlockingQueue<>();
                                executorService.submit(new MessageHandler(
                                        shellRunner,
                                        absentButtonId,
                                        newButtonMessageQueue,
                                        configVersionRef,
                                        messageHandlerStopFlag));
                                return newButtonMessageQueue;
                            });
                    buttonMessageQueue.put(message);
                }
            }

            // Signal message handlers to stop
            messageHandlerStopFlag.set(true);

            LOGGER.info("MessageRouter completed");
        } catch (InterruptedException ex) {
            LOGGER.fatal("MessageRouter crashed with exception {}", convertException(ex));
        }
    }

}
