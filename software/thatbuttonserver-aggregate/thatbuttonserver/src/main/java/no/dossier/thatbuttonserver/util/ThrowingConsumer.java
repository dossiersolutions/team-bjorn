package no.dossier.thatbuttonserver.util;

@FunctionalInterface
public interface ThrowingConsumer<E extends Throwable, A> {

    public void accept(A arg) throws E;

}
