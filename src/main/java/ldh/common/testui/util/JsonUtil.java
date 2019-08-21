package ldh.common.testui.util;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.lang.reflect.Type;

/**
 * Created by ldh on 2018/3/19.
 */
public class JsonUtil {

    public static String toJson(Object object) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .create();
        return gson.toJson(object);
    }

    public static String toJsonExpose(Object object) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .create();
        return gson.toJson(object);
    }

    public static String parseJson(String str) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .setLenient()
                .disableHtmlEscaping()
                .setPrettyPrinting()
                .create();
        JsonReader reader = new JsonReader(new StringReader(str));
        reader.setLenient(true);
        JsonParser jsonPar = new JsonParser();
        JsonElement jsonEl = jsonPar.parse(reader);
        String prettyJson = gson.toJson(jsonEl);
        return prettyJson;
    }

    public static String toSimpleJson(Object object) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
//                .excludeFieldsWithoutExposeAnnotation()
                .create();
        return gson.toJson(object);
    }

    public static <T>T toObject(String json, Class<T> clazz) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
                    @Override
                    public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
                        if (src == src.longValue())
                            return new JsonPrimitive(src.longValue());
                        return new JsonPrimitive(src);
                    }
                })
                .create();
        return gson.fromJson(json, clazz);
    }

    public static <T>T toObjectExpose(String json, Class<T> clazz) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
                    @Override
                    public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
                        if (src == src.longValue())
                            return new JsonPrimitive(src.longValue());
                        return new JsonPrimitive(src);
                    }
                })
                .create();
        return gson.fromJson(json, clazz);
    }

    public static <T>T toObjectExpose(String json, Type tType) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Double.class, new JsonSerializer<Double>() {
                    @Override
                    public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
                        if (src == src.longValue())
                            return new JsonPrimitive(src.longValue());
                        return new JsonPrimitive(src);
                    }
                })
                .create();
        return gson.fromJson(json, tType);
    }

    public static <T>T toObject(String json, Type type) {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .create();
        return gson.fromJson(json, type);
    }

    public static String getElementFromJson(String json, String key) {
        JsonObject jsonObject = new JsonParser().parse(json).getAsJsonObject();
        return jsonObject.get(key).toString();
    }
}
