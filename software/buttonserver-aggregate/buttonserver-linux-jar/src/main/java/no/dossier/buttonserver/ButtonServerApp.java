package no.dossier.buttonserver;

import java.io.IOException;

public final class ButtonServerApp {

    public static void main(String[] args) throws IOException  {
        ButtonServer.run(new LinuxShellRunner());
    }

    private ButtonServerApp() {
    }

}
