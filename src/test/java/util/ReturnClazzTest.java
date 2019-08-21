package util;

import ldh.common.testui.demo.common.HelloComplexDemo;
import ldh.common.testui.vo.ReturnClazz;
import ldh.common.testui.vo.VarModel;
import org.junit.Test;

import java.lang.reflect.Method;

/**
 * Created by ldh on 2019/1/14.
 */
public class ReturnClazzTest {

    @Test
    public void testMap() throws NoSuchMethodException {
        Class clazz = HelloComplexDemo.class;
        Method method = clazz.getDeclaredMethod("getCommonMap");
        VarModel varModel = new VarModel(method.getReturnType(), method.getGenericReturnType());
        System.out.println("isBean:" + varModel.getReturnType() + ", key:" + varModel.getKeyClazz() +", value:" + varModel.getBeanClazz());

        Method method2 = clazz.getDeclaredMethod("getMap2");
        VarModel varModel2 = new VarModel(method2.getReturnType(), method2.getGenericReturnType());
        System.out.println("isBean:" + varModel2.getReturnType() + ", key:" + varModel2.getKeyClazz() +", value:" + varModel2.getBeanClazz());

        Method method3 = clazz.getDeclaredMethod("getMap3");
        VarModel varModel3 = new VarModel(method2.getReturnType(), method3.getGenericReturnType());
        System.out.println("isBean:" + varModel3.getReturnType() + ", key:" + varModel3.getKeyClazz() +", value:" + varModel3.getBeanClazz());
    }

    @Test
    public void testList() throws NoSuchMethodException {
        Class clazz = HelloComplexDemo.class;
        Method method = clazz.getDeclaredMethod("getStudents");
        VarModel varModel = new VarModel(method.getReturnType(), method.getGenericReturnType());
        System.out.println("isBean:" + varModel.getReturnType() + ", " + varModel.getBeanClazz());
        Method method2 = varModel.getBeanClazz().getDeclaredMethod("getBeans");
        VarModel varModel2 = new VarModel("var1", method2.getReturnType(), method2.getGenericReturnType(), varModel.getTypeVariableMap());
        System.out.println("isBean:" + varModel2.getReturnType() + ", " + varModel2.getBeanClazz());
    }

    @Test
    public void testCommon() throws Exception {
        Class clazz = HelloComplexDemo.class;
        Method method = clazz.getDeclaredMethod("getIsSuccess");
        VarModel varModel = new VarModel(method.getReturnType(), method.getGenericReturnType());
        System.out.println("returnType:" + varModel.getReturnType() + ", " + varModel.getBeanClazz());

    }

    @Test
    public void test() throws Exception {
        Method method = RefletTest.T.class.getDeclaredMethod("getPageResult", new Class[]{});
        ReturnClazz returnClazz = new ReturnClazz(method.getReturnType(), method.getGenericReturnType());
        System.out.println("type:" + returnClazz.getReturnType() + ", " + returnClazz.getBeanClazz());

        Method method1 = returnClazz.getReturnClazz().getMethod("getBeans", new Class[]{});
        returnClazz = new ReturnClazz(method1.getReturnType(), method1.getGenericReturnType(), returnClazz.getTypeVariableMap());
        System.out.println("type2:" + returnClazz.getReturnType() + ", " + returnClazz.getBeanClazz());
    }

    @Test
    public void test1() throws Exception {
        Method method = RefletTest.T.class.getDeclaredMethod("getPageResult1", new Class[]{});
        ReturnClazz returnClazz = new ReturnClazz(method.getReturnType(), method.getGenericReturnType());
        System.out.println("type:" + returnClazz.getReturnType() + ", " + returnClazz.getBeanClazz());

        Method method1 = returnClazz.getReturnClazz().getMethod("getBeans", new Class[]{});
        returnClazz = new ReturnClazz(method1.getReturnType(), method1.getGenericReturnType(), returnClazz.getTypeVariableMap());
        System.out.println("type2:" + returnClazz.getReturnType() + ", " + returnClazz.getBeanClazz());
    }

    @Test
    public void test2() throws Exception {
        Method method = RefletTest.T.class.getDeclaredMethod("getStudent", new Class[]{});
        ReturnClazz returnClazz = new ReturnClazz(method.getReturnType(), method.getGenericReturnType());
        System.out.println("type:" + returnClazz.getReturnType() + ", " + returnClazz.getBeanClazz());

        Method method1 = returnClazz.getReturnClazz().getMethod("getAge", new Class[]{});
        returnClazz = new ReturnClazz(method1.getReturnType(), method1.getGenericReturnType(), returnClazz.getTypeVariableMap());
        System.out.println("type2:" + returnClazz.getReturnType() + ", " + returnClazz.getBeanClazz());
    }

    @Test
    public void test3() throws Exception {
        Method method = RefletTest.T.class.getDeclaredMethod("getNameType", new Class[]{});
        ReturnClazz returnClazz = new ReturnClazz(method.getReturnType(), method.getGenericReturnType());
        System.out.println("type:" + returnClazz.getReturnType() + ", " + returnClazz.getBeanClazz());
    }
}
