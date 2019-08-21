package ldh.common.testui.vo;

import ldh.common.testui.constant.ReturnType;
import lombok.Data;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by ldh on 2019/1/14.
 */
@Data
public class ReturnClazz {

    private Class<?> returnClazz;
    private Type type;

    private ReturnType returnType;
    private transient Class<?> beanClazz;  // 实际类型，主要针对泛型
    private transient Class<?> keyClazz; // 泛型map，中key的实际类型

    private Map<String, Class> typeVariableMap = new HashMap();

    public static final List<Class> COMMON_CLASS = Arrays.asList(Integer.class, int.class, Short.class, short.class, Byte.class, byte.class, Boolean.class,
            boolean.class, String.class, Long.class, long.class, Double.class, double.class, BigDecimal.class, Float.class, float.class, Date.class);

    public ReturnClazz(Class<?> returnClazz, Type type) {
        this.returnClazz = returnClazz;
        this.type = type;
        build(null);
    }

    public ReturnClazz(Class<?> returnClazz, Type type, Map<String, Class> typeVariableNameMap) {
        this.returnClazz = returnClazz;
        this.type = type;
        build(typeVariableNameMap);
    }

    private void build(Map<String, Class> typeVariableNameMap) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            Type[] types = parameterizedType.getActualTypeArguments();
            if (types.length == 2) { // map
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
                } else if (types[0] instanceof TypeVariable) {
                    TypeVariable typeVariable = (TypeVariable) types[0];
                    if (typeVariableNameMap != null) {
                        beanClazz = typeVariableNameMap.get(typeVariable.getName());
                        if (COMMON_CLASS.contains(beanClazz)) {
                            returnType = ReturnType.Common;
                        } else {
                            returnType = ReturnType.Bean;
                        }
                    }
                } else {
                    beanClazz = (Class) types[0];
                    returnType = ReturnType.Bean;

                    Arrays.stream(returnClazz.getTypeParameters()).forEach(classTypeVariable -> typeVariableMap.put(classTypeVariable.getName(), beanClazz));

                }
            }
        } else if (returnClazz.isArray()) {
            Class cc = returnClazz.getComponentType();
            returnType = ReturnType.Array;
            beanClazz = cc;
        } else if (returnClazz.isEnum()) {
            returnType = ReturnType.Enum;
            beanClazz = returnClazz;
        } else if (COMMON_CLASS.contains(returnClazz)) {
            returnType = ReturnType.Common;
            beanClazz = returnClazz;
        } else {
            returnType = ReturnType.Bean;
            beanClazz = returnClazz;
        }
    }
}
