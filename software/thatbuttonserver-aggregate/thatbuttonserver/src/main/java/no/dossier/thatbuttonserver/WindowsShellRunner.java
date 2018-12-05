package no.dossier.thatbuttonserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static no.dossier.thatbuttonserver.util.ExceptionToStringConverter.convertException;

public final class WindowsShellRunner extends ShellRunner {

    private static final Logger LOGGER = LogManager.getLogger(WindowsShellRunner.class);

    @Override
    void runCommand(String command) {
        try {
            LOGGER.info("Running WindowsShellRunner with command {}", command);
            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", command);
            processBuilder.start();
        } catch (IOException ex) {
            LOGGER.fatal("WindowsShellRunner with command {} crashed with exception {}", command, convertException(ex));
        }
    }

}
