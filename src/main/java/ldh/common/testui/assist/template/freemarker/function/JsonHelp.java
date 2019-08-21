package ldh.common.testui.assist.template.freemarker.function;

import java.lang.reflect.Type;

/**
 * Created by ldh on 2018/4/25.
 */
public class JsonHelp {

    public static Object toBean(String json, Class<?> clazzType) {
        return ldh.common.testui.util.JsonUtil.toObject(json, clazzType);
    }

    public static Object toBean(String json, Type clazzType) {
        return ldh.common.testui.util.JsonUtil.toObject(json, clazzType);
    }
}
