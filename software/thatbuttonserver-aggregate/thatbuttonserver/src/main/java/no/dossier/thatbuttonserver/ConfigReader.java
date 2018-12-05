package no.dossier.thatbuttonserver;

import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import no.dossier.thatbuttonserver.types.Config;
import no.dossier.thatbuttonserver.util.Result;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import static no.dossier.thatbuttonserver.util.ExceptionToStringConverter.convertException;

public final class ConfigReader {

    private static final Logger LOGGER = LogManager.getLogger(ConfigReader.class);

    public static final String CONFIG_FILE_NAME = "buttonserver-conf.json";

    public static Result<String, Config> readConfig() throws IOException {
        File configFile = new File(CONFIG_FILE_NAME).getCanonicalFile();
        LOGGER.info("Reading config file {}", configFile);

        Result<String, Config> result;
        try (Reader input = new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8)) {
            JsonParser jsonParser = new JsonParser();
            JsonElement jsonElement = jsonParser.parse(input);
            result = JsonToConfigDecoder.decode(jsonElement);
        } catch (JsonIOException | JsonSyntaxException ex) {
            result = Result.fail(String.format("Failed to parse JSON: %s", convertException(ex)));
        }
        return result;
    }

    private ConfigReader() {
    }

}
