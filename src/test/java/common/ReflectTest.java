package common;

import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import ldh.common.testui.demo.Student;
import ldh.common.testui.demo.complex.HelloDemo2;
import ldh.common.testui.util.JsonUtil;
import ldh.common.testui.util.ReflectTypeUtil;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ldh on 2018/3/29.
 */
public class ReflectTest {

    @Test
    public void list() throws NoSuchMethodException {
        Method method = HelloDemo2.class.getDeclaredMethod("listTest", String.class);
        Type type = method.getGenericReturnType();// 获取返回值类型
        Class returnClass = method.getReturnType();
        if (List.class.isAssignableFrom(returnClass)) {
            System.out.println("true");
        }
        if (type instanceof ParameterizedType) { // 判断获取的类型是否是参数类型
            System.out.println(type);
            Type[] typesto = ((ParameterizedType) type).getActualTypeArguments();// 强制转型为带参数的泛型类型，
            // getActualTypeArguments()方法获取类型中的实际类型，如map<String,Integer>中的
            // String，integer因为可能是多个，所以使用数组
            for (Type type2 : typesto) {
//                System.out.println("泛型类型" + type2);
            }
        }
    }

    @Test
    public void array() throws NoSuchMethodException {
        Method method = HelloDemo2.class.getDeclaredMethod("arrayTest", String[].class);
        Class[] paramTypes = method.getParameterTypes();
        System.out.println(paramTypes[0].isArray());
        Type type = method.getGenericReturnType();// 获取返回值类型
        Class returnClass = method.getReturnType();
        if (List.class.isAssignableFrom(returnClass)) {
            System.out.println("true");
        }
        if (type instanceof ParameterizedType) { // 判断获取的类型是否是参数类型
            System.out.println(type);
            Type[] typesto = ((ParameterizedType) type).getActualTypeArguments();// 强制转型为带参数的泛型类型，
            // getActualTypeArguments()方法获取类型中的实际类型，如map<String,Integer>中的
            // String，integer因为可能是多个，所以使用数组
            for (Type type2 : typesto) {
                System.out.println("泛型类型" + type2);
            }
        }
    }

    @Test
    public void test2() {
        List<String> list = new ArrayList<>();
        System.out.println(list instanceof List);
        System.out.println(list instanceof ArrayList);
        System.out.println(list.getClass().isAssignableFrom(List.class));
        System.out.println(List.class.isAssignableFrom(list.getClass()));
    }

    @Test
    public void arrayTest() {
        String[] arrayStr = new String[]{};
        String json = "[\"adfas\", \"dsfasdfas\"]";
        String[] result = JsonUtil.toObject(json, arrayStr.getClass());
        Arrays.stream(result).forEach(System.out::println);
    }

    @Test
    public void arrayTest2() {
        Student[] arrayStr = new Student[]{};
        String json = "[{\"id\":12,\"name\":\"test\", \"age\":18}, {\"id\":13,\"name\":\"test1\", \"age\":19}]";
        Student[] result = JsonUtil.toObject(json, arrayStr.getClass());
        Arrays.stream(result).forEach(e->System.out.println(e.getName() + "," + e.getAge() + "," + e.getId()));
    }

    @Test
    public void listTest() throws NoSuchMethodException {
        Method method = HelloDemo2.class.getDeclaredMethod("listTest", String.class);
        Type type = ReflectTypeUtil.getMethodReturnT(method, 0);
        Class cc = (Class) type;
        System.out.println(cc.getSimpleName());
    }

    @Test
    @Ignore
    public void enumTest(){
        Class glyphIcons = MaterialDesignIcon.class;
        GlyphIcons[] tt = (GlyphIcons[]) glyphIcons.getEnumConstants();
        for (GlyphIcons g : tt) {
            System.out.println("ggg:" + g);
        }
    }
}
