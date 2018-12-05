package no.dossier.thatbuttonserver.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class ExceptionToStringConverter {

    public static <E extends Throwable> String convertException(E exception) {
        StringWriter exceptionWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(exceptionWriter));
        String exceptionStr = exceptionWriter.toString();
        return exceptionStr;
    }

    private ExceptionToStringConverter() {
    }

}
