package no.dossier.buttonserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

import static no.dossier.buttonserver.util.ExceptionToStringConverter.convertException;

public final class LinuxShellRunner extends ShellRunner {

    private static final Logger LOGGER = LogManager.getLogger(LinuxShellRunner.class);

    @Override
    void runCommand(String command) {
        try {
            LOGGER.info("Running LinuxShellRunner with command {}", command);
            ProcessBuilder processBuilder = new ProcessBuilder("sh", command);
            processBuilder.start();
        } catch (IOException ex) {
            LOGGER.fatal("LinuxShellRunner with command {} crashed with exception {}", command, convertException(ex));
        }
    }

}
