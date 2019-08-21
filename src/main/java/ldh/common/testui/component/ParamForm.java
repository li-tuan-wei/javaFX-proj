package ldh.common.testui.component;

import com.jfoenix.controls.JFXButton;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import ldh.common.testui.cell.ObjectStringConverter;
import ldh.common.testui.constant.CompareType;
import ldh.common.testui.constant.MethodType;
import ldh.common.testui.controller.ClassPaneController;
import ldh.common.testui.assist.convert.ConvertFactory;
import ldh.common.testui.util.JsonUtil;
import ldh.common.testui.util.MethodUtil;
import ldh.common.testui.util.VarUtil;
import ldh.common.testui.vo.MethodData;
import ldh.common.testui.vo.ParamCell;
import ldh.common.testui.vo.ReturnClazz;
import ldh.common.testui.vo.VarModel;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ldh on 2018/3/22.
 */
public class ParamForm extends GridPane {

    private Class clazz;
    private Method method;
    private Map<String, ParamCell> paramCellMap = new HashMap<>();
    private TableView<Map<String, MethodData>> tableView;
    private ClassPaneController classPaneController;
    private Map<String, MethodData> editDataMap;
    private TextField varNameTextField = null;
    private TextField testNameTextField = new TextField();

    public ParamForm(Class clazz, Method method, TableView<Map<String, MethodData>> tableView) {
        this.clazz = clazz;
        this.method = method;
        this.tableView = tableView;
        int row = 0;
//        row = initTitle(row);
        row = initMethod(row);
        row = initTestName(row);
        row = initSetValue(row);
        row = initParamValue(row);
        row = initExceptionValue(row);
        row = initReturnValue(row);
        row = initGetValue(row);
//        row = initReturnVar(row);
        row = initSubmit(row);

        initConstraints();
        this.setHgap(5);
        this.setVgap(5);
        this.getStyleClass().add("param-form");

        this.setStyle("-fx-background-color: whitesmoke");
    }

    private int initTestName(int row) {
        row = addSeparator(row, "测试说明");
        Label label = new Label("测试名称");
        testNameTextField = new TextField();
        GridPane.setConstraints(label, 0, row, 1, 1);
        GridPane.setConstraints(testNameTextField, 1, row, 1, 1);
        this.getChildren().addAll(label, testNameTextField);
        return ++row;
    }

    public void setClassPaneController(ClassPaneController classPaneController) {
        this.classPaneController = classPaneController;
    }

    private int initReturnVar(int row) {
        Class returnClass = method.getReturnType();
        if (returnClass == void.class) return row;
        row = addSeparator(row, "设置返回值变量");
        Label label = new Label("变量名称");
        varNameTextField = new TextField();
        GridPane.setConstraints(label, 0, row, 1, 1);
        GridPane.setConstraints(varNameTextField, 1, row, 1, 1);
        this.getChildren().addAll(label, varNameTextField);
        return ++row;
    }

    private int initSubmit(int row) {
        Button submit = new JFXButton("保存");
        submit.getStyleClass().addAll("btn", "btn-info");
        submit.setOnAction(this::saveBtn);
        Button reset = new JFXButton("取消");
        reset.getStyleClass().addAll("btn", "btn-info");
        reset.setOnAction(e->classPaneController.showTablePane());
        GridPane.setConstraints(submit, 0, row, 1, 1);
        GridPane.setConstraints(reset, 1, row, 3, 1);
        this.getChildren().addAll(submit, reset);
        return ++row;
    }

    private int initTitle(int row) {
        Label label = new Label("添加参数");
        label.setFont(Font.font(24));
        GridPane.setConstraints(label, 1, row, 2, 1);
        this.getChildren().add(label);
        return ++row;
    }

    private int initGetValue(int row) {
        row = addSeparator(row, "对象取值");
        Set<Method> getMethods = MethodUtil.getGetMethods(clazz);
        for (Method method : getMethods) {
            add(row++, method.getReturnType().getSimpleName() + " " + method.getName(),MethodType.Get, method.getReturnType(), method.getGenericReturnType());
        }
        return row;
    }

    private int initExceptionValue(int row) {
        row = addSeparator(row, "异常检测");
        add(row++, "exception", MethodType.Exception, Boolean.class, Boolean.class);
        add(row++, "exceptionName", MethodType.Exception, String.class, String.class);
        return row;
    }

    private int initReturnValue(int row) {
        Class returnClass = method.getReturnType();
        if (returnClass != void.class) {
            row = addSeparator(row, "返回值");
            String methodReturnName = MethodUtil.methodReturnName(method);
            add(row++, methodReturnName, MethodType.Return, returnClass, method.getGenericReturnType());
        }
        return row;
    }

    private int initSetValue(int row) {
        Set<Method> setMethods = MethodUtil.getSetMethods(clazz);
        if (setMethods.size() > 0) {
            row = addSeparator(row, "对象赋值");
        }
        for (Method method : setMethods) {
            Button button = new Button("X");
            add(row, method.getName() + "(" + method.getParameterTypes()[0].getSimpleName() + ")", MethodType.Set, method.getParameterTypes()[0], method.getGenericParameterTypes()[0], button);
            row++;
        }
        return row;
    }

    private int initParamValue(int row) {
        Parameter[] parameters = method.getParameters();
        Class[] types = method.getParameterTypes();
        Type[] gtypes = method.getGenericParameterTypes();
        if (parameters.length > 0) row = addSeparator(row, "参数赋值");
        int i = 0;
        for (Parameter parameter : parameters) {
            String paramName = MethodUtil.paramName(types[i], parameter, gtypes[i]);
            add(row, paramName, MethodType.Param, types[i], gtypes[i]);
            row++;
            i++;
        }
        return row;
    }

    private void add(int row, String labelName, MethodType methodType, Class clazz, Type type ) {
        add(row, labelName, methodType, clazz, type, null);
    }

    private void add(int row, String labelName, MethodType methodType, Class clazz, Type type, Button button) {
        Label label = new Label(labelName);
        Region region = buildRegion(clazz);
        if (methodType == MethodType.Get && !ReturnClazz.COMMON_CLASS.contains(clazz) && !clazz.isEnum() && !clazz.isArray()) {
            int col = 0;
            Label cbLabel = new Label("只能设置变量，使用对象验证，示例：{{变量名称}}");
            GridPane.setConstraints(label, col++, row, 1, 1);
            GridPane.setConstraints(region, col++, row, 1, 1);
            GridPane.setConstraints(cbLabel, col++, row, 2, 1);
            this.getChildren().addAll(label, region, cbLabel);

            String varName = "{{" + this.clazz.getSimpleName() + "_" + labelName.split(" ")[1] + "}}";
            ((TextField) region).setText(varName);
            ParamCell paramCell = new ParamCell(region, null, methodType, clazz, type, labelName);
            paramCellMap.put(labelName, paramCell);
        } else {
            Label cbLabel = new Label("转换器：");
            ComboBox<String> convertBox = buildConvertComboBox(clazz, methodType, region);
            ParamCell paramCell = new ParamCell(region, convertBox, methodType, clazz, type, labelName);
            paramCellMap.put(labelName, paramCell);

            int col = 0;
            GridPane.setConstraints(label, col++, row, 1, 1);
            GridPane.setConstraints(region, col++, row, 1, 1);
            GridPane.setConstraints(cbLabel, col++, row, 1, 1);
            GridPane.setConstraints(convertBox, col++, row, 1, 1);
            this.getChildren().addAll(label, region, cbLabel, convertBox);
        }

    }

    private int initMethod(int row) {
        String m = method.toString();
        HBox hBox = new HBox();
        hBox.setPadding(new Insets(5, 5, 5, 20));
        Label label = new Label(m.trim());
        label.setStyle("-fx-font-size: 16px");
        hBox.getChildren().add(label);
        GridPane.setConstraints(hBox, 0, row, 5, 1);
        this.getChildren().add(hBox);
        return ++row;
    }

    private ComboBox<String> buildConvertComboBox(Class clazz, MethodType methodType, Region region) {
        ComboBox<String> convertComboBox = new ComboBox<>();
        Set<String> stringSet = ConvertFactory.getInstance().getConvertMap().keySet();
        convertComboBox.getItems().addAll(stringSet);
        if (ConvertFactory.getInstance().get(clazz.getSimpleName()) != null && methodType != MethodType.Exception) {
            convertComboBox.getSelectionModel().select(clazz.getSimpleName());
        }
        if (MethodUtil.isCollection(clazz) || MethodUtil.isMap(clazz) || clazz.isArray()) {
            convertComboBox.getSelectionModel().select("Json");
            jsonAndBean(convertComboBox, region);
        } else if (clazz.isEnum()) {
            convertComboBox.getSelectionModel().select("Enum");
        } else if (clazz == String.class && methodType == MethodType.Exception) {
            convertComboBox.getSelectionModel().select("Class");
        } else if (clazz == Boolean.class && methodType == MethodType.Exception) {
            convertComboBox.getSelectionModel().select(clazz.getSimpleName());
        } else if (ConvertFactory.getInstance().get(clazz.getSimpleName()) != null) {
            convertComboBox.getSelectionModel().select(clazz.getSimpleName());
        } else {
            convertComboBox.getSelectionModel().select("Json");
            jsonAndBean(convertComboBox, region);
        }
        return convertComboBox;
    }

    private Region buildRegion(Class clazz) {
        if (clazz == boolean.class || clazz == Boolean.class) {
            ChoiceBox choiceBox = new ChoiceBox();
            choiceBox.setPrefWidth(200);
            choiceBox.getItems().addAll("空值", "False", "True");
            return choiceBox;
        } else if (clazz == Exception.class) {
            return new CheckBox("是否为异常");
        } else if (clazz.isEnum()) {
            Object[] objs = clazz.getEnumConstants();
            List<String> enumNames = Arrays.stream(objs).map(obj->obj.toString()).collect(Collectors.toList());;
            ComboBox<String> enumComboBox = new ComboBox<>();
            enumComboBox.getItems().addAll(enumNames);
            return enumComboBox;
        }
        return new TextField();
    }

    private void initConstraints() {
        ColumnConstraints columnConstraints1 = new ColumnConstraints(150);
        columnConstraints1.setHalignment(HPos.RIGHT);

        ColumnConstraints columnConstraints2 = new ColumnConstraints(200,400, 20000, Priority.ALWAYS, HPos.LEFT, true);
        ColumnConstraints columnConstraints3 = new ColumnConstraints(100);
        columnConstraints3.setHalignment(HPos.RIGHT);
        ColumnConstraints columnConstraints4 = new ColumnConstraints(200);
        this.getColumnConstraints().addAll(columnConstraints1, columnConstraints2, columnConstraints3, columnConstraints4);
    }

    private int addSeparator(int row, String name) {
        StackPane stackPane = new StackPane();
        stackPane.setAlignment(Pos.CENTER_LEFT);
        Separator separator = new Separator();
        Label label = new Label(name);
        stackPane.getChildren().addAll(separator, label);
        GridPane.setConstraints(stackPane, 0, row++, 6, 1);
        this.getChildren().add(stackPane);
        return row;
    }

    private void saveBtn(ActionEvent actionEvent) {
        Map<String, MethodData> dataMap = new HashMap<>();
        MethodData testName = new MethodData(0, "name", testNameTextField.getText().trim(), null, null,null);
        dataMap.put("testName", testName);
        for(Map.Entry<String, ParamCell> entry : paramCellMap.entrySet()) {
            ParamCell paramCell = entry.getValue();
            String value = paramCell.getValue();
            if (StringUtils.isEmpty(value)) continue;
            if (VarUtil.isPutVar(value) && (paramCell.getMethodType() == MethodType.Get)) {
                VarModel varModel = new VarModel(paramCell.getClazz(), paramCell.getType());
                varModel.setVarName(VarUtil.getPutVarName(paramCell.getValue()));

                MethodData methodData = new MethodData(0, entry.getKey(), value, paramCell.getMethodType(), paramCell.getClazz().getName(), null);
                dataMap.put(entry.getKey(), methodData);
                continue;
            }
            MethodData methodData = new MethodData(0, entry.getKey(), value, paramCell.getMethodType(), paramCell.getClazz().getName(), paramCell.getConvert().getSelectionModel().getSelectedItem());
            if (paramCell.getMethodType() == MethodType.Set) {
                dataMap.put(entry.getKey(), methodData);
            } else if (paramCell.getMethodType() == MethodType.Param) {
                dataMap.put(entry.getKey(), methodData);
            } else if (paramCell.getMethodType() == MethodType.Return) {
                dataMap.put(entry.getKey(), methodData);
            } else if (paramCell.getMethodType() == MethodType.Return_bean) {
                dataMap.put(entry.getKey(), methodData);
            } else if (paramCell.getMethodType() == MethodType.Exception) {
                dataMap.put(entry.getKey(), methodData);
            } else if (paramCell.getMethodType() == MethodType.Get) {
                dataMap.put(entry.getKey(), methodData);
            } else {
                dataMap.put(entry.getKey(), methodData);
            }
        }
        if (this.editDataMap !=  null) {
            int idx = tableView.getItems().indexOf(editDataMap);
//            tableView.getItems().remove(editDataMap);
            classPaneController.removeData(editDataMap);
            tableView.getItems().add(idx, dataMap);
        } else {
            tableView.getItems().add(dataMap);
        }
        classPaneController.saveParam(new ActionEvent());
        classPaneController.showTablePane();
    }

    private void jsonAndBean(ComboBox<String> convertComboBox, Region region) {
//        convertComboBox.getSelectionModel().selectedItemProperty().addListener((b, o, n)->{
//            if (n.equals("Bean")) {
//                region.getChildrenUnmodifiable().get(0).setVisible(true);
//                region.getChildrenUnmodifiable().get(1).setVisible(false);
//                region.setPrefHeight(30);
//            } else if (n.equals("Json")) {
//                region.getChildrenUnmodifiable().get(0).setVisible(false);
//                region.getChildrenUnmodifiable().get(1).setVisible(true);
//                region.setPrefHeight(80);
//            }
//        });
    }

    public void initData(Map<String, MethodData> dataMap) {
        this.editDataMap = dataMap;
        if (dataMap == null) return;
        for (Map.Entry<String, ParamCell> entry : paramCellMap.entrySet()) {
            String key = entry.getKey();
            ParamCell paramCell = entry.getValue();
            MethodData methodData = dataMap.get(key);
            if (methodData != null) {
                paramCell.setData(methodData.getData());
                paramCell.setConvert(methodData.getConvert());
            }
//            paramCell.getCompareType().getSelectionModel().select(ConvertFactory.getInstance().get(methodData.getConvert()));
        }
        MethodData testName = dataMap.get("testName");
        testNameTextField.setText(testName.getData());
    }
}
