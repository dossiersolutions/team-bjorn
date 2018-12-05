package no.dossier.thatbuttonserver.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.Iterator;
import java.util.function.Function;

import static no.dossier.thatbuttonserver.util.List.cons;
import static no.dossier.thatbuttonserver.util.List.nil;
import static no.dossier.thatbuttonserver.util.Option.none;
import static no.dossier.thatbuttonserver.util.Result.fail;
import static no.dossier.thatbuttonserver.util.Result.failUnless;
import static no.dossier.thatbuttonserver.util.Result.ok;
import static no.dossier.thatbuttonserver.util.Result.okIf;

public final class JsonDecoder<A> {

    public static <A> JsonDecoder<A> success(A value) {
        return fromResult(ok(value));
    }

    public static <A> JsonDecoder<A> failure(String error) {
        return fromResult(fail(error));
    }

    public static <A> JsonDecoder<A> fromResult(Result<String, A> result) {
        return new JsonDecoder<>(ignored -> result);
    }

    public static JsonDecoder<Integer> integerDecoder() {
        return new JsonDecoder<>(jsonElement -> failUnless(
                jsonElement.isJsonPrimitive(),
                () -> {
                    JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
                    return failUnless(
                            jsonPrimitive.isNumber(),
                            () -> {
                                try {
                                    return ok(jsonPrimitive.getAsInt());
                                } catch (NumberFormatException ex) {
                                    return fail(String.format("Not an int: %s", jsonPrimitive));
                                }
                            },
                            () -> String.format("Not a JSON number: %s", jsonPrimitive));
                },
                () -> String.format("Not a JSON number: %s", jsonElement)));
    }

    public static JsonDecoder<String> stringDecoder() {
        return new JsonDecoder<>(jsonElement -> failUnless(
                jsonElement.isJsonPrimitive(),
                () -> {
                    JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
                    return okIf(
                            jsonPrimitive.isString(),
                            jsonPrimitive::getAsString,
                            () -> String.format("Not a JSON string: %s", jsonPrimitive));
                },
                () -> String.format("Not a JSON string: %s", jsonElement)));
    }

    public static JsonDecoder<Boolean> booleanDecoder() {
        return new JsonDecoder<>(jsonElement -> failUnless(
                jsonElement.isJsonPrimitive(),
                () -> {
                    JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
                    return okIf(
                            jsonPrimitive.isBoolean(),
                            jsonPrimitive::getAsBoolean,
                            () -> String.format("Not a JSON boolean: %s", jsonPrimitive));
                },
                () -> String.format("Not a JSON boolean: %s", jsonElement)));
    }

    public static <A> JsonDecoder<Option<A>> nullableDecoder(JsonDecoder<A> valueDecoder) {
        return new JsonDecoder<>(jsonElement -> jsonElement.isJsonNull() ?
                ok(none()) :
                valueDecoder.run(jsonElement).map(Option::some));
    }

    public static <A> JsonDecoder<List<A>> listDecoder(JsonDecoder<A> elementDecoder) {
        return new JsonDecoder<>(jsonElement -> failUnless(
                jsonElement.isJsonArray(),
                () -> {
                    JsonArray jsonArray = jsonElement.getAsJsonArray();
                    List<JsonElement> jsonElements = jsonArrayToList(jsonArray.iterator());
                    Result<String, List<A>> result = decodeElements(elementDecoder, jsonElements);
                    return result;
                },
                () -> String.format("Not a JSON array: %s", jsonElement)));
    }

    private static List<JsonElement> jsonArrayToList(Iterator<JsonElement> iterator) {
        return iterator.hasNext() ?
                cons(iterator.next(), jsonArrayToList(iterator)) :
                nil();
    }

    private static <A> Result<String, List<A>> decodeElements(
            JsonDecoder<A> elementDecoder,
            List<JsonElement> jsonElements) {

        return jsonElements.unwrap(
                (head, tail) -> {
                    Result<String, A> headResult = elementDecoder.run(head);
                    Result<String, List<A>> result = headResult.unwrap(
                            newHead -> {
                                Result<String, List<A>> tailResult = decodeElements(elementDecoder, tail);
                                return tailResult.map(newTail -> cons(newHead, newTail));
                            },
                            Result::fail);
                    return result;
                },
                () -> ok(nil()));
    }

    public static <A> JsonDecoder<A> fieldDecoder(String name, JsonDecoder<A> valueDecoder) {
        return new JsonDecoder<>(jsonElement -> failUnless(
                jsonElement.isJsonObject(),
                () -> {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    JsonElement value = jsonObject.get(name);
                    return (value != null) ?
                            valueDecoder.run(value).mapFail(msg -> String.format("%s: %s", name, msg)) :
                            fail(String.format("Property \"%s\" not found: %s", name, jsonObject));
                },
                () -> String.format("Not a JSON object: %s", jsonElement)));
    }

    public static <A> JsonDecoder<Option<A>> optionalFieldDecoder(String name, JsonDecoder<A> valueDecoder) {
        return new JsonDecoder<>(jsonElement -> failUnless(
                jsonElement.isJsonObject(),
                () -> {
                    JsonObject jsonObject = jsonElement.getAsJsonObject();
                    JsonElement value = jsonObject.get(name);
                    return (value != null) ?
                            valueDecoder.run(value).bimap(Option::some, msg -> String.format("%s: %s", name, msg)) :
                            ok(none());
                },
                () -> String.format("Not a JSON object: %s", jsonElement)));
    }

    public static <A> JsonDecoder<Option<A>> optionDecoder(JsonDecoder<A> valueDecoder) {
        return new JsonDecoder<>(jsonElement -> {
            Result<String, A> valueResult = valueDecoder.run(jsonElement);
            return ok(valueResult.unwrap(
                    Option::some,
                    ignored -> none()));
        });
    }

    public static <A, B, C> JsonDecoder<C> map2(
            JsonDecoder<A> decoder1,
            JsonDecoder<B> decoder2,
            Function<A, Function<B, C>> f) {

        return decoder1.flatMap(value1 -> decoder2.map(f.apply(value1)));
    }

    public static <A, B, C> JsonDecoder<C> flatMap2(
            JsonDecoder<A> decoder1,
            JsonDecoder<B> decoder2,
            Function<A, Function<B, JsonDecoder<C>>> f) {

        return decoder1.flatMap(value1 -> decoder2.flatMap(f.apply(value1)));
    }

    private final Function<JsonElement, Result<String, A>> decode;

    private JsonDecoder(Function<JsonElement, Result<String, A>> decode) {
        this.decode = decode;
    }

    public Result<String, A> run(JsonElement jsonElement) {
        Result<String, A> result = decode.apply(jsonElement);
        // System.out.format("Input: %s%n", jsonElement);
        // System.out.format("Result: %s%n", result);
        return result;
    }

    public <B> JsonDecoder<B> map(Function<A, B> f) {
        return new JsonDecoder<>(jsonElement -> run(jsonElement).map(f));
    }

    public JsonDecoder<A> mapFail(Function<String, String> f) {
        return new JsonDecoder<>(jsonElement -> run(jsonElement).mapFail(f));
    }

    public <B, C> JsonDecoder<C> map2(JsonDecoder<B> decoder2, Function<A, Function<B, C>> f) {
        return flatMap(value1 -> decoder2.map(f.apply(value1)));
    }

    public <B> JsonDecoder<B> flatMap(Function<A, JsonDecoder<B>> f) {
        return new JsonDecoder<>(jsonElement -> run(jsonElement).flatMap(value -> f.apply(value).run(jsonElement)));
    }

}
