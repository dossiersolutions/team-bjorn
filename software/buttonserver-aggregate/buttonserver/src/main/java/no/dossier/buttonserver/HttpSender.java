package no.dossier.buttonserver;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public final class HttpSender {

    public static void sendHttp() throws IOException {
        URL url = new URL("127.0.0.1:12345");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        // connection.setRequestProperty("User-Agent", "ButtonServer");
    }

    private HttpSender() {
    }

}
