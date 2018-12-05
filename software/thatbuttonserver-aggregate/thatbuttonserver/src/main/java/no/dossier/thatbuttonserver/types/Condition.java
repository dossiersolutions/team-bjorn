package no.dossier.thatbuttonserver.types;

import no.dossier.thatbuttonserver.util.List;
import no.dossier.thatbuttonserver.util.Option;

public abstract class Condition {

    private static final Condition ALWAYS = new Always();

    public static Condition always() {
        return ALWAYS;
    }

    private static final Condition NEVER = new Never();

    public static Condition never() {
        return NEVER;
    }

    public static Condition and(List<Condition> conditions) {
        return new And(conditions);
    }

    public static Condition or(List<Condition> conditions) {
        return new Or(conditions);
    }

    public static Condition not(Condition condition) {
        return new Not(condition);
    }

    public static <A> Condition propertyComparison(
            PropertyType<A> propertyType,
            CmpOperator<A> operator,
            A value) {

        return new PropertyComparison<>(propertyType, operator, value);
    }

    public static <A extends Comparable<A>> Option<Condition> propertyBetweenOption(
            PropertyType<A> propertyType,
            A minValue,
            A maxValue) {

        return Option.someIf(
                minValue.compareTo(maxValue) <= 0,
                () -> new PropertyBetween<>(propertyType, minValue, maxValue));
    }

    public static <A> Condition propertyIn(PropertyType<A> propertyType, List<A> values) {
        return new PropertyIn<>(propertyType, values);
    }

    private Condition() {
    }

    public abstract boolean eval(ButtonEvent event);

    @Override
    public abstract String toString();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);


    private static final class Always extends Condition {

        @Override
        public boolean eval(ButtonEvent event) {
            return true;
        }

        @Override
        public String toString() {
            return "Always";
        }

        @Override
        public int hashCode() {
            return 888377429;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Always;
        }

    }


    private static final class Never extends Condition {

        @Override
        public boolean eval(ButtonEvent event) {
            return false;
        }

        @Override
        public String toString() {
            return "Never";
        }

        @Override
        public int hashCode() {
            return 2129258893;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof Never;
        }

    }


    private static final class And extends Condition {

        private final List<Condition> conditions;

        private And(List<Condition> conditions) {
            this.conditions = conditions;
        }

        @Override
        public boolean eval(ButtonEvent event) {
            return conditions.forAll(condition -> condition.eval(event));
        }

        @Override
        public String toString() {
            return String.format("And(%s)", conditions);
        }

        @Override
        public int hashCode() {
            return conditions.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            boolean result;
            if (this == obj) {
                result = true;
            } else if (obj instanceof And) {
                And that = (And) obj;
                result = conditions.equals(that.conditions);
            } else {
                result = false;
            }
            return result;
        }

    }


    private static final class Or extends Condition {

        private final List<Condition> conditions;

        private Or(List<Condition> conditions) {
            this.conditions = conditions;
        }

        @Override
        public boolean eval(ButtonEvent event) {
            return conditions.exists(condition -> condition.eval(event));
        }

        @Override
        public String toString() {
            return String.format("Or(%s)", conditions);
        }

        @Override
        public int hashCode() {
            return conditions.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            boolean result;
            if (this == obj) {
                result = true;
            } else if (obj instanceof Or) {
                Or that = (Or) obj;
                result = conditions.equals(that.conditions);
            } else {
                result = false;
            }
            return result;
        }

    }


    private static final class Not extends Condition {

        private final Condition condition;

        private Not(Condition condition) {
            this.condition = condition;
        }

        @Override
        public boolean eval(ButtonEvent event) {
            return !condition.eval(event);
        }

        @Override
        public String toString() {
            return String.format("Not(%s)", condition);
        }

        @Override
        public int hashCode() {
            return condition.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            boolean result;
            if (this == obj) {
                result = true;
            } else if (obj instanceof Not) {
                Not that = (Not) obj;
                result = condition.equals(that.condition);
            } else {
                result = false;
            }
            return result;
        }

    }


    private static final class PropertyComparison<A> extends Condition {

        private final PropertyType<A> propertyType;
        private final CmpOperator<A> operator;
        private final A value;

        private PropertyComparison(PropertyType<A> propertyType, CmpOperator<A> operator, A value) {
            this.propertyType = propertyType;
            this.operator = operator;
            this.value = value;
        }

        @Override
        public boolean eval(ButtonEvent event) {
            A eventValue = propertyType.extractEventValue(event);
            return operator.test(eventValue, value);
        }

        @Override
        public String toString() {
            return String.format("PropertyComparison(%s, %s, %s)", propertyType, operator, value);
        }

        @Override
        public int hashCode() {
            return (1388436817 * propertyType.hashCode()) +
                    (1264266287 * operator.hashCode()) +
                    (1439960759 * value.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            boolean result;
            if (this == obj) {
                result = true;
            } else if (obj instanceof PropertyComparison) {
                PropertyComparison<?> that = (PropertyComparison<?>) obj;
                result = propertyType.equals(that.propertyType) &&
                        operator.equals(that.operator) &&
                        value.equals(that.value);
            } else {
                result = false;
            }
            return result;
        }

    }


    private static final class PropertyBetween<A extends Comparable<A>> extends Condition {

        private final PropertyType<A> propertyType;
        private final A minValue;
        private final A maxValue;

        private PropertyBetween(PropertyType<A> propertyType, A minValue, A maxValue) {
            this.propertyType = propertyType;
            this.minValue = minValue;
            this.maxValue = maxValue;
        }

        @Override
        public boolean eval(ButtonEvent event) {
            A eventValue = propertyType.extractEventValue(event);
            return (minValue.compareTo(eventValue) <= 0) && (eventValue.compareTo(maxValue) <= 0);
        }

        @Override
        public String toString() {
            return String.format("PropertyBetween(%s, %s, %s)", propertyType, minValue, maxValue);
        }

        @Override
        public int hashCode() {
            return (896049211 * propertyType.hashCode()) +
                    (2016690983 * minValue.hashCode()) +
                    (1620899261 * maxValue.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            boolean result;
            if (this == obj) {
                result = true;
            } else if (obj instanceof PropertyBetween) {
                PropertyBetween<?> that = (PropertyBetween<?>) obj;
                result = propertyType.equals(that.propertyType) &&
                        minValue.equals(that.minValue) &&
                        maxValue.equals(that.maxValue);
            } else {
                result = false;
            }
            return result;
        }

    }


    private static final class PropertyIn<A> extends Condition {

        private final PropertyType<A> propertyType;
        private final List<A> values;

        private PropertyIn(PropertyType<A> propertyType, List<A> values) {
            this.propertyType = propertyType;
            this.values = values;
        }

        @Override
        public boolean eval(ButtonEvent event) {
            A eventValue = propertyType.extractEventValue(event);
            return values.exists(eventValue::equals);
        }

        @Override
        public String toString() {
            return String.format("PropertyIn(%s, %s)", propertyType, values);
        }

        @Override
        public int hashCode() {
            return (1789482131 * propertyType.hashCode()) +
                    (34526447 * values.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            boolean result;
            if (this == obj) {
                result = true;
            } else if (obj instanceof PropertyIn) {
                PropertyIn<?> that = (PropertyIn<?>) obj;
                result = propertyType.equals(that.propertyType) && values.equals(that.values);
            } else {
                result = false;
            }
            return result;
        }

    }

}
