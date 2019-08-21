package ldh.common.testui.assist.convert;

import ldh.common.testui.util.JsonUtil;

import java.lang.reflect.Type;

/**
 * Created by ldh on 2018/3/29.
 */
public class EnumConvert implements Convert {

    // new TypeToken<List<Person>>(){}.getType()
    private Class clazz;

    @Override
    public Enum parse(String str) {
        return Enum.valueOf(clazz, str);
    }

    public void setClass(Class clazz) {
        this.clazz = clazz;
    }
}
