package ldh.common.testui.util;

import ldh.common.testui.assist.convert.Convert;
import ldh.common.testui.assist.convert.ConvertFactory;
import ldh.common.testui.assist.convert.EnumConvert;
import ldh.common.testui.assist.convert.JsonConvert;
import ldh.common.testui.assist.template.beetl.BeetlFactory;
import ldh.common.testui.handle.RunTreeItem;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TestMethod;
import ldh.common.testui.vo.MethodData;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by ldh on 2018/3/15.
 */
public class MethodUtil {

    public static final List<Class> COMMON_CLASS = Arrays.asList(Integer.class, int.class, Short.class, short.class, Byte.class, byte.class, Boolean.class,
            boolean.class, String.class, Long.class, long.class, Double.class, double.class, BigDecimal.class, Float.class, float.class, Date.class);


    public static List<Method> getMethods(Class clazz) {
        List<Method> result = new ArrayList<>();
        if (clazz == null || clazz == Object.class) return result;
        Method[] methods = clazz.getDeclaredMethods();
        result = Stream.of(methods).filter(m->m.getModifiers() == 1)
                .filter(m->!m.getName().equals("toString"))
                .filter(m->!m.getName().equals("equals"))
                .filter(m->!m.getName().equals("hashCode"))
                .collect(Collectors.toList());

        List<Method> parentMethods = getMethods(clazz.getSuperclass());
        result.addAll(parentMethods);
        return result;
    }

    public static List<Field> getFields(Class clazz) {
        List<Field> result = new ArrayList<>();
        Field[] fields = clazz.getDeclaredFields();
        result = Stream.of(fields).collect(Collectors.toList());
        return result;
    }


    public static Set<Method> getGetMethods(Class clazz) {
        Set<Method> result = new HashSet<>();
        getMethods(clazz, result, "get", 0);
        return result;
    }

    public static Set<Method> getSetMethods(Class clazz) {
        Set<Method> result = new HashSet<>();
        getMethods(clazz, result, "set", 1);
        return result;
    }

    private static Set<Method> getMethods(Class clazz, Set<Method> methodSet, String prefix, int paramSize) {
        if (clazz == null || clazz == Object.class) return methodSet;
        Method[] methods = clazz.getDeclaredMethods();
        List<Method> result = Arrays.stream(methods).filter(method->(method.getName().startsWith(prefix))
           && method.getParameterTypes().length == paramSize).collect(Collectors.toList());
        methodSet.addAll(result);
        getMethods(clazz.getSuperclass(), methodSet, prefix, paramSize);
        return methodSet;
    }

    public static boolean isPrimitive(Class clazz) {
        return clazz.isPrimitive() || clazz == Integer.class
                || clazz == String.class || clazz == Date.class
                || clazz == Long.class || clazz == BigDecimal.class
                ;
    }

    public static boolean isCollection(Class clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    public static boolean isPojo(Class clazz) {
        return MethodUtil.getSetMethods(clazz).size() > 0 && !Number.class.isAssignableFrom(clazz) && !clazz.isPrimitive();
    }

    public static boolean isMap(Class clazz) {
        return Map.class.isAssignableFrom(clazz);
    }

    public static String buildMethodName(Method method) {
        String name = method.getReturnType().getSimpleName() + " " + method.getName() + "(";
        Parameter[] parameters = method.getParameters();
        Class[] parameterTypes = method.getParameterTypes();
        int i = 0;
        for (Parameter parameter : parameters) {
            if (i == 0) {
                name += parameterTypes[i].getSimpleName() + " " + parameter.getName();
            } else {
                name +=  ", " + parameterTypes[i].getSimpleName() + " " + parameter.getName() ;
            }
            i++;
        }
        return name + ")";
    }

    public static String buildMethodNameNoParamName(Method method) {
        String name = method.getReturnType().getSimpleName() + " " + method.getName() + "(";
        Parameter[] parameters = method.getParameters();
        Class[] parameterTypes = method.getParameterTypes();
        int i = 0;
        for (Parameter parameter : parameters) {
            if (i == 0) {
                name += parameterTypes[i].getSimpleName();
            } else {
                name +=  ", " + parameterTypes[i].getSimpleName();
            }
            i++;
        }
        return name + ")";
    }

    public static Object invoke(Object bean, Method method, Object[] args) throws Exception {
        return method.invoke(bean, args);
    }

    public static Object[] buildArgs(Map<String, MethodData> dataMap, Method method, Map<String, Object> paramMap) throws Exception {
        Parameter[] parameters = method.getParameters();
        Class[] types = method.getParameterTypes();
        Type[] gtypes = method.getGenericParameterTypes();
        Object[] args = new Object[types.length];
        int i=0;
        for (Class clazz : types) {
            String paramName = MethodUtil.paramName(types[i], parameters[i], gtypes[i]);
            MethodData methodData = dataMap.get(paramName);
            if (methodData == null) {
                args[i++] = null;
                continue;
            }
            Convert convert = ConvertFactory.getInstance().get(methodData.getConvert());
            String data = methodData.getData();
            if (convert instanceof JsonConvert) {
                JsonConvert jsonConvert = (JsonConvert) convert;
                jsonConvert.setType(types[i]);
                data = BeetlFactory.getInstance().process(data, paramMap);
            } else if (convert instanceof EnumConvert) {
                EnumConvert enumConvert = (EnumConvert) convert;
                enumConvert.setClass(types[i]);
            }
            if (VarUtil.isVar(data)) {
                String value = BeetlFactory.getInstance().process(data, paramMap);
                args[i] = convert.parse(value);
            } else {
                args[i] = convert.parse(data);
            }
            i++;
        }
        return args;
    }

    public static Object buildReturnValue(Map<String, MethodData> data, Method method, Map<String, Object> paramMap) throws Exception {
        String returnName = MethodUtil.methodReturnName(method);
        MethodData methodData = data.get(returnName);
        Convert convert = ConvertFactory.getInstance().get(methodData.getConvert());
        if (convert instanceof JsonConvert || convert instanceof EnumConvert) {
            ((JsonConvert) convert).setType(method.getReturnType());
        }
        if (VarUtil.isVar(methodData.getData())) {
            String value = BeetlFactory.getInstance().process(methodData.getData(), paramMap);
            return convert.parse(value);
        }
        return convert.parse(methodData.getData());

    }

    public static Object buildReturnValue(Map<String, MethodData> data, Method m) {
        String key = m.getName();
        MethodData methodData = data.get(key);
        if (methodData.getData() == null || methodData.getData().equals("")) return null;
        Convert convert = ConvertFactory.getInstance().get(methodData.getConvert());
        if (convert instanceof JsonConvert) {
            ((JsonConvert) convert).setType(m.getReturnType());
        }
        return convert.parse(methodData.getData());
    }

    public static String methodReturnName(Method method) {
        Class returnClass = method.getReturnType();
        String key = returnClass.getSimpleName() + " " + method.getName();
        return key;
    }

    public static String paramName(Class paramClass, Parameter parameter, Type type) {
        String name = paramClass.getSimpleName() + " " + parameter.getName();
        return name;
    }

    public static Class forClass(String className) {
        if (StringUtils.isEmpty(className)) return null;
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Method forMethod(Class clazz, String methodName) {
        List<Method> methods = MethodUtil.getMethods(clazz);
        for (Method method : methods) {
            if (methodName.equals(MethodUtil.buildMethodName(method))) {
                return method;
            }
        }
        // 如果开启参数名和没看起参数名，可能找不到方法
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
            for (Method method : methods) {
                if (methodName2.equals(MethodUtil.buildMethodNameNoParamName(method))) {
                    return method;
                }
            }
        }
        return null;
    }

    public static String simpleClassName(String className) {
        int idx = className.lastIndexOf(".");
        if (idx > 0) {
            return className.substring(idx + 1);
        }
        return className;
    }

    public static Method findMethodByMethodName(TestMethod testMethod) {
        try {
            Class clazz = Class.forName(testMethod.getClassName());
            List<Method> methods = MethodUtil.getMethods(clazz);
            for (Method method : methods) {
                if (MethodUtil.buildMethodName(method).equals(testMethod.getMethodName())) {
                    return method;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Map<String,ParamModel> buildParamModel(Map<String, MethodData> data, List<ParamModel> allParamModel) {
        Map<String,ParamModel> paramModelMap = new HashMap<>();
        for (MethodData methodData : data.values()) {
            if (VarUtil.isVar(methodData.getData())) {
                String varName = VarUtil.getVarName(methodData.getData());
                for (ParamModel pm : allParamModel) {
                    if (pm.getName().equals(varName)) {
                        paramModelMap.put(varName, pm);
                    }
                }
            }
        }
        return paramModelMap;
    }
}
