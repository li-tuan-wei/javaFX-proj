package ldh.common.testui.assist.convert;

import ldh.common.testui.util.JsonUtil;

import java.lang.reflect.Type;

/**
 * Created by ldh on 2018/3/29.
 */
public class ClassConvert implements Convert{

    @Override
    public Class parse(String str) {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("class路径错误");
    }

}
