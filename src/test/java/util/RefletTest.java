package util;

import ldh.common.testui.demo.PageResult;
import ldh.common.testui.demo.Student;
import ldh.common.testui.util.MethodUtil;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by ldh on 2019/1/9.
 */
public class RefletTest {

    @Test
    public void dd() {
        boolean is = Collection.class.isAssignableFrom(List.class);
        System.out.println("is:" + is);

        Class clazz = PageResult.class;
        System.out.println("isArray: " + clazz.isArray());
        System.out.println("isEnum: " + clazz.isEnum());
        System.out.println("isMap: " + Map.class.isAssignableFrom(clazz));
        System.out.println("isList: " + Collection.class.isAssignableFrom(clazz));
        System.out.println("isBean: " + (!clazz.isArray() && !clazz.isEnum() && !Map.class.isAssignableFrom(clazz)
                && !Collection.class.isAssignableFrom(clazz)));
    }

    @Test
    @Ignore
    public void tt() throws Exception {
        T t = new T();
        Method[] methods = T.class.getDeclaredMethods();
        for(Method method : methods) {
            method(method);
//            System.out.println();
        }
    }

    @Test
    public void tt2() throws Exception {
        List<Class> clazzs = Arrays.asList(Integer.class, Short.class, Byte.class, Date.class, String.class, Long.class, Double.class, Enum.class);
        for(Class clazz : clazzs) {
            Set<Method> methods = MethodUtil.getGetMethods(clazz);
            System.out.println(clazz + ":" + methods.size());
            for(Method method : methods) {
//                System.out.println(method.getName());
            }
        }
    }

    private void method(Method method) {
        System.out.println("method:" + method);
        Class<?> returnClazz = method.getReturnType();
        Type type = method.getGenericReturnType();
        System.out.println("return class:" + returnClazz + "," + returnClazz.isArray());
        System.out.println("return type:" + type);
        System.out.println("return type:" + type.toString().equals(returnClazz.toString()));

        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            Type[] types = parameterizedType.getActualTypeArguments();
            if (types.length == 2) {
                Type[] types2 = parameterizedType.getActualTypeArguments();
                if (types2.length == 2) { // map
                    Class type2 = (Class) types2[0];
                    System.out.println("return2 type1:" + type2);
                    Class type22 = (Class) types2[1];
                    System.out.println("return2 type2:" + type22);
                } else {
                    Class type2 = (Class) parameterizedType.getActualTypeArguments()[0];
                    System.out.println("return2 type1:" + type2);
                }
            } else {
                if (types[0] instanceof ParameterizedType) {
                    ParameterizedType parameterizedType1 = (ParameterizedType) types[0];
                    Type[] types2 = parameterizedType1.getActualTypeArguments();
                    if (types2.length == 2) { // map
                        Class type2 = (Class) types2[0];
                        System.out.println("return type1:" + type2);
                        Class type22 = (Class) types2[1];
                        System.out.println("return type2:" + type22);
                    } else {
                        Class type2 = (Class) parameterizedType1.getActualTypeArguments()[0];
                        System.out.println("return type0:" + type2);
                    }

                } else {
                    System.out.println("student:" + types[0]);
                }
            }
        } else if (returnClazz.isArray()) {
            Class cc = returnClazz.getComponentType();
            System.out.println("array:" + cc);
        }
    }


    public static class T {

        public List<Student> getStudents() {
            return new ArrayList<Student>();
        }

        public List<Map<Integer, Student>> getStudents3() {
            return new ArrayList<Map<Integer, Student>>();
        }

        public Student getStudent() {
            return new Student();
        }

        public Student[] getStudent2() {
            return null;
        }

        public Set<Student> getStudentsForSet() {
            return new HashSet<Student>();
        }

        public Map<Integer, Student> getStudentsForMap() {
            return new HashMap<Integer, Student>();
        }

        public PageResult<Student> getPageResult() {
            return new PageResult<Student>();
        }

        public PageResult<Integer> getPageResult1() {
            return new PageResult<Integer>();
        }

        public <T> T getNameType() {
            return null;
        }
    }
}
