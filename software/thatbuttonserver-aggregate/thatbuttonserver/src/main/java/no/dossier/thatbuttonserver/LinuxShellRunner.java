package no.dossier.thatbuttonserver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static no.dossier.thatbuttonserver.util.ThrowableToStringConverter.convertThrowable;

public final class LinuxShellRunner extends ShellRunner {

    private static final Logger LOGGER = LogManager.getLogger(LinuxShellRunner.class);

    @Override
    void runCommand(String command) {
        try {
            LOGGER.info("Running LinuxShellRunner with command sh -c {}", command);
            ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
            Process process = processBuilder.start();
            new BufferedReader(new InputStreamReader(process.getInputStream())).lines().forEach(line -> LOGGER.info("Output: {}", line));
            new BufferedReader(new InputStreamReader(process.getErrorStream())).lines().forEach(line -> LOGGER.info("Error: {}", line));
        } catch (IOException ex) {
            LOGGER.fatal("LinuxShellRunner with command {} crashed with exception {}", command, convertThrowable(ex));
        }
    }

}
