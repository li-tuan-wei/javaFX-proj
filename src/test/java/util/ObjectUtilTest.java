package util;

import ldh.common.testui.assist.convert.ConvertFactory;
import ldh.common.testui.assist.template.beetl.function.LangHelp;
import ldh.common.testui.assist.template.freemarker.function.JsonHelp;
import ldh.common.testui.util.JsonUtil;
import ldh.common.testui.util.MethodUtil;
import ldh.common.testui.util.ObjectUtil;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by ldh on 2018/12/24.
 */
public class ObjectUtilTest {

    @Test
    public void equalTest() {
        String a1 = "100.00";
        String a2 = "100.0000";
        Object aa1 = ConvertFactory.getInstance().get("BigDecimal").parse(a1);
        Object aa2 = ConvertFactory.getInstance().get("BigDecimal").parse(a2);
        boolean isEqual = ObjectUtil.isEqual(aa1, aa2);
        System.out.println("isEqual:" + isEqual);
    }

    @Test
    public void test() {
        Set<String> methodSet = new HashSet();
        Object bean = new LangHelp();
        methodSet.add(bean.getClass().getSimpleName());
        List<Method> methods = MethodUtil.getMethods(bean.getClass());
        for(Method method : methods) {
            methodSet.add(method.getName());
        }

        methodSet.forEach(str->System.out.println(str));
    }

    @Test
    public void mapTest() {
        Map<String, Object> map1 = new HashMap();
        map1.put("key1", "key1");

        Map<String, Object> map2 = new HashMap();
        map2.put("key2", "key2");
        map2.putAll(map1);

        System.out.println(JsonUtil.toJson(map2));
        map1.put("key1-1", "key1-1");
        System.out.println(JsonUtil.toJson(map2));

    }

    @Test
    public void methodName() {
        String methodName = "Student getById(Integer id)";
        int idx = methodName.indexOf("(");
        if (idx > 0) {
            int idx2 = methodName.indexOf(")", idx+1);
            String paramStr = methodName.substring(idx, idx2);
            String[] params = paramStr.split(",");
            String p = "";
            for (String param : params) {
                p += param.split(" ")[0];
            }
            String methodName2 = methodName.substring(0, idx) + p + methodName.substring(idx2);
            System.out.println(methodName2);
        }
    }

    @Test
    public void swaggerUrl() {
        String url = "/achievement/deleteById/{id2}/json/{id}";
        url = url.replace("{", "${");
        System.out.println("url:" + url);

        String u = "http://localhost:8080/swagger-ui.html";
        int t = u.indexOf("swagger-ui.html");
        if (t > 0) {
            u = u.substring(0, t) + "v2/api-docs";
        }
        System.out.println("u:" + u);
    }
}
