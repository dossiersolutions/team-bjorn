package no.dossier.buttonserver.util;

@FunctionalInterface
public interface ThrowingConsumer<E extends Throwable, A> {

    public void accept(A arg) throws E;

}
