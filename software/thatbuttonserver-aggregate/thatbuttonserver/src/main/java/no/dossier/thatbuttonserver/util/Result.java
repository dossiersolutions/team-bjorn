package no.dossier.thatbuttonserver.util;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Result<E, A> {

    public static <E, A> Result<E, A> ok(A value) {
        return new Ok<>(value);
    }

    public static <E, A> Result<E, A> fail(E error) {
        return new Fail<>(error);
    }

    public static <E, A> Result<E, A> okIf(boolean cond, Supplier<A> onTrue, Supplier<E> onFalse) {
        return cond ? ok(onTrue.get()) : fail(onFalse.get());
    }

    public static <E, A> Result<E, A> failUnless(boolean cond, Supplier<Result<E, A>> onTrue, Supplier<E> onFalse) {
        return cond ? onTrue.get() : fail(onFalse.get());
    }

    public static <E, A> Result<E, A> optionResult(Option<A> option, Supplier<E> onNone) {
        return option.unwrap(
                Result::ok,
                () -> fail(onNone.get()));
    }

    private Result() {
    }

    public final boolean isOk() {
        return unwrap(
                value -> true,
                error -> false);
    }

    public final A unsafeGet() {
        return unwrap(
                value -> value,
                error -> {
                    throw new IllegalStateException(error.toString());
                });
    }

    public final <B> Result<E, B> map(Function<A, B> f) {
        return unwrap(
                value -> ok(f.apply(value)),
                Result::fail);
    }

    public final <E2> Result<E2, A> mapFail(Function<E, E2> f) {
        return unwrap(
                Result::ok,
                error -> fail(f.apply(error)));
    }

    public final <E2, B> Result<E2, B> bimap(Function<A, B> onOk, Function<E, E2> onFail) {
        return unwrap(
                value -> ok(onOk.apply(value)),
                error -> fail(onFail.apply(error)));
    }

    public final <B, C> Result<E, C> map2(Result<E, B> result2, Function<A, Function<B, C>> f) {
        return flatMap(value1 -> result2.map(f.apply(value1)));
    }

    public final <B, C, D> Result<E, D> map3(
            Result<E, B> result2,
            Result<E, C> result3,
            Function<A, Function<B, Function<C, D>>> f) {

        return map2(result2, f).map2(result3, func -> func);
    }

    public final <B> Result<E, B> flatMap(Function<A, Result<E, B>> f) {
        return unwrap(
                f,
                Result::fail);
    }

    public final <E2> Result<E2, A> orElse(Function<E, Result<E2, A>> f) {
        return unwrap(
                Result::ok,
                f);
    }

    @Override
    public final String toString() {
        return unwrap(
                value -> String.format("Ok(%s)", value),
                error -> String.format("Fail(%s)", error));
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    public abstract <B> B unwrap(Function<A, B> onOk, Function<E, B> onFail);

    public abstract <X extends Throwable> void branch(
            ThrowingConsumer<X, A> onOk,
            ThrowingConsumer<X, E> onFail) throws X;


    private static final class Ok<E, A> extends Result<E, A> {

        private final A value;

        private Ok(A value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return (305752081 * Objects.hashCode(value)) + 381918461;
        }

        @Override
        public boolean equals(Object obj) {
            boolean result;
            if (this == obj) {
                result = true;
            } else if (obj instanceof Ok) {
                Ok<?, ?> that = (Ok<?, ?>) obj;
                result = value.equals(that.value);
            } else {
                result = false;
            }
            return result;
        }

        @Override
        public <B> B unwrap(Function<A, B> onOk, Function<E, B> onFail) {
            return onOk.apply(value);
        }

        @Override
        public <X extends Throwable> void branch(ThrowingConsumer<X, A> onOk, ThrowingConsumer<X, E> onFail) throws X {
            onOk.accept(value);
        }

    }


    private static final class Fail<E, A> extends Result<E, A> {

        private final E error;

        private Fail(E error) {
            this.error = error;
        }

        @Override
        public int hashCode() {
            return (1349021873 * Objects.hashCode(error)) + 472276111;
        }

        @Override
        public boolean equals(Object obj) {
            boolean result;
            if (this == obj) {
                result = true;
            } else if (obj instanceof Fail) {
                Fail<?, ?> that = (Fail<?, ?>) obj;
                result = error.equals(that.error);
            } else {
                result = false;
            }
            return result;
        }

        @Override
        public <B> B unwrap(Function<A, B> onOk, Function<E, B> onFail) {
            return onFail.apply(error);
        }

        @Override
        public <X extends Throwable> void branch(ThrowingConsumer<X, A> onOk, ThrowingConsumer<X, E> onFail) throws X {
            onFail.accept(error);
        }

    }

}
