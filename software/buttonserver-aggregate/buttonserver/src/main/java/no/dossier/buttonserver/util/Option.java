package no.dossier.buttonserver.util;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Option<A> {

    public static <A> Option<A> some(A value) {
        return new Some<>(value);
    }

    private static final None<Object> GENERIC_NONE = new None<>();

    public static <A> Option<A> none() {
        //noinspection unchecked
        return (Option<A>) GENERIC_NONE;
    }

    public static <A> Option<A> someIf(boolean cond, Supplier<A> onTrue) {
        return cond ? some(onTrue.get()) : none();
    }

    private Option() {
    }

    public final A unsafeGet() {
        return unwrap(
                value -> value,
                () -> {
                    throw new IllegalStateException();
                });
    }

    public final A getOrElse(Supplier<A> onNone) {
        return unwrap(
                value -> value,
                onNone);
    }

    public final <B> Option<B> map(Function<A, B> f) {
        return unwrap(
                value -> some(f.apply(value)),
                Option::none);
    }

    @Override
    public final String toString() {
        return unwrap(
                value -> String.format("Some(%s)", value),
                () -> "None");
    }

    @Override
    public final int hashCode() {
        return unwrap(
                value -> (179741777 * Objects.hashCode(value)) + 143043163,
                () -> 1277127847);
    }

    @Override
    public final boolean equals(Object obj) {
        boolean result;
        if (this == obj) {
            result = true;
        } else if (obj instanceof Option) {
            Option<?> that = (Option<?>) obj;
            result = unwrap(
                    thisValue -> that.unwrap(
                            thatValue -> Objects.equals(thisValue, thatValue),
                            () -> false),
                    () -> that.unwrap(
                            thatValue -> false,
                            () -> true));
        } else {
            result = false;
        }
        return result;
    }

    public abstract <B> B unwrap(Function<A, B> onSome, Supplier<B> onNone);


    private static final class Some<A> extends Option<A> {

        private final A value;

        private Some(A value) {
            this.value = value;
        }

        @Override
        public <B> B unwrap(Function<A, B> onSome, Supplier<B> onNone) {
            return onSome.apply(value);
        }

    }


    private static final class None<A> extends Option<A> {

        @Override
        public <B> B unwrap(Function<A, B> onSome, Supplier<B> onNone) {
            return onNone.get();
        }

    }

}
