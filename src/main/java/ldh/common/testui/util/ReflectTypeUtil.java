package ldh.common.testui.util;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by ldh on 2018/3/29.
 */
public class ReflectTypeUtil {

    public static Class getMethodReturnT(Method method, int index) {
        Class returnType = method.getReturnType();
        if (method.getReturnType() == void.class) return void.class;
        Type type = method.getGenericReturnType();
        if(type instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (!(actualTypeArguments[index] instanceof ParameterizedType)) {
                return (Class) actualTypeArguments[index];
            }
        }
        return null;
    }

    public static Class getMethodParamT(Type type, int index) {
        if(type instanceof ParameterizedType){
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            return (Class) actualTypeArguments[index];
        }
        return null;
    }
}
