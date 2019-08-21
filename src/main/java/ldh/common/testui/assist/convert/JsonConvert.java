package ldh.common.testui.assist.convert;

import ldh.common.testui.util.JsonUtil;

import java.lang.reflect.Type;

/**
 * Created by ldh on 2018/3/29.
 */
public class JsonConvert<T> implements Convert<T> {

    // new TypeToken<List<Person>>(){}.getType()
    private Type type;

    @Override
    public T parse(String str) {
        return JsonUtil.toObject(str, type);
    }

    public void setType(Type type) {
        this.type = type;
    }
}
