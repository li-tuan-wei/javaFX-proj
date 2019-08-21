package ldh.common.testui.util;

import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.MapValueFactory;
import ldh.common.testui.cell.CheckColumnCell;
import ldh.common.testui.cell.MethodDataColumnCell;
import ldh.common.testui.cell.ObjectTableCellFactory;
import ldh.common.testui.constant.ParamCategory;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.vo.MethodData;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ldh on 2018/3/17.
 */
public class TableMapUtil {

    public static TableColumn<Map<String, MethodData>, MethodData> buildExceptionColumns(Method method) {
        TableColumn<Map<String, MethodData>, MethodData> exceptionColumn = new TableColumn<>("异常情况");

        TableColumn<Map<String, MethodData>, MethodData> exceptionColumn1 = new TableColumn("exception");
        exceptionColumn1.setCellValueFactory(new MethodDataColumnCell("exception"));
        exceptionColumn1.setPrefWidth(80);
        exceptionColumn1.setCellFactory(new ObjectTableCellFactory<>((methodData -> methodData.getData())));
        exceptionColumn.getColumns().add(exceptionColumn1);

        TableColumn<Map<String, MethodData>, MethodData> exceptionColumn2 = new TableColumn("exceptionName");
        exceptionColumn2.setCellValueFactory(new MethodDataColumnCell("exceptionName"));
        exceptionColumn2.setPrefWidth(120);
        exceptionColumn2.setCellFactory(new ObjectTableCellFactory<>((methodData -> methodData.getData())));
        exceptionColumn.getColumns().add(exceptionColumn2);

        return exceptionColumn;
    }

    public static TableColumn<Map<String, MethodData>, MethodData> buildResultColumns(Method method) {
        String methodReturnName = MethodUtil.methodReturnName(method);
        TableColumn<Map<String, MethodData>, MethodData> resultColumn = new TableColumn(methodReturnName);
        resultColumn.setCellValueFactory(new MethodDataColumnCell(methodReturnName));
        resultColumn.setCellFactory(new ObjectTableCellFactory<>((methodData -> methodData.getData())));

//        Class returnClass = method.getReturnType();
//        if (returnClass != void.class) {
//            String methodReturnName = MethodUtil.methodReturnName(method);
//            TableColumn<Map<String, MethodData>, MethodData> tableColumn = new TableColumn(methodReturnName);
//            tableColumn.setCellValueFactory(new MethodDataColumnCell(methodReturnName));
//            tableColumn.setCellFactory(new ObjectTableCellFactory<>((methodData -> methodData.getData())));

//            if (MethodUtil.isPrimitive(returnClass)) {
//                resultColumn.getColumns().add(tableColumn);
//            } else if (MethodUtil.isCollection(returnClass)) {
//                tableColumn.setPrefWidth(100);
//                resultColumn.getColumns().add(tableColumn);
//            } else if (MethodUtil.isMap(returnClass)) {
//                tableColumn.setPrefWidth(180);
//                resultColumn.getColumns().add(tableColumn);
//            } else if (returnClass.isArray()) {
//                tableColumn.setPrefWidth(100);
//                resultColumn.getColumns().add(tableColumn);
//            } else {
//                Set<Method> methods = MethodUtil.getGetMethods(returnClass);
//                for (Method m : methods) {
//                    TableColumn<Map<String, MethodData>, MethodData> tc = new TableColumn(m.getReturnType().getSimpleName() + " " + m.getName());
//                    tc.setCellValueFactory(new MethodDataColumnCell(m.getReturnType().getSimpleName() + " " + m.getName()));
//                    tc.setCellFactory(new ObjectTableCellFactory<>((methodData -> methodData.getData())));
//                    resultColumn.getColumns().add(tc);
//                }
//            }
//        }

        return resultColumn;
    }

    public static TableColumn<Map<String, MethodData>, MethodData> buildParamColumns(Method method) {
        TableColumn<Map<String, MethodData>, MethodData> paramColumn = new TableColumn<>("参数");

        Parameter[] parameters = method.getParameters();
        Class[] types = method.getParameterTypes();
        Type[] gtypes = method.getGenericParameterTypes();
        int i = 0;
        for (Parameter parameter : parameters) {
            String paramName = MethodUtil.paramName(types[i], parameter, gtypes[i]);
            TableColumn<Map<String, MethodData>, MethodData> tableColumn = new TableColumn<Map<String, MethodData>, MethodData> (paramName);
            tableColumn.setCellValueFactory(new MethodDataColumnCell(paramName));
            tableColumn.setCellFactory(new ObjectTableCellFactory<>((methodData -> methodData.getData())));

            if (MethodUtil.isPrimitive(types[i]) || types[i].isEnum()) {

            } else if (MethodUtil.isCollection(types[i])){
                tableColumn.setPrefWidth(120);
            } else if (MethodUtil.isMap(types[i])){
                tableColumn.setPrefWidth(170);
            } else if (types[i].isArray()){
                tableColumn.setPrefWidth(100);
            } else {
                tableColumn.setPrefWidth(280);
            }
            i++;
            paramColumn.getColumns().add(tableColumn);
        }
        return parameters.length > 0 ? paramColumn : null;
    }

    public static TableColumn<Map<String, MethodData>, MethodData> buildNameColumns(String name, String desc) {
        TableColumn<Map<String, MethodData>, MethodData> nameColumn = new TableColumn<>(desc);
        nameColumn.setCellValueFactory(new MethodDataColumnCell(name));
        nameColumn.setCellFactory(new ObjectTableCellFactory<>((methodData -> methodData.getData())));

        return nameColumn;
    }

    public static TableColumn<Map<String, MethodData>, MethodData> buildBeanSetColumns(Class clazz) {
        TableColumn<Map<String, MethodData>, MethodData> beanSetColumn = new TableColumn<>("对象赋值");

        Set<Method> setMethods = MethodUtil.getSetMethods(clazz);
        for (Method method : setMethods) {
            TableColumn<Map<String, MethodData>, MethodData> tableColumn = new TableColumn(method.getName()+ "(" + method.getParameterTypes()[0].getSimpleName() + ")");
            tableColumn.setCellValueFactory(new MethodDataColumnCell(method.getName()+ "(" + method.getParameterTypes()[0].getSimpleName() + ")"));
            tableColumn.setCellFactory(new ObjectTableCellFactory<>((methodData -> methodData.getData())));
            tableColumn.setPrefWidth(150);
            beanSetColumn.getColumns().add(tableColumn);
        }

        return setMethods.size() > 0 ? beanSetColumn : null;
    }

    public static TableColumn<Map<String, MethodData>, MethodData> buildCheckColumns(Method method) {
        TableColumn<Map<String, MethodData>, MethodData> checkColumn = new TableColumn("验证结果");
        checkColumn.setCellValueFactory(new MethodDataColumnCell("check"));
//        checkColumn.setCellFactory(new ObjectTableCellFactory<>((methodData -> methodData.getData())));
        checkColumn.setCellFactory(new CheckColumnCell());
        return checkColumn;
    }

    public static TableColumn<Map<String, MethodData>, MethodData> buildBeanGetColumns(Class clazz) {
        TableColumn<Map<String, MethodData>, MethodData> beanGetColumn = new TableColumn<>("对象取值");

        Set<Method> getMethods = MethodUtil.getGetMethods(clazz);
        for (Method method : getMethods) {
            TableColumn<Map<String, MethodData>, MethodData> tableColumn = new TableColumn(MethodUtil.buildMethodName(method));
            tableColumn.setCellValueFactory(new MethodDataColumnCell(method.getReturnType().getSimpleName() + " " + method.getName()));
            tableColumn.setCellFactory(new ObjectTableCellFactory<>((methodData -> methodData.getData())));
            tableColumn.setPrefWidth(150);
            beanGetColumn.getColumns().add(tableColumn);
        }

        return getMethods.size() > 0 ? beanGetColumn : null;
    }
}
