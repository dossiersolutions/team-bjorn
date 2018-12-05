package no.dossier.thatbuttonserver;

import no.dossier.thatbuttonserver.types.ButtonMessage;
import no.dossier.thatbuttonserver.types.ConfigVersion;
import no.dossier.thatbuttonserver.types.ThatButtonState;
import no.dossier.thatbuttonserver.util.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static no.dossier.thatbuttonserver.ThatButtonStateCodec.decodeThatButtonState;
import static no.dossier.thatbuttonserver.util.ThrowableToStringConverter.convertThrowable;

public final class SocketListener implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(SocketListener.class);

    private static final int QUIT_POLL_INTERVAL_MILLIS = 1000;

    private final BlockingQueue<ButtonMessage> messageQueue;
    private final AtomicReference<ConfigVersion> configVersionRef;
    private final AtomicBoolean stopFlag;
    private int currPort;

    public SocketListener(
            BlockingQueue<ButtonMessage> messageQueue,
            AtomicReference<ConfigVersion> configVersionRef,
            AtomicBoolean stopFlag) {

        this.messageQueue = messageQueue;
        this.configVersionRef = configVersionRef;
        this.stopFlag = stopFlag;
        currPort = configVersionRef.get().getConfig().getSettings().getPort();
    }

    @Override
    public void run() {
        try {
            LOGGER.info("Starting SocketListener");
            while (!stopFlag.get()) {
                LOGGER.info("Listening on port {}", currPort);
                try (ServerSocket serverSocket = new ServerSocket(currPort)) {
                    serverSocket.setSoTimeout(QUIT_POLL_INTERVAL_MILLIS);

                    while (!stopFlag.get() &&
                            (configVersionRef.get().getConfig().getSettings().getPort() == currPort)) {

                        try (
                                Socket socket = serverSocket.accept();
                                InputStream input = socket.getInputStream()) {

                            Instant messageInstant = Instant.now();
                            Result<String, ThatButtonState> thatButtonStateResult = decodeThatButtonState(input);
                            thatButtonStateResult.branch(
                                    thatButtonState -> messageQueue.put(new ButtonMessage(
                                            messageInstant,
                                            thatButtonState)),
                                    decodeError -> LOGGER.error("Invalid message: {}", decodeError));
                        } catch (SocketTimeoutException ex) {
                            // Catch & swallow
                        }
                    }
                }
                currPort = configVersionRef.get().getConfig().getSettings().getPort();
            }
            LOGGER.info("SocketListener completed");
        } catch (InterruptedException | IOException ex) {
            LOGGER.fatal("SocketListener crashed with exception {}", convertThrowable(ex));
        }
    }

}
