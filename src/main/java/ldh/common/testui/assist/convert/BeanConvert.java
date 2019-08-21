package ldh.common.testui.assist.convert;

/**
 * Created by ldh on 2018/3/29.
 */
public class BeanConvert implements Convert{

    @Override
    public Object parse(String str) {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("class路径错误");
    }

}
