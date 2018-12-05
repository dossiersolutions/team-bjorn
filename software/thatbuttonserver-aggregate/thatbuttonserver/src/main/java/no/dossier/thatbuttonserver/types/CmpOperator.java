package no.dossier.thatbuttonserver.types;

import java.util.function.BiPredicate;

public final class CmpOperator<A> {

    public static <A> CmpOperator<A> equals() {
        return new CmpOperator<>("EQUALS", Object::equals);
    }

    public static <A> CmpOperator<A> notEquals() {
        return new CmpOperator<>("NOT_EQUALS", (value1, value2) -> !value1.equals(value2));
    }

    public static <A extends Comparable<A>> CmpOperator<A> lessThan() {
        return new CmpOperator<>("LESS_THAN", (value1, value2) -> value1.compareTo(value2) < 0);
    }

    public static <A extends Comparable<A>> CmpOperator<A> lessOrEqual() {
        return new CmpOperator<>("LESS_OR_EQUAL", (value1, value2) -> value1.compareTo(value2) <= 0);
    }

    public static <A extends Comparable<A>> CmpOperator<A> greaterThan() {
        return new CmpOperator<>("GREATER_THAN", (value1, value2) -> value1.compareTo(value2) > 0);
    }

    public static <A extends Comparable<A>> CmpOperator<A> greaterOrEqual() {
        return new CmpOperator<>("GREATER_OR_EQUAL", (value1, value2) -> value1.compareTo(value2) >= 0);
    }

    private final String name;
    private final BiPredicate<A, A> testFunc;

    private CmpOperator(String name, BiPredicate<A, A> testFunc) {
        this.name = name;
        this.testFunc = testFunc;
    }

    public boolean test(A value1, A value2) {
        return testFunc.test(value1, value2);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        boolean result;
        if (this == obj) {
            result = true;
        } else if (obj instanceof CmpOperator) {
            CmpOperator<?> that = (CmpOperator<?>)obj;
            result = name.equals(that.name);
        } else {
            result = false;
        }
        return result;
    }

}
