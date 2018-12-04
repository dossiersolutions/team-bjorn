package no.dossier.buttonserver;

import com.google.gson.JsonElement;
import no.dossier.buttonserver.types.Action;
import no.dossier.buttonserver.types.ButtonId;
import no.dossier.buttonserver.types.ButtonState;
import no.dossier.buttonserver.types.CmpOperator;
import no.dossier.buttonserver.types.Condition;
import no.dossier.buttonserver.types.Config;
import no.dossier.buttonserver.types.EventType;
import no.dossier.buttonserver.types.PotentiometerState;
import no.dossier.buttonserver.types.PotentiometerStep;
import no.dossier.buttonserver.types.PropertyType;
import no.dossier.buttonserver.types.Settings;
import no.dossier.buttonserver.types.Trigger;
import no.dossier.buttonserver.util.JsonDecoder;
import no.dossier.buttonserver.util.List;
import no.dossier.buttonserver.util.Option;
import no.dossier.buttonserver.util.Result;

import java.util.function.Function;

import static no.dossier.buttonserver.types.Action.forwardMessageAction;
import static no.dossier.buttonserver.types.Action.logEventAction;
import static no.dossier.buttonserver.types.ButtonId.buttonIdResult;
import static no.dossier.buttonserver.types.Condition.always;
import static no.dossier.buttonserver.types.Condition.never;
import static no.dossier.buttonserver.types.Condition.propertyBetweenOption;
import static no.dossier.buttonserver.types.Condition.propertyComparison;
import static no.dossier.buttonserver.types.Condition.propertyIn;
import static no.dossier.buttonserver.types.PotentiometerState.potentiometerStateResult;
import static no.dossier.buttonserver.types.PotentiometerStep.potentiometerStepResult;
import static no.dossier.buttonserver.util.JsonDecoder.fieldDecoder;
import static no.dossier.buttonserver.util.JsonDecoder.flatMap2;
import static no.dossier.buttonserver.util.JsonDecoder.integerDecoder;
import static no.dossier.buttonserver.util.JsonDecoder.listDecoder;
import static no.dossier.buttonserver.util.JsonDecoder.optionalFieldDecoder;
import static no.dossier.buttonserver.util.JsonDecoder.stringDecoder;
import static no.dossier.buttonserver.util.Result.optionResult;

/*
 * Pseudo-BNF Grammar:
 *
 * ("Pseudo" in the sense that curly brackets are used for JSON objects, not Extended BNF repetition;
 * and that square brackets are used for JSON arrays, not Extended BNF optionality)
 *
 * Property names are all in camelCase
 * Enum values are all in PascalCase (or operator symbols)
 *
 * Both "settings" and "triggers" are optional, but if "triggers" is absent, then nothing will happen
 * <config> ::= { "version": "0.1", "settings" : <settings>, "triggers" : [ <trigger> ] }
 *
 * <settings> ::= {}
 *
 * Both "condition" and "actions" are optional.
 *   - If "condition" is absent, it is interpreted as if there were a "condition": "Always" property there
 *   - If "actions" is absent, then nothing will happen
 * <trigger> ::= { "condition" : <condition>, "actions" : [ <action> ] }
 *
 * <condition> ::= <literal> | <connective> | <negation> | <comparison> | <between> | <in>
 * <literal> ::= { "operator": <literalOperator> }
 * <literalOperator> ::= "Always" | "Never"
 * <connective> ::= { "operator": <connectiveOperator>, "conditions": [ <condition> ] }
 * <connectiveOperator> ::= "And" | "Or"
 * <negation> ::= { "operator": "Not", "condition": <condition> }
 *
 * Comparison is type-safe, i.e. <propertyValue> has to be consistent with <propertyType>
 * <comparison> ::= { "property": <propertyType>, "operator": <cmpOperator>, "value": <propertyValue> }
 *
 * <cmpOperator> ::= "==" | "!=" | "<" | "<=" | ">" | ">="
 *
 * Between is type-safe, i.e. <propertyType> has to be "PotentiometerState" or "PotentiometerStep" (ButtonId is not treated as an ordered value)
 * <between> ::= { "property": <propertyType>, "operator": "Between", "minValue": <INTEGER>, "maxValue": <INTEGER> }
 *
 * In is type-safe, i.e. <propertyValue> has to be consistent with <propertyType>
 * <in> ::= { "property": <propertyType>, "operator": "In", "values": [ <propertyValue> ] }
 *
 * <propertyType> ::= "EventType" | "ButtonId" | "ButtonState" | "PotentiometerState" | "PotentiometerStep"
 *
 * Note that <buttonState> in the config file is a string, the buttonserver will not accept an integer 0 or 1.
 * <propertyValue> ::= <eventType> | <buttonState> | <INTEGER>
 * <eventType> ::= "ButtonDown" | "ButtonUp" | "Click" | "DoubleClick" | "PotentiometerChange"
 * <buttonState> ::= "Up" | "Down"
 *
 * <eventAction> ::= <shellAction> | <forwardMessageAction> | <logEventAction> | <triggersAction>
 *
 * Parameters in the "command" string referencing event attributes are prefixed by a $ sign. To insert a $ sign literally, use $$.
 * Expansion is performed also inside string literals.
 * Possible parameter references are:
 *   - $EventType (Expanded to one of: ButtonDown, ButtonUp, Click, DoubleClick, PotentiometerChange)
 *   - $Timestamp (Expanded to format 2018-11-28T13:59:10.123Z - Note: Zulu time zone, and optional milliseconds part)
 *   - $ButtonId (Expanded to 0..65535)
 *   - $ButtonState (Expanded to one of: Up, Down)
 *   - $PotentiometerState (Expanded to 0..1023)
 *   - $PotentiometerStep (Expanded to 0..8)
 * <shellAction> ::= { "action": "Shell", "command": <STRING> }
 *
 * <forwardMessageAction> ::= { "action": "ForwardMessage", "hostName": <STRING>, "port": <INTEGER> }
 * <logEventAction> ::= { "action": "LogEvent" }
 *
 * "triggers" is optional, but if it is absent, then nothing will happen in this branch
 * <triggersAction> ::= { "action": "Triggers", "triggers": [ <eventTrigger> ] }
 */
public final class JsonToConfigDecoder {

    private static final int DEFAULT_PORT = 38911;

    public static Result<String, Config> decode(JsonElement jsonElement) {
        return configDecoder().run(jsonElement);
    }

    private static JsonDecoder<Config> configDecoder() {
        JsonDecoder<String> versionFieldDecoder = fieldDecoder("version", stringDecoder());

        return versionFieldDecoder.flatMap(version -> {
            JsonDecoder<Config> configDecoder;
            if (version.equals("0.1")) {
                // Support missing "settings" property
                JsonDecoder<Option<Settings>> settingsFieldDecoder =
                        optionalFieldDecoder("settings", settingsDecoder());

                JsonDecoder<Settings> settingsDecoder = settingsFieldDecoder
                        .map(settingsOption -> settingsOption.getOrElse(() -> new Settings(DEFAULT_PORT)));

                // Support missing "triggers" property
                JsonDecoder<Option<List<Trigger>>> eventTriggersFieldDecoder =
                        optionalFieldDecoder("triggers", listDecoder(triggerDecoder()));

                JsonDecoder<List<Trigger>> eventTriggersDecoder = eventTriggersFieldDecoder
                        .map(triggersOption -> triggersOption.getOrElse(List::nil));

                configDecoder = JsonDecoder.map2(
                        settingsDecoder,
                        eventTriggersDecoder,
                        settings -> triggers -> new Config(settings, triggers));
            } else {
                configDecoder = JsonDecoder.failure(String.format("Unsupported version %s", version));
            }
            return configDecoder;
        });
    }

    private static JsonDecoder<Settings> settingsDecoder() {
        // Support missing "port" property
        JsonDecoder<Option<Integer>> portFieldDecoder = optionalFieldDecoder("port", integerDecoder());

        return portFieldDecoder.map(portOption -> {
            int port = portOption.getOrElse(() -> DEFAULT_PORT);
            return new Settings(port);
        });
    }

    private static JsonDecoder<Trigger> triggerDecoder() {
        // Support missing "conditions" property
        JsonDecoder<Option<Condition>> conditionFieldDecoder = optionalFieldDecoder("condition", conditionDecoder());

        JsonDecoder<Condition> conditionDecoder = conditionFieldDecoder
                .map(conditionOption -> conditionOption.getOrElse(Condition::always));

        // Support missing "actions" property
        JsonDecoder<Option<List<Action>>> actionsFieldDecoder =
                optionalFieldDecoder("actions", listDecoder(actionDecoder()));

        JsonDecoder<List<Action>> actionsDecoder = actionsFieldDecoder
                .map(actionsOption -> actionsOption.getOrElse(List::nil));

        return JsonDecoder.map2(
                conditionDecoder,
                actionsDecoder,
                conditions -> actions -> new Trigger(conditions, actions));
    }

    private static JsonDecoder<Condition> conditionDecoder() {
        JsonDecoder<String> operatorStrFieldDecoder = fieldDecoder("operator", stringDecoder());

        return operatorStrFieldDecoder.flatMap(operatorStr -> {
            JsonDecoder<Condition> decoder;
            switch (operatorStr) {
                case "Always":
                    decoder = JsonDecoder.success(always());
                    break;
                case "Never":
                    decoder = JsonDecoder.success(never());
                    break;
                case "And":
                    decoder = connectiveConditionDecoder(Condition::and);
                    break;
                case "Or":
                    decoder = connectiveConditionDecoder(Condition::or);
                    break;
                case "Not":
                    decoder = negationConditionDecoder();
                    break;
                case "==":
                    decoder = comparisonConditionDecoder(new ComparisonConditionFactory() {
                        @Override
                        public <A> Condition getCondition(PropertyType<A> propertyType, A value) {
                            return propertyComparison(propertyType, CmpOperator.equals(), value);
                        }
                    });
                    break;
                case "!=":
                    decoder = comparisonConditionDecoder(new ComparisonConditionFactory() {
                        @Override
                        public <A> Condition getCondition(PropertyType<A> propertyType, A value) {
                            return propertyComparison(propertyType, CmpOperator.notEquals(), value);
                        }
                    });
                    break;
                case "<":
                    decoder = ordComparisonConditionDecoder(new OrdComparisonConditionFactory() {
                        @Override
                        public <A extends Comparable<A>> Condition getCondition(PropertyType<A> propertyType, A value) {
                            return propertyComparison(propertyType, CmpOperator.lessThan(), value);
                        }
                    });
                    break;
                case "<=":
                    decoder = ordComparisonConditionDecoder(new OrdComparisonConditionFactory() {
                        @Override
                        public <A extends Comparable<A>> Condition getCondition(PropertyType<A> propertyType, A value) {
                            return propertyComparison(propertyType, CmpOperator.lessOrEqual(), value);
                        }
                    });
                    break;
                case ">":
                    decoder = ordComparisonConditionDecoder(new OrdComparisonConditionFactory() {
                        @Override
                        public <A extends Comparable<A>> Condition getCondition(PropertyType<A> propertyType, A value) {
                            return propertyComparison(propertyType, CmpOperator.greaterThan(), value);
                        }
                    });
                    break;
                case ">=":
                    decoder = ordComparisonConditionDecoder(new OrdComparisonConditionFactory() {
                        @Override
                        public <A extends Comparable<A>> Condition getCondition(PropertyType<A> propertyType, A value) {
                            return propertyComparison(propertyType, CmpOperator.greaterOrEqual(), value);
                        }
                    });
                    break;
                case "Between":
                    decoder = betweenConditionDecoder();
                    break;
                case "In":
                    decoder = inConditionDecoder();
                    break;
                default:
                    decoder = JsonDecoder.failure("Invalid condition operator");
            }
            return decoder.mapFail(msg -> String.format("%s: %s", operatorStr, msg));
        });
    }

    private static JsonDecoder<Condition> connectiveConditionDecoder(Function<List<Condition>, Condition> factory) {
        JsonDecoder<List<Condition>> conditionsFieldDecoder =
                fieldDecoder("conditions", listDecoder(conditionDecoder()));

        return conditionsFieldDecoder.map(factory);
    }

    private static JsonDecoder<Condition> negationConditionDecoder() {
        JsonDecoder<Condition> conditionFieldDecoder = fieldDecoder("condition", conditionDecoder());

        return conditionFieldDecoder.map(Condition::not);
    }

    private static interface ComparisonConditionFactory {

        public <A> Condition getCondition(PropertyType<A> propertyType, A value);

    }

    private static JsonDecoder<Condition> comparisonConditionDecoder(ComparisonConditionFactory conditionFactory) {
        JsonDecoder<String> propertyNameFieldDecoder = fieldDecoder("property", stringDecoder());

        return propertyNameFieldDecoder
                .flatMap(propertyName -> propertyDecoder(propertyName, new PropertyDecoderFactory<Condition>() {

                    @Override
                    public JsonDecoder<Condition> getEventTypeDecoder() {
                        return conditionDecoder(PropertyType.EVENT_TYPE, eventTypeDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getButtonIdDecoder() {
                        return conditionDecoder(PropertyType.BUTTON_ID, buttonIdDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getButtonStateDecoder() {
                        return conditionDecoder(PropertyType.BUTTON_STATE, buttonStateDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getPotentiometerStateDecoder() {
                        return conditionDecoder(PropertyType.POTENTIOMETER_STATE, potentiometerStateDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getPotentiometerStepDecoder() {
                        return conditionDecoder(PropertyType.POTENTIOMETER_STEP, potentiometerStepDecoder());
                    }

                    private <A> JsonDecoder<Condition> conditionDecoder(
                            PropertyType<A> propertyType,
                            JsonDecoder<A> valueDecoder) {

                        JsonDecoder<A> valueFieldDecoder = fieldDecoder("value", valueDecoder);
                        return valueFieldDecoder.map(value -> conditionFactory.getCondition(propertyType, value));
                    }

                }));
    }

    private static interface OrdComparisonConditionFactory {

        public <A extends Comparable<A>> Condition getCondition(PropertyType<A> propertyType, A value);

    }

    private static JsonDecoder<Condition> ordComparisonConditionDecoder(
            OrdComparisonConditionFactory conditionFactory) {

        JsonDecoder<String> propertyNameFieldDecoder = fieldDecoder("property", stringDecoder());

        return propertyNameFieldDecoder
                .flatMap(propertyName -> propertyDecoder(propertyName, new PropertyDecoderFactory<Condition>() {

                    @Override
                    public JsonDecoder<Condition> getEventTypeDecoder() {
                        return JsonDecoder.failure("Not an ordered property");
                    }

                    @Override
                    public JsonDecoder<Condition> getButtonIdDecoder() {
                        return JsonDecoder.failure("Not an ordered property");
                    }

                    @Override
                    public JsonDecoder<Condition> getButtonStateDecoder() {
                        return JsonDecoder.failure("Not an ordered property");
                    }

                    @Override
                    public JsonDecoder<Condition> getPotentiometerStateDecoder() {
                        return conditionDecoder(PropertyType.POTENTIOMETER_STATE, potentiometerStateDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getPotentiometerStepDecoder() {
                        return conditionDecoder(PropertyType.POTENTIOMETER_STEP, potentiometerStepDecoder());
                    }

                    private <A extends Comparable<A>> JsonDecoder<Condition> conditionDecoder(
                            PropertyType<A> propertyType,
                            JsonDecoder<A> valueDecoder) {

                        JsonDecoder<A> valueFieldDecoder = fieldDecoder("value", valueDecoder);
                        return valueFieldDecoder.map(value -> conditionFactory.getCondition(propertyType, value));
                    }

                }));
    }

    private static JsonDecoder<Condition> betweenConditionDecoder() {
        JsonDecoder<String> propertyNameFieldDecoder = fieldDecoder("property", stringDecoder());

        return propertyNameFieldDecoder
                .flatMap(propertyName -> propertyDecoder(propertyName, new PropertyDecoderFactory<Condition>() {

                    @Override
                    public JsonDecoder<Condition> getEventTypeDecoder() {
                        return JsonDecoder.failure("Not an ordered property");
                    }

                    @Override
                    public JsonDecoder<Condition> getButtonIdDecoder() {
                        return JsonDecoder.failure("Not an ordered property");
                    }

                    @Override
                    public JsonDecoder<Condition> getButtonStateDecoder() {
                        return JsonDecoder.failure("Not an ordered property");
                    }

                    @Override
                    public JsonDecoder<Condition> getPotentiometerStateDecoder() {
                        return conditionDecoder(
                                PropertyType.POTENTIOMETER_STATE,
                                potentiometerStateDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getPotentiometerStepDecoder() {
                        return conditionDecoder(PropertyType.POTENTIOMETER_STEP, potentiometerStepDecoder());
                    }

                    private <A extends Comparable<A>> JsonDecoder<Condition> conditionDecoder(
                            PropertyType<A> propertyType,
                            JsonDecoder<A> valueDecoder) {

                        JsonDecoder<A> minValueFieldDecoder = fieldDecoder("minValue", valueDecoder);
                        JsonDecoder<A> maxValueFieldDecoder = fieldDecoder("maxValue", valueDecoder);

                        return flatMap2(
                                minValueFieldDecoder,
                                maxValueFieldDecoder,
                                minValue -> maxValue -> {
                                    Result<String, Condition> betweenConditionResult = optionResult(
                                            propertyBetweenOption(propertyType, minValue, maxValue),
                                            () -> "minValue must be less than or equal to maxValue");

                                    return JsonDecoder.fromResult(betweenConditionResult);
                                });
                    }

                }));
    }

    private static JsonDecoder<Condition> inConditionDecoder() {
        JsonDecoder<String> propertyNameFieldDecoder = fieldDecoder("property", stringDecoder());

        return propertyNameFieldDecoder
                .flatMap(propertyName -> propertyDecoder(propertyName, new PropertyDecoderFactory<Condition>() {

                    @Override
                    public JsonDecoder<Condition> getEventTypeDecoder() {
                        return conditionDecoder(PropertyType.EVENT_TYPE, eventTypeDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getButtonIdDecoder() {
                        return conditionDecoder(PropertyType.BUTTON_ID, buttonIdDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getButtonStateDecoder() {
                        return conditionDecoder(PropertyType.BUTTON_STATE, buttonStateDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getPotentiometerStateDecoder() {
                        return conditionDecoder(PropertyType.POTENTIOMETER_STATE, potentiometerStateDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getPotentiometerStepDecoder() {
                        return conditionDecoder(PropertyType.POTENTIOMETER_STEP, potentiometerStepDecoder());
                    }

                    private <A> JsonDecoder<Condition> conditionDecoder(
                            PropertyType<A> propertyType,
                            JsonDecoder<A> valueDecoder) {

                        JsonDecoder<List<A>> valuesFieldDecoder = fieldDecoder("values", listDecoder(valueDecoder));
                        return valuesFieldDecoder.map(values -> propertyIn(propertyType, values));
                    }

                }));
    }

    private static interface PropertyDecoderFactory<A> {

        public JsonDecoder<A> getEventTypeDecoder();

        public JsonDecoder<A> getButtonIdDecoder();

        public JsonDecoder<A> getButtonStateDecoder();

        public JsonDecoder<A> getPotentiometerStateDecoder();

        public JsonDecoder<A> getPotentiometerStepDecoder();

    }

    private static <A> JsonDecoder<A> propertyDecoder(String propertyName, PropertyDecoderFactory<A> decoderFactory) {
        JsonDecoder<A> decoder;
        switch (propertyName) {
            case "EventType":
                decoder = decoderFactory.getEventTypeDecoder();
                break;
            case "ButtonId":
                decoder = decoderFactory.getButtonIdDecoder();
                break;
            case "ButtonState":
                decoder = decoderFactory.getButtonStateDecoder();
                break;
            case "PotentiometerState":
                decoder = decoderFactory
                        .getPotentiometerStateDecoder();
                break;
            case "PotentiometerStep":
                decoder = decoderFactory.getPotentiometerStepDecoder();
                break;
            default:
                decoder = JsonDecoder.failure("Invalid property");
        }
        return decoder.mapFail(msg -> String.format("%s: %s", propertyName, msg));
    }

    private static JsonDecoder<EventType> eventTypeDecoder() {
        return stringDecoder().flatMap(eventTypeName -> {
            JsonDecoder<EventType> decoder;
            switch (eventTypeName) {
                case "Init":
                    decoder = JsonDecoder.success(EventType.INIT);
                    break;
                case "ButtonDown":
                    decoder = JsonDecoder.success(EventType.BUTTON_DOWN);
                    break;
                case "ButtonUp":
                    decoder = JsonDecoder.success(EventType.BUTTON_UP);
                    break;
                case "Click":
                    decoder = JsonDecoder.success(EventType.CLICK);
                    break;
                case "DoubleClick":
                    decoder = JsonDecoder.success(EventType.DOUBLE_CLICK);
                    break;
                case "PotentiometerState":
                    decoder = JsonDecoder.success(EventType.POTENTIOMETER_STATE_CHANGE);
                    break;
                case "PotentiometerStep":
                    decoder = JsonDecoder.success(EventType.POTENTIOMETER_STEP_CHANGE);
                    break;
                default:
                    decoder = JsonDecoder.failure("Invalid eventType");
            }
            return decoder.mapFail(msg -> String.format("%s: %s", eventTypeName, msg));
        });
    }

    private static JsonDecoder<ButtonId> buttonIdDecoder() {
        return integerDecoder().flatMap(value -> JsonDecoder.fromResult(buttonIdResult(value)));
    }

    private static JsonDecoder<ButtonState> buttonStateDecoder() {
        return stringDecoder().flatMap(buttonStateName -> {
            JsonDecoder<ButtonState> decoder;
            switch (buttonStateName) {
                case "Up":
                    decoder = JsonDecoder.success(ButtonState.BUTTON_UP);
                    break;
                case "Down":
                    decoder = JsonDecoder.success(ButtonState.BUTTON_DOWN);
                    break;
                default:
                    decoder = JsonDecoder.failure("Invalid buttonState");
            }
            return decoder.mapFail(msg -> String.format("%s: %s", buttonStateName, msg));
        });
    }

    private static JsonDecoder<PotentiometerState> potentiometerStateDecoder() {
        return integerDecoder().flatMap(value -> JsonDecoder.fromResult(potentiometerStateResult(value)));
    }

    private static JsonDecoder<PotentiometerStep> potentiometerStepDecoder() {
        return integerDecoder().flatMap(value -> JsonDecoder.fromResult(potentiometerStepResult(value)));
    }

    private static JsonDecoder<Action> actionDecoder() {
        JsonDecoder<String> actionTypeFieldDecoder = fieldDecoder("action", stringDecoder());

        return actionTypeFieldDecoder.flatMap(actionType -> {
            JsonDecoder<Action> decoder;
            switch (actionType) {
                case "Shell":
                    decoder = shellActionDecoder();
                    break;
                case "ForwardMessage":
                    decoder = forwardMessageActionDecoder();
                    break;
                case "LogEvent":
                    decoder = logEventActionDecoder();
                    break;
                case "Triggers":
                    decoder = triggersActionDecoder();
                    break;
                default:
                    decoder = JsonDecoder.failure("Invalid action");
            }
            return decoder.mapFail(msg -> String.format("action: %s: %s", actionType, msg));
        });
    }

    private static JsonDecoder<Action> shellActionDecoder() {
        JsonDecoder<String> commandTemplateFieldDecoder = fieldDecoder("command", stringDecoder());

        return commandTemplateFieldDecoder.map(Action::shellAction);
    }

    private static JsonDecoder<Action> forwardMessageActionDecoder() {
        JsonDecoder<String> hostNameFieldDecoder = fieldDecoder("hostName", stringDecoder());

        JsonDecoder<Integer> portFieldDecoder = fieldDecoder("port", integerDecoder());

        return JsonDecoder.map2(
                hostNameFieldDecoder,
                portFieldDecoder,
                hostName -> port -> forwardMessageAction(hostName, port));
    }

    private static JsonDecoder<Action> logEventActionDecoder() {
        return JsonDecoder.success(logEventAction());
    }

    private static JsonDecoder<Action> triggersActionDecoder() {
        JsonDecoder<List<Trigger>> triggersFieldDecoder =
                fieldDecoder("triggers", listDecoder(triggerDecoder()));

        return triggersFieldDecoder.map(Action::triggersAction);
    }

    private JsonToConfigDecoder() {
    }

}
