package common;

import ldh.common.testui.demo.common.HelloDemo;
import ldh.common.testui.demo.complex.HelloDemo2;
import ldh.common.testui.util.MethodUtil;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by ldh on 2018/3/21.
 */
public class MethodUtilTest {

    @Test
    public void testMethod() throws IOException {
        Class clazz = HelloDemo2.class;
        List<Method> methods =  MethodUtil.getMethods(clazz);
        methods.stream().forEach(m->System.out.println(m.toGenericString()));

        String path = "java.lang";
        Enumeration<URL> urlEnumerations = MethodUtilTest.class.getClassLoader().getResources(path);
        while (urlEnumerations.hasMoreElements()) {
            URL url = urlEnumerations.nextElement();
            System.out.println("url:" + url);
        }
    }
}
