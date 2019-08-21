package ldh.common.testui.assist.template.beetl.function;

import ldh.common.testui.util.JsonUtil;
import ldh.common.testui.vo.JsonPathHelp;

import java.lang.reflect.Type;

/**
 * Created by ldh on 2018/12/25.
 */
public class JsonHelp {

    public Object toBean(String json, Class<?> clazzType) {
        return ldh.common.testui.util.JsonUtil.toObject(json, clazzType);
    }

    public Object toBean(String json, Type clazzType) {
        return ldh.common.testui.util.JsonUtil.toObject(json, clazzType);
    }

    public String toJson(String str) {
        return JsonUtil.parseJson(str);
    }

    public String jsonPathValue(String json, String jsonPath) {
        JsonPathHelp jsonPathHelp = new JsonPathHelp(json, jsonPath);
        return jsonPathHelp.jsonValue(String.class);
    }
}
