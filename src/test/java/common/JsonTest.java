package common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ldh.common.testui.constant.VarType;
import ldh.common.testui.demo.Student;
import ldh.common.testui.util.JsonUtil;
import ldh.common.testui.vo.SpringParam;
import org.junit.Test;

import java.util.Map;

/**
 * Created by ldh on 2018/3/19.
 */
public class JsonTest {

    @Test
    public void jsonTest() {
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .create();
        SpringParam springParam = new SpringParam();
        String json = gson.toJson(springParam);
        System.out.println(json);
    }

    @Test
    public void replace() {
        String d = "ldh.test.demo";
        String t = d.replace(".", "/");
        System.out.println(t);
    }

    @Test
    public void json() {
        Object obj = null;
        System.out.println(JsonUtil.toJson(obj));
    }

    @Test
    public void studentJson() {
        String s = "{\n" +
                "  \"id\": 1,\n" +
                "  \"name\": \"test1\",\n" +
                "  \"age\": 18,\n" +
                "  \"student\": {\"id\":2, \"name\":\"test2\", \"age\":29}\n" +
                "}";
        Student student = JsonUtil.toObject(s, Student.class);
        Object[] args = new Object[]{"testName", student};
        String json = JsonUtil.toSimpleJson(args);
        System.out.println("json:" + json);
    }

    @Test
    public void classTest() throws Exception {
        Object o = Class.forName(RuntimeException.class.getName());
        System.out.println(RuntimeException.class.isInstance(o));
        Object tt = "sadadfas".getClass().newInstance();
        System.out.println(tt);
        System.out.println(tt.getClass().getSimpleName());
    }
}
