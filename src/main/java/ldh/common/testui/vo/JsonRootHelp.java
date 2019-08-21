package ldh.common.testui.vo;

import com.jayway.jsonpath.JsonPath;
import ldh.common.testui.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class JsonRootHelp {

    private String expression;
    private String json;
    private Object data = null;

    private static final String RValue = "R.value(";

    public static final String REPLACE= "_r";

    public JsonRootHelp(String json, String expression) {
        this.expression = expression;
        this.json = json;
    }

    public String parse() {
        int idx = expression.indexOf(RValue);
        String newExpression = expression;
        if (idx >= 0) {
            int idx2 = getClose(newExpression, idx);
            if (idx2 == -1) {
                throw new RuntimeException("json格式错误!");
            }
            String newJsonPath = expression.substring(idx + RValue.length(), idx2);
            JsonPathHelp jsonPathHelp = new JsonPathHelp(json, newJsonPath);
            String value = jsonPathHelp.jsonValue(String.class);
            data = value;
            return expression.substring(0, idx) + REPLACE + expression.substring(idx2+1);
        }
        return expression;
    }

    public Object getData() {
        return data;
    }

    public Map<String, Object> getNewParamMap() {
        Map<String, Object> paramMap = new HashMap();
        if (data != null) {
            paramMap.put(REPLACE, data);
        }
        return paramMap;
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
