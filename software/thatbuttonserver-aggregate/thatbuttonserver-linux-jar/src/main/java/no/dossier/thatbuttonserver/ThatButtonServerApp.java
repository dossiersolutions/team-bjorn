package no.dossier.thatbuttonserver;

import java.io.IOException;

public final class ThatButtonServerApp {

    public static void main(String[] args) throws IOException  {
        ThatButtonServer.run(new LinuxShellRunner());
    }

    private ThatButtonServerApp() {
    }

}
