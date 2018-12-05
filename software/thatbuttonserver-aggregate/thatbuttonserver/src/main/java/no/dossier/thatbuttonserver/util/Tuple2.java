package no.dossier.thatbuttonserver.util;

public final class Tuple2<A, B> {

    private final A first;
    private final B second;

    public Tuple2(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public A getFirst() {
        return first;
    }

    public B getSecond() {
        return second;
    }

    @Override
    public String toString() {
        return String.format("Tuple2(%s, %s)", first, second);
    }

    @Override
    public int hashCode() {
        return (723243769 * first.hashCode()) +
                (1439206579 * second.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (this == obj) {
            result = true;
        } else if (obj instanceof Tuple2) {
            Tuple2<?, ?> that = (Tuple2<?, ?>) obj;
            result = first.equals(that.first) && second.equals(that.second);
        } else {
            result = false;
        }
        return result;
    }

}
