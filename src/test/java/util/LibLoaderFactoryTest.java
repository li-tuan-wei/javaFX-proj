package util;

import ldh.common.testui.util.LibLoaderFactory;
import ldh.common.testui.util.MethodUtil;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by ldh on 2019/1/17.
 */
public class LibLoaderFactoryTest {

    public static void main(String[] args) throws Exception {
        String path = "E:\\testuiclass\\";
        Class clazz = LibLoaderFactory.getInstance().loadClass(path, "util.BankCardNoUtil");
        Object bean = clazz.newInstance();
        List<Method> methods = MethodUtil.getMethods(bean.getClass());
        methods.stream().forEach(method -> {
            System.out.println("method:" + method.getName());
        });
        Method method = clazz.getDeclaredMethod("createICBCBankCardNo");
        String code = (String) method.invoke(bean, new Object[]{});
        System.out.println("code:" + code);
        LibLoaderFactory.getInstance().close();
    }
}
