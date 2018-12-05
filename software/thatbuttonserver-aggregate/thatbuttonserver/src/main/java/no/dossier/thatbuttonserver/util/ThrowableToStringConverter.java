package no.dossier.thatbuttonserver.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ThrowableToStringConverter {

    public static String convertThrowable(Throwable exception) {
        StringWriter exceptionWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(exceptionWriter));
        String exceptionStr = exceptionWriter.toString();
        return exceptionStr;
    }

    private ThrowableToStringConverter() {
    }

}
