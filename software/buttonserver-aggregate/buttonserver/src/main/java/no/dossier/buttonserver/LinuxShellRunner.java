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
            String expandedCommand = String.format("-c \"%s\"", command);
            LOGGER.info("Running LinuxShellRunner with command sh {}", expandedCommand);
            ProcessBuilder processBuilder = new ProcessBuilder("sh", expandedCommand);
            processBuilder.start();
        } catch (IOException ex) {
            LOGGER.fatal("LinuxShellRunner with command {} crashed with exception {}", command, convertException(ex));
        }
    }

}
