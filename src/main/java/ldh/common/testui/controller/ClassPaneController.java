package ldh.common.testui.controller;

import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ldh.common.testui.cell.ObjectListCell;
import ldh.common.testui.cell.ObjectStringConverter;
import ldh.common.testui.component.ParamForm;
import ldh.common.testui.constant.InstanceClassType;
import ldh.common.testui.constant.ParamCategory;
import ldh.common.testui.dao.TestMethodDao;
import ldh.common.testui.dao.TestMethodDataDao;
import ldh.common.testui.handle.RunTreeItem;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TestMethod;
import ldh.common.testui.model.TestMethodData;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.*;
import ldh.common.testui.vo.MethodData;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ldh on 2018/3/15.
 */
public class ClassPaneController extends BaseController implements Initializable {

    @FXML private ComboBox<String> classNameComboBox;
    @FXML private ComboBox<Method> methodBox;
    @FXML private TableView<Map<String, MethodData>> paramTable;
    @FXML private ComboBox<ParamModel> methodPackageComboBox;
    @FXML private ScrollPane paramPane;
    @FXML private ScrollPane tablePane;
    @FXML private ComboBox<InstanceClassType> instanceClassBox;

    private volatile TestMethod testMethod;
    private ParamForm paramForm = null;

    @Override
    public void setTreeItem(TreeItem<TreeNode> treeItem) {
        this.treeItem = treeItem;
        Set<ParamModel> paramModelSet = getParamModels(ParamCategory.Method_Package);
        methodPackageComboBox.getItems().addAll(paramModelSet);

        new Thread(new Task<Void>(){

            @Override
            protected Void call() throws Exception {
                List<TestMethod> testMethods = TestMethodDao.getByTreeNodeId(treeItem.getValue().getId());
                if (testMethods.size() < 1) return null;
                testMethod = testMethods.get(0);
                ParamModel paramModel = DataUtil.getById(testMethod.getParamId());
                Platform.runLater(()->{
                    methodPackageComboBox.getSelectionModel().select(paramModel);
                    methodPackageComboBox.setDisable(true);
                    instanceClassBox.setDisable(true);
                    instanceClassBox.getSelectionModel().select(InstanceClassType.valueOf(testMethod.getInstanceClassName()));
                });

                Platform.runLater(()->{
                    initClassNameData();
                    classNameComboBox.getSelectionModel().select(testMethod.getClassName());
                    classNameComboBox.setDisable(true);
                });
                Platform.runLater(()->{
                    initMethodNameData(testMethod.getClassName());
                    methodBox.getSelectionModel().select(MethodUtil.findMethodByMethodName(testMethod));
                    methodBox.setDisable(true);
                });

                List<TestMethodData> datas = TestMethodDataDao.getByTestMethodId(testMethod.getId());
                List<Map<String, MethodData>> values = new ArrayList<>();
                for (TestMethodData testMethodData : datas) {
                    Map<String, MethodData> data = JsonUtil.toObject(testMethodData.getData(), new TypeToken<Map<String, MethodData>>(){}.getType());
                    data.values().stream().forEach(d->d.setId(testMethodData.getId()));
                    values.add(data);
                }
                Platform.runLater(()->{
                    paramTable.getItems().addAll(values);
                });
                return null;
            }
        }).start();
    }

    @FXML void runCase() throws Exception {
        try {
            Object bean = RunTreeItem.createBean(testMethod);
            ObservableList<Map<String, MethodData>> datas = paramTable.getItems();

            LogUtil.log("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
            LogUtil.log(String.format("测试类：%s, 测试方法：%s", testMethod.getClassName(), testMethod.getMethodName()));
            List<ParamModel> allParamModel = RunTreeItem.getAllParamModel(treeItem);
//            treeItem.getValue().put("bean", bean);
            new Thread(new Task<Void>() {

                @Override
                protected Void call() throws Exception {
                    int i = 0;
                    try {
                        Map<String, Object> paramMap = TreeUtil.buildParamMap(treeItem);
                        Map<String, Object> childParamMap = TreeUtil.getParamMap(treeItem, paramMap);
                        paramMap.putAll(childParamMap);

                        for (Map<String, MethodData> data : datas) {
                            try {
                                Map<String, Object> dataMap = new HashMap<>();
                                for (Map.Entry<String, MethodData> entry : data.entrySet()) {
                                    dataMap.put(entry.getKey(), entry.getValue().getData());
                                }
                                LogUtil.log(String.format("\t检查第%s个测试数据：%s", ++i, JsonUtil.toSimpleJson(dataMap)));
                                runData(bean, data, allParamModel, paramMap);
                            } catch (Exception e) {
                                LogUtil.log(e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            }).start();

        } catch (Exception e) {
//            treeItem.getValue().clear();
            e.printStackTrace();
            LogUtil.log(e.getMessage());
        }

    }

    @FXML public void refresh() {
        setTreeItem(treeItem);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("init111");
        methodBox.setCellFactory(new ObjectListCell<Method>((method)->MethodUtil.buildMethodName(method)));
        methodBox.setConverter(new ObjectStringConverter<Method>((method)->MethodUtil.buildMethodName(method)));
        methodBox.getSelectionModel().selectedItemProperty().addListener((b, o, n)->{
            try {
                Method method = methodBox.getSelectionModel().getSelectedItem();
                if (method == null) return;
                paramTable.getItems().clear();
                paramTable.getColumns().clear();
                TableColumn<Map<String, MethodData>, MethodData> testNameColumn = TableMapUtil.buildNameColumns("testName", "测试名称");
                TableColumn<Map<String, MethodData>, MethodData> beanSetColumn = TableMapUtil.buildBeanSetColumns(getTestClass());
                TableColumn<Map<String, MethodData>, MethodData> paramColumn = TableMapUtil.buildParamColumns(method);
                TableColumn<Map<String, MethodData>, MethodData> exceptionColumn = TableMapUtil.buildExceptionColumns(method);
                TableColumn<Map<String, MethodData>, MethodData> resultColumn = TableMapUtil.buildResultColumns(method);
                TableColumn<Map<String, MethodData>, MethodData> beanGetColumn = TableMapUtil.buildBeanGetColumns(getTestClass());
                TableColumn<Map<String, MethodData>, MethodData> varNameColumn = null;

                for (TableColumn<Map<String, MethodData>, MethodData> tableColumn : Arrays.asList(testNameColumn, beanSetColumn, paramColumn, exceptionColumn, resultColumn, varNameColumn, beanGetColumn)) {
                    if (tableColumn != null) {
                        paramTable.getColumns().add(tableColumn);
                    }
                }
                paramForm = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        methodPackageComboBox.setCellFactory(new ObjectListCell<ParamModel>((paramModel -> paramModel.getName())));
        methodPackageComboBox.setConverter(new ObjectStringConverter<ParamModel>((paramModel -> paramModel.getName())));
        methodPackageComboBox.getSelectionModel().selectedItemProperty().addListener((b, o, n)->{
            initClassNameData();
        });

        classNameComboBox.getSelectionModel().selectedItemProperty().addListener((b, o, n)->{
            initMethodNameData(n);
        });

        initContextMenu();

        instanceClassBox.getItems().addAll(InstanceClassType.values());
        instanceClassBox.setCellFactory(new ObjectListCell<>(type-> type.getDesc()));
        instanceClassBox.setConverter(new ObjectStringConverter<>((type -> type.getDesc())));
        instanceClassBox.getSelectionModel().select(InstanceClassType.Reflect);
    }

    public void addParam(ActionEvent actionEvent) throws Exception {
        if (paramForm == null) {
            paramForm = new ParamForm(getTestClass(), methodBox.getSelectionModel().getSelectedItem(), paramTable);
            paramForm.setClassPaneController(this);
            paramPane.setContent(paramForm);
        }
        paramForm.initData(null);
        UiUtil.transitionPane(tablePane, paramPane, null);
    }

    public void showTablePane() {
        UiUtil.transitionPane(paramPane, tablePane, null);
    }

    public void saveParam(ActionEvent actionEvent) {
        try {
            if (!methodBox.isDisable()) {
                Method method = methodBox.getSelectionModel().getSelectedItem();
                if (method == null) return;
                testMethod = new TestMethod();
                testMethod.setTreeNodeId(treeItem.getValue().getId());
                testMethod.setParamId(methodPackageComboBox.getSelectionModel().getSelectedItem().getId());
                testMethod.setClassName(classNameComboBox.getSelectionModel().getSelectedItem());
                testMethod.setMethodName(MethodUtil.buildMethodName(methodBox.getSelectionModel().getSelectedItem()));
                testMethod.setInstanceClassName(instanceClassBox.getSelectionModel().getSelectedItem().name());
                TestMethodDao.save(testMethod);
                methodBox.setDisable(true);
                methodPackageComboBox.setDisable(true);
                classNameComboBox.setDisable(true);
                instanceClassBox.setDisable(true);
            }
//            TestMethodDataDao.deleteByTestMethodId(testMethod.getId());
            ObservableList<Map<String, MethodData>> datas = paramTable.getItems();
            for (Map<String, MethodData> data : datas) {
                TestMethodData methodData = new TestMethodData();
                if (data.size() < 1) return;
                Integer id = data.values().iterator().next().getId();
                if (id !=null && !id.equals(0)) {
                    methodData.setId(id);
                }
                methodData.setTestMethodId(testMethod.getId());
                MethodData testName = data.get("testName");
                methodData.setTestName(testName.getData());
//                MethodData varName = data.get("varName");
//                methodData.setVarName(varName.getData());
                String json = JsonUtil.toJson(data);
                methodData.setData(json);
                TestMethodDataDao.save(methodData);
                data.values().stream().forEach(d->d.setId(methodData.getId()));
                paramTable.refresh();
                VarFactory.getInstance().clean();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deleteParam(ActionEvent actionEvent) {
        if (testMethod == null) return;
        try {
            TestMethodDataDao.deleteByTestMethodId(testMethod.getId());
            TestMethodDao.delete(testMethod);
            testMethod = null;
            methodPackageComboBox.setDisable(false);
            methodPackageComboBox.getItems().clear();
            methodBox.setDisable(false);
            methodBox.getItems().clear();
            classNameComboBox.setDisable(false);
            classNameComboBox.getItems().clear();
            paramTable.getItems().clear();
            instanceClassBox.setDisable(false);
            this.setTreeItem(treeItem);
            VarFactory.getInstance().clean();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem remove = new MenuItem("删除");
        remove.setOnAction(this::removeData);
        MenuItem run = new MenuItem("运行");
        run.setOnAction(this::runData);
        MenuItem edit = new MenuItem("编辑");
        edit.setOnAction(this::editData);
        contextMenu.getItems().addAll(run, edit, remove);
        paramTable.setContextMenu(contextMenu);
    }

    private void removeData(ActionEvent actionEvent) {
        Map<String, MethodData> data = paramTable.getSelectionModel().getSelectedItem();
        if (data == null) return;
        removeData(data);
//        saveParam(new ActionEvent());
    }

    public void removeData(Map<String, MethodData> data) {
        Integer id = data.values().iterator().next().getId();
        if (id !=null && !id.equals(0)) {
            try {
                TestMethodDataDao.delete(id);
                VarFactory.getInstance().clean();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        paramTable.getItems().remove(data);
        paramTable.refresh();
    }

    private void runData(ActionEvent actionEvent) {
        Map<String, MethodData> data = paramTable.getSelectionModel().getSelectedItem();
        if (data == null) return;
        new Thread(new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                try {
                    Map<String, Object> paramMap = TreeUtil.buildParamMap(treeItem);
                    Map<String, Object> childParamMap = TreeUtil.getParamMap(treeItem, paramMap);
                    paramMap.putAll(childParamMap);

                    List<ParamModel> allParamModel = RunTreeItem.getAllParamModel(treeItem);
                    Object bean = RunTreeItem.createBean(testMethod);
//                    treeItem.getValue().put("bean", bean);
                    runData(bean, data, allParamModel, paramMap);
                } catch (Exception e) {
//                    treeItem.getValue().clear();
                    e.printStackTrace();
                }
                return null;
            }
        }).start();
    }

    private void editData(ActionEvent actionEvent) {
        Map<String, MethodData> data = paramTable.getSelectionModel().getSelectedItem();
        if (data == null) return;
        try {
            if (paramForm == null) {
                paramForm = new ParamForm(getTestClass(), methodBox.getSelectionModel().getSelectedItem(), paramTable);
                paramForm.setClassPaneController(this);
                paramPane.setContent(paramForm);
            }
            paramForm.initData(data);
            UiUtil.transitionPane(tablePane, paramPane, (Void)->paramForm.initData(data));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runData(Object bean, Map<String, MethodData> data, List<ParamModel> allParamModel, Map<String, Object> paramMap) {
        TestMethodData methodData = new TestMethodData();
        methodData.setTestMethodId(testMethod.getId());
        String json = JsonUtil.toJson(data);
        methodData.setData(json);
        if (data.size() < 1) return;
        methodData.setId(data.values().iterator().next().getId());
        RunTreeItem.runTestMethodData(treeItem, bean, testMethod, methodData,  paramMap);
        paramTable.refresh();
    }

    private Class getTestClass() throws Exception {
        String className = classNameComboBox.getSelectionModel().getSelectedItem();
        return Class.forName(className);
    }

    private void initClassNameData() {
        classNameComboBox.getItems().clear();
        ParamModel paramModel = methodPackageComboBox.getSelectionModel().getSelectedItem();
        if (paramModel != null) {
            try {
                List<Class> classList = FileUtil.searchClass(paramModel.getValue());
                for (Class clazz : classList) {
                    classNameComboBox.getItems().add(clazz.getName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void initMethodNameData(String n) {
        try {
            methodBox.getItems().clear();
            if (n == null) return;
            Class clazz = Class.forName(n);
            List<Method> methods = MethodUtil.getMethods(clazz);
            methods = methods.stream().filter(method -> !(method.getName().startsWith("get") && method.getParameters().length == 0))
                    .filter(method -> !(method.getName().startsWith("is") && method.getParameters().length == 0))
                    .filter(method-> !(method.getName().startsWith("set") && method.getParameters().length == 1))
                    .collect(Collectors.toList());
            methodBox.getItems().addAll(methods);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
