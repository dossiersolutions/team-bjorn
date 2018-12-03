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
import no.dossier.buttonserver.util.Result;

import java.util.function.Function;

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
 * <action> ::= <shellAction> | <logEventAction> | <triggersAction>
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
 *
 * <shellAction> ::= { "action": "Shell", "command": <STRING> }
 *
 * <logEventAction> ::= { "action": "LogEvent" }
 *
 * "triggers" is optional, but if it is absent, then nothing will happen in this branch
 * <triggersAction> ::= { "action": "Triggers", "triggers": [ <trigger> ] }
 */
public final class JsonToConfigDecoder {

    private static final int DEFAULT_PORT = 38911;

    public static Result<String, Config> decode(JsonElement jsonElement) {
        return configDecoder().run(jsonElement);
    }

    private static JsonDecoder<Config> configDecoder() {

        JsonDecoder<String> versionDecoder = fieldDecoder("version", stringDecoder());

        return versionDecoder.flatMap(version -> {
            JsonDecoder<Config> configDecoder;
            if (version.equals("0.1")) {
                // Support missing "settings" property
                JsonDecoder<Settings> settingsDecoder =
                        optionalFieldDecoder("settings", settingsDecoder())
                                .map(settingsOption -> settingsOption.getOrElse(() -> new Settings(DEFAULT_PORT)));

                // Support missing "triggers" property
                JsonDecoder<List<Trigger>> triggersDecoder = optionalFieldDecoder("triggers", listDecoder(triggerDecoder()))
                        .map(triggersOption -> triggersOption.getOrElse(List::nil));

                configDecoder = JsonDecoder.map2(
                        settingsDecoder,
                        triggersDecoder,
                        settings -> triggers -> new Config(settings, triggers));
            } else {
                configDecoder = JsonDecoder.failure(String.format("Unsupported version %s", version));
            }
            return configDecoder;
        });
    }

    private static JsonDecoder<Settings> settingsDecoder() {
        // Support missing "port" property
        return optionalFieldDecoder("port", integerDecoder()).map(
                portOption -> {
                    int port = portOption.getOrElse(() -> DEFAULT_PORT);
                    return new Settings(port);
                });
    }

    private static JsonDecoder<Trigger> triggerDecoder() {
        // Support missing "conditions" property
        JsonDecoder<Condition> conditionDecoder =
                optionalFieldDecoder("condition", conditionDecoder())
                        .map(conditionOption -> conditionOption.getOrElse(Condition::always));

        // Support missing "actions" property
        JsonDecoder<List<Action>> actionsDecoder =
                optionalFieldDecoder("actions", listDecoder(actionDecoder()))
                        .map(actionsOption -> actionsOption.getOrElse(List::nil));

        return JsonDecoder.map2(
                conditionDecoder,
                actionsDecoder,
                conditions -> actions -> new Trigger(conditions, actions));
    }

    private static JsonDecoder<Condition> conditionDecoder() {
        JsonDecoder<String> operatorStrDecoder = fieldDecoder("operator", stringDecoder());

        return operatorStrDecoder.flatMap(operatorStr -> {
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
                case "!=":
                case "<":
                case "<=":
                case ">":
                case ">=":
                    decoder = comparisonConditionDecoder(operatorStr);
                    break;
                case "Between":
                    decoder = betweenConditionDecoder();
                    break;
                case "In":
                    decoder = inConditionDecoder();
                    break;
                default:
                    decoder = JsonDecoder.failure(String.format("Invalid condition operator %s", operatorStr));
            }
            return decoder;
        });
    }

    private static JsonDecoder<Condition> connectiveConditionDecoder(Function<List<Condition>, Condition> factory) {
        JsonDecoder<List<Condition>> conditionsDecoder = fieldDecoder("conditions", listDecoder(conditionDecoder()));

        return conditionsDecoder.map(factory);
    }

    private static JsonDecoder<Condition> negationConditionDecoder() {
        JsonDecoder<Condition> conditionDecoder = fieldDecoder("condition", conditionDecoder());

        return conditionDecoder.map(Condition::not);
    }

    private static JsonDecoder<Condition> comparisonConditionDecoder(String operatorStr) {
        return fieldDecoder("property", stringDecoder())
                .flatMap(propertyName -> propertyDecoder(propertyName, new PropertyDecoderFactory<Condition>() {

                    @Override
                    public JsonDecoder<Condition> getEventTypeDecoder() {
                        return nonOrdPropertyComparisonDecoder(PropertyType.EVENT_TYPE, eventTypeDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getButtonIdDecoder() {
                        return nonOrdPropertyComparisonDecoder(PropertyType.BUTTON_ID, buttonIdDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getButtonStateDecoder() {
                        return nonOrdPropertyComparisonDecoder(PropertyType.BUTTON_STATE, buttonStateDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getPotentiometerStateDecoder() {
                        return ordPropertyComparisonDecoder(
                                PropertyType.POTENTIOMETER_STATE,
                                potentiometerStateDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getPotentiometerStepDecoder() {
                        return ordPropertyComparisonDecoder(
                                PropertyType.POTENTIOMETER_STEP,
                                potentiometerStepDecoder());
                    }

                    private <A> JsonDecoder<Condition> nonOrdPropertyComparisonDecoder(
                            PropertyType<A> propertyType,
                            JsonDecoder<A> valueDecoder) {

                        JsonDecoder<CmpOperator<A>> operatorDecoder;
                        switch (operatorStr) {
                            case "==":
                                operatorDecoder = JsonDecoder.success(CmpOperator.equals());
                                break;
                            case "!=":
                                operatorDecoder = JsonDecoder.success(CmpOperator.notEquals());
                                break;
                            case "<":
                            case "<=":
                            case ">":
                            case ">=":
                                operatorDecoder = JsonDecoder.failure(String.format(
                                        "Operator %s not applicable for non-ordered property",
                                        operatorStr));
                                break;
                            default:
                                operatorDecoder = JsonDecoder.failure(String.format("Invalid operator: %s", operatorStr));
                        }

                        return JsonDecoder.map2(
                                operatorDecoder,
                                fieldDecoder("value", valueDecoder),
                                operator -> value -> propertyComparison(propertyType, operator, value));
                    }

                    private <A extends Comparable<A>> JsonDecoder<Condition> ordPropertyComparisonDecoder(
                            PropertyType<A> propertyType,
                            JsonDecoder<A> valueDecoder) {

                        JsonDecoder<CmpOperator<A>> operatorDecoder;
                        switch (operatorStr) {
                            case "==":
                                operatorDecoder = JsonDecoder.success(CmpOperator.equals());
                                break;
                            case "!=":
                                operatorDecoder = JsonDecoder.success(CmpOperator.notEquals());
                                break;
                            case "<":
                                operatorDecoder = JsonDecoder.success(CmpOperator.lessThan());
                                break;
                            case "<=":
                                operatorDecoder = JsonDecoder.success(CmpOperator.lessOrEqual());
                                break;
                            case ">":
                                operatorDecoder = JsonDecoder.success(CmpOperator.greaterThan());
                                break;
                            case ">=":
                                operatorDecoder = JsonDecoder.success(CmpOperator.greaterOrEqual());
                                break;
                            default:
                                operatorDecoder = JsonDecoder.failure(String.format(
                                        "Invalid operator: %s",
                                        operatorStr));
                        }

                        JsonDecoder<A> valueFieldDecoder = fieldDecoder("value", valueDecoder);

                        return JsonDecoder.map2(
                                operatorDecoder,
                                valueFieldDecoder,
                                operator -> value -> propertyComparison(propertyType, operator, value));
                    }

                }));
    }

    private static JsonDecoder<Condition> betweenConditionDecoder() {
        return fieldDecoder("property", stringDecoder())
                .flatMap(propertyName -> propertyDecoder(propertyName, new PropertyDecoderFactory<Condition>() {

                    @Override
                    public JsonDecoder<Condition> getEventTypeDecoder() {
                        return nonOrdBetweenConditionDecoder();
                    }

                    @Override
                    public JsonDecoder<Condition> getButtonIdDecoder() {
                        return nonOrdBetweenConditionDecoder();
                    }

                    @Override
                    public JsonDecoder<Condition> getButtonStateDecoder() {
                        return nonOrdBetweenConditionDecoder();
                    }

                    @Override
                    public JsonDecoder<Condition> getPotentiometerStateDecoder() {
                        return ordBetweenConditionDecoder(
                                PropertyType.POTENTIOMETER_STATE,
                                potentiometerStateDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getPotentiometerStepDecoder() {
                        return ordBetweenConditionDecoder(PropertyType.POTENTIOMETER_STEP, potentiometerStepDecoder());
                    }

                    private JsonDecoder<Condition> nonOrdBetweenConditionDecoder() {
                        return JsonDecoder.failure("Operator Between not applicable for non-ordered property");
                    }

                    private <A extends Comparable<A>> JsonDecoder<Condition> ordBetweenConditionDecoder(
                            PropertyType<A> propertyType,
                            JsonDecoder<A> valueDecoder) {

                        JsonDecoder<A> minValueDecoder = fieldDecoder("minValue", valueDecoder);
                        JsonDecoder<A> maxValueDecoder = fieldDecoder("maxValue", valueDecoder);

                        return flatMap2(
                                minValueDecoder,
                                maxValueDecoder,
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
        return fieldDecoder("property", stringDecoder())
                .flatMap(propertyName -> propertyDecoder(propertyName, new PropertyDecoderFactory<Condition>() {

                    @Override
                    public JsonDecoder<Condition> getEventTypeDecoder() {
                        return inConditionDecoder(PropertyType.EVENT_TYPE, eventTypeDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getButtonIdDecoder() {
                        return inConditionDecoder(PropertyType.BUTTON_ID, buttonIdDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getButtonStateDecoder() {
                        return inConditionDecoder(PropertyType.BUTTON_STATE, buttonStateDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getPotentiometerStateDecoder() {
                        return inConditionDecoder(PropertyType.POTENTIOMETER_STATE, potentiometerStateDecoder());
                    }

                    @Override
                    public JsonDecoder<Condition> getPotentiometerStepDecoder() {
                        return inConditionDecoder(PropertyType.POTENTIOMETER_STEP, potentiometerStepDecoder());
                    }

                    private <A> JsonDecoder<Condition> inConditionDecoder(
                            PropertyType<A> propertyType,
                            JsonDecoder<A> valueDecoder) {

                        JsonDecoder<List<A>> valuesDecoder = fieldDecoder("values", listDecoder(valueDecoder));

                        return valuesDecoder.map(values -> propertyIn(propertyType, values));
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

    private static <A> JsonDecoder<A> propertyDecoder(String propertyName, PropertyDecoderFactory<A> handler) {
        JsonDecoder<A> decoder;
        switch (propertyName) {
            case "EventType":
                decoder = handler.getEventTypeDecoder();
                break;
            case "ButtonId":
                decoder = handler.getButtonIdDecoder();
                break;
            case "ButtonState":
                decoder = handler.getButtonStateDecoder();
                break;
            case "PotentiometerState":
                decoder = handler.getPotentiometerStateDecoder();
                break;
            case "PotentiometerStep":
                decoder = handler.getPotentiometerStepDecoder();
                break;
            default:
                decoder = JsonDecoder.failure(String.format("Invalid property: %s", propertyName));
        }
        return decoder;
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
                    decoder = JsonDecoder.failure(String.format("Invalid eventType: %s", eventTypeName));
            }
            return decoder;
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
                    decoder = JsonDecoder.failure(String.format("Invalid buttonState: %s", buttonStateName));
            }
            return decoder;
        });
    }

    private static JsonDecoder<PotentiometerState> potentiometerStateDecoder() {
        return integerDecoder().flatMap(value -> JsonDecoder.fromResult(potentiometerStateResult(value)));
    }

    private static JsonDecoder<PotentiometerStep> potentiometerStepDecoder() {
        return integerDecoder().flatMap(value -> JsonDecoder.fromResult(potentiometerStepResult(value)));
    }

    private static JsonDecoder<Action> actionDecoder() {
        JsonDecoder<String> actionTypeDecoder = fieldDecoder("action", stringDecoder());

        return actionTypeDecoder.flatMap(actionType -> {
            JsonDecoder<Action> decoder;
            switch (actionType) {
                case "Shell":
                    JsonDecoder<String> commandTemplateDecoder = fieldDecoder("command", stringDecoder());
                    decoder = commandTemplateDecoder.map(Action::shellAction);
                    break;
                case "ForwardMessage":
                    decoder = JsonDecoder.map2(
                            fieldDecoder("hostName", stringDecoder()),
                            fieldDecoder("port", integerDecoder()),
                            hostName -> port -> Action.forwardMessageAction(hostName, port));
                    break;
                case "LogEvent":
                    decoder = JsonDecoder.success(Action.logEventAction());
                    break;
                case "Triggers":
                    JsonDecoder<List<Trigger>> triggersDecoder = fieldDecoder("triggers", listDecoder(triggerDecoder()));
                    decoder = triggersDecoder.map(Action::triggersAction);
                    break;
                default:
                    decoder = JsonDecoder.failure(String.format("Invalid action: %s", actionType));
            }
            return decoder;
        });
    }

    private JsonToConfigDecoder() {
    }

}
