package ldh.common.testui.vo;

import ldh.common.testui.constant.ReturnType;
import ldh.common.testui.util.MethodUtil;
import lombok.Data;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

@Data
public class VarModel {

    private String varName;
    private Class<?> clazz;
    private Type type;

    private transient Class<?> beanClazz;  // 实际类型，主要针对泛型
    private transient Class<?> keyClazz; // 泛型map，中key的实际类型
    private transient boolean isManyDatas = false;
    private transient Map<String, Class> typeVariableMap = new HashMap();
    private transient ReturnType returnType;

    public static final List<Class> KEY_CLASS = Arrays.asList(Integer.class, Long.class, String.class);

//    public VarModel() {}

    public VarModel(Class clazz, Type type) {
        this.clazz = clazz;
        this.type = type;
        build();
    }

    public VarModel(String varName, Class clazz, Type type, Map<String, Class> typeVariableMap) {
        this.varName = varName;
        this.clazz = clazz;
        this.type = type;
        this.typeVariableMap = typeVariableMap;
        build();
    }

    public boolean isCommon() {
        return returnType == ReturnType.Common;
    }

    public boolean isTBean() {
        return returnType == ReturnType.TBean;
    }

    public boolean isBean() {
//        boolean result = !clazz.isArray() && !clazz.isEnum() && !Map.class.isAssignableFrom(clazz)
//                && !Collection.class.isAssignableFrom(clazz);
//        if (result) beanClazz = clazz;
//        return result;
        return returnType == ReturnType.Bean;
    }

    public boolean isArray() {
//        boolean result =  clazz.isArray();
//        if (result) {
//            beanClazz = clazz.getComponentType();
//            isManyDatas = true;
//        }
//        return result;
        return returnType == ReturnType.Array;
    }

    public boolean isListOrSet() {
        boolean isResult = Collection.class.isAssignableFrom(clazz);
        if(type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;

            Type[] types = parameterizedType.getActualTypeArguments();
            if (types.length == 1) { // 不是map
                 if (types[0] instanceof TypeVariable) {
                    TypeVariable typeVariable = (TypeVariable) types[0];
                    if (typeVariableMap != null) {
                        beanClazz = typeVariableMap.get(typeVariable.getName());
                        if (MethodUtil.COMMON_CLASS.contains(beanClazz)) {
                            returnType = ReturnType.Common;
                        } else {
                            returnType = ReturnType.TBean;
                        }
                    }
                } else if (!(types[0] instanceof ParameterizedType) && isResult) {  // 不支持多层嵌套
                    beanClazz = (Class) types[0];
                }
            }
        }
        if (isResult) {
            isManyDatas = true;
        }
        return isResult;
    }

    public boolean isMap() {
        if(type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] types = parameterizedType.getActualTypeArguments();
            if (types.length == 2) { // 是map
                if (!(types[0] instanceof ParameterizedType) && !(types[1] instanceof ParameterizedType)) {  // 不支持多层嵌套
                    keyClazz = (Class<?>) types[0];
                    if (KEY_CLASS.contains(keyClazz) || keyClazz.isEnum()) {
                        beanClazz = (Class<?>) types[1];
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isListMap() {
        if(type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] types = parameterizedType.getActualTypeArguments();
            if (types.length == 1) { // 不是map
                if ((types[0] instanceof ParameterizedType)) {  // 不支持多层嵌套
                    ParameterizedType parameterizedType1 = (ParameterizedType) types[0];
                    Type[] types2 = parameterizedType1.getActualTypeArguments();
                    if (types2.length == 2) { // map
                        if (!(types2[0] instanceof ParameterizedType) && !(types2[1] instanceof ParameterizedType)) {  // 不支持多层嵌套
                            keyClazz = (Class<?>) types2[0];
                            if (KEY_CLASS.contains(keyClazz) || keyClazz.isEnum()) {
                                beanClazz = (Class<?>) types2[1];
                                isManyDatas = true;
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    public Class getBeanClazz() {
        return beanClazz;
    }

    public Class getKeyClazz() {
        return keyClazz;
    }

    private void build() {
        boolean isT = false;
        if (type instanceof ParameterizedType) {
            isT = true;
            ParameterizedType parameterizedType = (ParameterizedType) type;

            Type[] types = parameterizedType.getActualTypeArguments();
            if (types.length == 2) { // map
                Type[] types2 = parameterizedType.getActualTypeArguments();
                if (types2.length == 2) { // map
                    Class type2 = (Class) types2[0];
                    keyClazz = type2;
                    if (types2[1] instanceof ParameterizedType) {
                        ParameterizedType ptype3 = (ParameterizedType) types2[1];
                        Type[] type3 = ptype3.getActualTypeArguments();
                        Class type22 = (Class) type3[0];
                        beanClazz = type22;
                    } else {
                        Class type22 = (Class) types2[1];
                        beanClazz = type22;
                    }

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
//                        System.out.println("return type1:" + type2);
                        Class type22 = (Class) types2[1];
//                        System.out.println("return type2:" + type22);
                    } else {
                        Class type2 = (Class) parameterizedType1.getActualTypeArguments()[0];
//                        System.out.println("return type0:" + type2);
                    }
                } else if (types[0] instanceof TypeVariable) {
                    TypeVariable typeVariable = (TypeVariable) types[0];
                    if (typeVariableMap != null) {
                        beanClazz = typeVariableMap.get(typeVariable.getName());
                        if (MethodUtil.COMMON_CLASS.contains(beanClazz)) {
                            returnType = ReturnType.Common;
                        } else {
                            returnType = ReturnType.TBean;
                        }
                    }
                } else {
                    beanClazz = (Class) types[0];
                    returnType = ReturnType.TBean;

                    Arrays.stream(clazz.getTypeParameters()).forEach(classTypeVariable -> typeVariableMap.put(classTypeVariable.getName(), beanClazz));

                }
            }
        }
        if (clazz.isArray()) {
            Class cc = clazz.getComponentType();
            returnType = ReturnType.Array;
            beanClazz = cc;
        } else if (clazz.isEnum()) {
            returnType = ReturnType.Enum;
            beanClazz = clazz;
        } else if (MethodUtil.COMMON_CLASS.contains(clazz)) {
            returnType = ReturnType.Common;
            beanClazz = clazz;
        } else if (Map.class.isAssignableFrom(clazz)) {
            returnType = ReturnType.Map;
        } else if (Collection.class.isAssignableFrom(clazz)) {
            returnType = ReturnType.List;
        } else {
            returnType = ReturnType.Bean;
            if (isT) {
                returnType = ReturnType.TBean;
            }
            beanClazz = clazz;
        }
    }

}
