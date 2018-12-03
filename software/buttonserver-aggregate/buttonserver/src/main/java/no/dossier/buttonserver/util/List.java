package no.dossier.buttonserver.util;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class List<A> {

    public static <A> List<A> cons(A head, List<A> tail) {
        return new Cons<>(head, tail);
    }

    private static final List<Object> GENERIC_NIL = new Nil<>();

    public static <A> List<A> nil() {
        //noinspection unchecked
        return (List<A>) GENERIC_NIL;
    }

    @SafeVarargs
    public static <A> List<A> list(A... elems) {
        List<A> result = nil();
        for (int i = elems.length-1; i >= 0; --i) {
            result = cons(elems[i],result);
        }
        return result;
    }

    private List() {
    }

    public abstract boolean exists(Predicate<A> pred);

    public abstract boolean forAll(Predicate<A> pred);

    public final List<A> filter(Predicate<A> pred) {
        return foldRight((elem,acc) -> pred.test(elem) ? cons(elem,acc) : acc, nil());
    }

    public final List<A> reverse() {
        return foldLeft((acc, elem) -> cons(elem, acc), nil());
    }

    public abstract <B> B foldLeft(BiFunction<B, A, B> reduceFunc, B initValue);

    public abstract <B> B foldRight(BiFunction<A, B, B> reduceFunc, B initValue);

    public abstract <E extends Throwable> void forEach(ThrowingConsumer<E,A> handler) throws E;

    @Override
    public final String toString() {
        StringBuilder builder = new StringBuilder();
        return unwrap(
                (head,tail) -> {
                    builder.append('[').append(head);
                    return tail.tailToString(builder);
                },
                () -> "[]"        );
    }

    abstract String tailToString(StringBuilder builder);

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    public abstract <B> B unwrap(BiFunction<A, List<A>, B> onCons, Supplier<B> onNil);


    private static final class Cons<A> extends List<A> {

        private final A head;
        private final List<A> tail;

        private Cons(A head, List<A> tail) {
            this.head = head;
            this.tail = tail;
        }

        @Override
        public boolean exists(Predicate<A> pred) {
            return pred.test(head) || tail.exists(pred);
        }

        @Override
        public boolean forAll(Predicate<A> pred) {
            return pred.test(head) && tail.forAll(pred);
        }

        @Override
        public <B> B foldLeft(BiFunction<B, A, B> reduceFunc, B initValue) {
            return tail.foldLeft(reduceFunc, reduceFunc.apply(initValue, head));
        }

        @Override
        public <B> B foldRight(BiFunction<A, B, B> reduceFunc, B initValue) {
            return reduceFunc.apply(head, tail.foldRight(reduceFunc, initValue));
        }

        @Override
        public <E extends Throwable> void forEach(ThrowingConsumer<E, A> handler) throws E {
            handler.accept(head);
            tail.forEach(handler);
        }

        @Override
        String tailToString(StringBuilder builder) {
            builder.append(", ").append(head);
            return tail.tailToString(builder);
        }

        @Override
        public int hashCode() {
            return (208872241 * head.hashCode()) +
                    (1987440031 * tail.hashCode()) +
                    612460021;
        }

        @Override
        public boolean equals(Object obj) {
            boolean result;
            if (this == obj) {
                result = true;
            } else if (obj instanceof Cons) {
                Cons<?> that = (Cons<?>) obj;
                result = head.equals(that.head) && tail.equals(that.tail);
            } else {
                result = false;
            }
            return result;
        }

        @Override
        public <B> B unwrap(BiFunction<A, List<A>, B> onCons, Supplier<B> onNil) {
            return onCons.apply(head, tail);
        }

    }


    private static final class Nil<A> extends List<A> {

        @Override
        public boolean exists(Predicate<A> pred) {
            return false;
        }

        @Override
        public boolean forAll(Predicate<A> pred) {
            return true;
        }

        @Override
        public <B> B foldLeft(BiFunction<B, A, B> reduceFunc, B initValue) {
            return initValue;
        }

        @Override
        public <B> B foldRight(BiFunction<A, B, B> reduceFunc, B initValue) {
            return initValue;
        }

        @Override
        public <E extends Throwable> void forEach(ThrowingConsumer<E, A> handler) {
            // Do nothing
        }

        @Override
        String tailToString(StringBuilder builder) {
            builder.append(']');
            return builder.toString();
        }

        @Override
        public int hashCode() {
            return 201360707;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Nil;
        }

        @Override
        public <B> B unwrap(BiFunction<A, List<A>, B> onCons, Supplier<B> onNil) {
            return onNil.get();
        }

    }

}
