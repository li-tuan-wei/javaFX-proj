package ldh.common.testui.vo;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;
import ldh.common.testui.util.JsonUtil;

import java.util.Stack;

/**
 * Created by ldh on 2019/1/24.
 */
public class JsonPathHelp {

    private String json;
    private String jsonPath;
    private boolean isParse = false;

    public static final String TO_JSON = "Json.toJson(";

    public JsonPathHelp(String json, String jsonPath, boolean isParse) {
        this.json = json;
        this.jsonPath = jsonPath;
        this.isParse = isParse;
    }

    public JsonPathHelp(String json, String jsonPath) {
        this(json, jsonPath, false);
    }

    public <T>T jsonValue(Class<T> clazz) {
        if (isParse) {
            String newJson = JsonUtil.parseJson(json);
            JsonPathHelp newJsonPathHelp = new JsonPathHelp(newJson, jsonPath, false);
            return newJsonPathHelp.jsonValue(clazz);
        }
        int idx = jsonPath.indexOf(TO_JSON);
        String newJson = json;
        String newJsonPath = jsonPath;
        if (idx >= 0) {
            int idx2 = getClose(jsonPath, idx);
            if (idx2 == -1) {
                throw new RuntimeException("json格式错误!");
            }
            String newJsonPath2 = jsonPath.substring(idx + TO_JSON.length(), idx2);
            newJsonPath = jsonPath.substring(idx2+2);
            JsonPathHelp newJsonPathHelp = new JsonPathHelp(json, newJsonPath2, false);
            newJson = newJsonPathHelp.jsonValue(String.class);
//            ReadContext ctx = JsonPath.parse(json);
//            String newJson = ctx.read(newJsonPath, String.class);
//            String otherJsonPath = jsonPath.substring(idx2+2);
//            JsonPathHelp newJsonPathHelp = new JsonPathHelp(newJson, otherJsonPath, false);
//            return newJsonPathHelp.jsonValue(clazz);
        }
        String newJsonPath3 = newJsonPath.startsWith("$.") ? newJsonPath : "$." + newJsonPath;
        return JsonPath.parse(newJson).read(newJsonPath3, clazz);
    }

    private int getClose(String json, int start) {
        Stack<Integer> stack = new Stack<>();
        for (int i=start, l = json.length(); i<l; i++) {
            String c = json.substring(i, i+1);
            if (c.equalsIgnoreCase("(")) {
                stack.push(i);
            } else if (c.equalsIgnoreCase(")")) {
                stack.pop();
                if (stack.size() == 0) {
                    return i;
                }
            }
        }
        return -1;
    }
}
