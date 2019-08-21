package ldh.common.testui.controller;

import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import ldh.common.testui.cell.ObjectListCell;
import ldh.common.testui.cell.ObjectStringConverter;
import ldh.common.testui.cell.ObjectTableCellFactory;
import ldh.common.testui.component.ParamForm;
import ldh.common.testui.constant.BeanVarType;
import ldh.common.testui.constant.InstanceClassType;
import ldh.common.testui.constant.ParamCategory;
import ldh.common.testui.dao.BeanCheckDao;
import ldh.common.testui.dao.BeanVarDao;
import ldh.common.testui.dao.TestMethodDao;
import ldh.common.testui.dao.TestMethodDataDao;
import ldh.common.testui.handle.RunTreeItem;
import ldh.common.testui.model.*;
import ldh.common.testui.util.*;
import ldh.common.testui.vo.MethodData;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by ldh on 2018/3/15.
 */
public class BeanVarController extends BaseController implements Initializable{

    @FXML private ComboBox<String> classNameComboBox;
    @FXML private StackPane stackPane;
    @FXML private TextField beanVarName;
    @FXML private ComboBox<BeanVarType> typeComboBox;
    @FXML private ComboBox<ParamModel> packageName;
    @FXML private ComboBox<ParamModel> packageName2;
    @FXML private ComboBox<ParamModel> databaseName;
    @FXML private ComboBox<Class> className;
    @FXML private ComboBox<Class> className2;
    @FXML private ComboBox<Method> methodBox;
    @FXML private ComboBox<InstanceClassType> instanceType;

    @FXML private ComboBox<ParamModel> packageName3;
    @FXML private ComboBox<Class> className3;
    @FXML private ComboBox<InstanceClassType> instanceType3;

    @FXML private GridPane methodGridPane;
    @FXML private GridPane sqlGridPane;
    @FXML private GridPane clazzGridPane;

    @FXML private TableView<BeanVar> beanVarTableView;
    @FXML private TextArea sqlText;
    @FXML private TextField argsText;
    @FXML private TextField params;

    @FXML private TableColumn<BeanVar, Method> methodColumn;
    @FXML private TableColumn<BeanVar, String> classNameColumn;

    @FXML private ScrollPane paramPane;
    @FXML private ScrollPane listPane;

    private BeanVar editBeanVar = null;

    @Override
    public void setTreeItem(TreeItem<TreeNode> treeItem) {
        this.treeItem = treeItem;
        Set<ParamModel> paramModelSet = getParamModels(ParamCategory.Method_Package);
        paramModelSet.add(buildLangParamModel("基础类型"));
        packageName.getItems().addAll(paramModelSet);
        packageName2.getItems().addAll(paramModelSet);
        packageName3.getItems().addAll(paramModelSet);

        Set<ParamModel> paramDatabaseSet = getParamModels(ParamCategory.Database);
        databaseName.getItems().addAll(paramDatabaseSet);

        loadData();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        methodColumn.setCellFactory(new ObjectTableCellFactory<>(method->MethodUtil.buildMethodName(method)));
        classNameColumn.setCellFactory(new ObjectTableCellFactory<>(className->MethodUtil.simpleClassName(className)));

        typeComboBox.setCellFactory(new ObjectListCell<>(beanVarType -> beanVarType.getDesc()));
        typeComboBox.setConverter(new ObjectStringConverter<>(beanVarType->beanVarType.getDesc()));
        typeComboBox.getItems().addAll(BeanVarType.values());
        typeComboBox.getSelectionModel().select(BeanVarType.Sql);
        typeComboBox.getSelectionModel().selectedItemProperty().addListener((b, o, n)-> {
            BeanVarType type = typeComboBox.getSelectionModel().getSelectedItem();
            if (type == null) return;
            if (type == BeanVarType.Method) {
                RegionUtil.show(methodGridPane, sqlGridPane, clazzGridPane);
            } else if(type == BeanVarType.Sql){
                RegionUtil.show(sqlGridPane, methodGridPane, clazzGridPane);
            } else if (type == BeanVarType.Clazz){
                RegionUtil.show(clazzGridPane, sqlGridPane, methodGridPane);
            }
        });
        packageName.setCellFactory(new ObjectListCell<>(paramModel -> paramModel.getName()));
        packageName.setConverter(new ObjectStringConverter<ParamModel>((paramModel)->paramModel.getName()));
        packageName.getSelectionModel().selectedItemProperty().addListener((b,o,n)-> initClassName(className, n));
        className.setCellFactory(new ObjectListCell<>(clazz->clazz.getSimpleName()));
        className.setConverter(new ObjectStringConverter<>(clazz->clazz.getSimpleName()));
//        className.getSelectionModel().selectedItemProperty().addListener((b, o, n)-> initMethodName(methodName, n));

        packageName2.setCellFactory(new ObjectListCell<>(paramModel -> paramModel.getName()));
        packageName2.setConverter(new ObjectStringConverter<ParamModel>((paramModel)->paramModel.getName()));
        packageName2.getSelectionModel().selectedItemProperty().addListener((b,o,n)-> initClassName(className2, n));
        className2.setCellFactory(new ObjectListCell<>(clazz->clazz.getSimpleName()));
        className2.setConverter(new ObjectStringConverter<>(clazz->clazz.getSimpleName()));
        className2.getSelectionModel().selectedItemProperty().addListener((b, o, n)-> initMethodName(n));

        instanceType.setCellFactory(new ObjectListCell<>(instanceClassType->instanceClassType.getDesc()));
        instanceType.setConverter(new ObjectStringConverter<>(instanceClassType->instanceClassType.getDesc()));
        instanceType.getItems().addAll(InstanceClassType.values());

        methodBox.setCellFactory(new ObjectListCell<>(method->MethodUtil.buildMethodName(method)));
        methodBox.setConverter(new ObjectStringConverter<>(method->MethodUtil.buildMethodName(method)));

        databaseName.setCellFactory(new ObjectListCell<>(paramModel -> paramModel.getName()));
        databaseName.setConverter(new ObjectStringConverter<ParamModel>((paramModel)->paramModel.getName()));

        packageName3.setCellFactory(new ObjectListCell<>(paramModel -> paramModel.getName()));
        packageName3.setConverter(new ObjectStringConverter<ParamModel>((paramModel)->paramModel.getName()));
        packageName3.getSelectionModel().selectedItemProperty().addListener((b,o,n)-> initClassName(className3, n));
        className3.setCellFactory(new ObjectListCell<>(clazz->clazz.getSimpleName()));
        className3.setConverter(new ObjectStringConverter<>(clazz->clazz.getSimpleName()));
        className3.getSelectionModel().selectedItemProperty().addListener((b, o, n)-> initMethodName(n));
        instanceType3.setCellFactory(new ObjectListCell<>(instanceClassType->instanceClassType.getDesc()));
        instanceType3.setConverter(new ObjectStringConverter<>(instanceClassType->instanceClassType.getDesc()));
        instanceType3.getItems().addAll(InstanceClassType.values());

        initContextMenu();
    }

    public void addBeanVar(ActionEvent actionEvent) throws Exception {
        BeanVar beanVar = new BeanVar();
        beanVar.setType(BeanVarType.Sql);
        initForm(beanVar);

        UiUtil.transitionPane(listPane, paramPane, null);
    }

    public void upData() {
        BeanVar selectedRow = beanVarTableView.getSelectionModel().getSelectedItem();
        if (selectedRow == null) {
            DialogUtil.alert("请选择一行", Alert.AlertType.ERROR);
            return;
        }
        if (beanVarTableView.getItems().size() <= 1) {
            DialogUtil.alert("不需要移动", Alert.AlertType.ERROR);
            return;
        }
        Integer currentIndex = selectedRow.getIndex();
        BeanVar preRow = preRow(currentIndex);
        if (preRow == null) return;
        int preIndex = preRow.getIndex();
        selectedRow.setIndex(preIndex);
        preRow.setIndex(currentIndex);
        try {
            BeanVarDao.save(selectedRow);
            BeanVarDao.save(preRow);
            int ci = beanVarTableView.getItems().indexOf(selectedRow);
            int pi = beanVarTableView.getItems().indexOf(preRow);
            beanVarTableView.getItems().remove(selectedRow);
            beanVarTableView.getItems().add(pi, selectedRow);
            beanVarTableView.getSelectionModel().select(pi);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downData() {
        BeanVar selectedRow = beanVarTableView.getSelectionModel().getSelectedItem();
        if (selectedRow == null) {
            DialogUtil.alert("请选择一行", Alert.AlertType.ERROR);
            return;
        }
        if (beanVarTableView.getItems().size() <= 1) {
            DialogUtil.alert("不需要移动", Alert.AlertType.ERROR);
            return;
        }
        Integer currentIndex = selectedRow.getIndex();
        BeanVar nextRow = nextRow(currentIndex);
        if (nextRow == null) return;
        int nextIndex = nextRow.getIndex();
        selectedRow.setIndex(nextIndex);
        nextRow.setIndex(currentIndex);
        try {
            BeanVarDao.save(selectedRow);
            BeanVarDao.save(nextRow);
            int ci = beanVarTableView.getItems().indexOf(selectedRow);
            int pi = beanVarTableView.getItems().indexOf(nextRow);
            beanVarTableView.getItems().remove(selectedRow);
            beanVarTableView.getItems().add(pi, selectedRow);
            beanVarTableView.getSelectionModel().select(pi);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveBeanVar(ActionEvent actionEvent) {
        try {
            BeanVar beanVar = buildBeanVar();
            beanVar.setIndex(getMaxIndex() + 1);
            if (editBeanVar != null) {
                beanVar.setId(editBeanVar.getId());
                beanVar.setIndex(editBeanVar.getIndex());
            }
            BeanVarDao.save(beanVar);
            if (editBeanVar != null) {
//                DataUtil.removeBeanVar(treeItem, id);
//                DataUtil.getAllDataForBeanVars(treeItem).add(beanVar);
                beanVarTableView.getItems().clear();
                beanVarTableView.getItems().addAll(DataUtil.getAllDataForBeanVars(treeItem));
            } else {
//                DataUtil.getAllDataForBeanVars(treeItem).add(beanVar);
                beanVarTableView.getItems().add(beanVar);
            }
            editBeanVar = null;
            UiUtil.transitionPane(paramPane, listPane, null);
        } catch (Exception e) {
            editBeanVar = null;
            e.printStackTrace();
        }
    }

    private BeanVar buildBeanVar() {
        BeanVar beanVar = new BeanVar();
        beanVar.setName(beanVarName.getText().trim());
        BeanVarType type = typeComboBox.getSelectionModel().getSelectedItem();
        if (type == BeanVarType.Sql) {
            beanVar.setSql(sqlText.getText().trim());
            beanVar.setArgs(argsText.getText() == null ? "" : argsText.getText().trim());
            beanVar.setDatabaseParamId(databaseName.getSelectionModel().getSelectedItem().getId());
            if(packageName.getSelectionModel().getSelectedItem() != null) {
                beanVar.setPackageParamId(packageName.getSelectionModel().getSelectedItem().getId());
                beanVar.setClassName(className.getSelectionModel().getSelectedItem().getName());
            }
        } else if (type == BeanVarType.Method) {
            beanVar.setPackageParamId(packageName2.getSelectionModel().getSelectedItem().getId());
            beanVar.setClassName(className2.getSelectionModel().getSelectedItem().getName());
            beanVar.setInstanceClassType(instanceType.getSelectionModel().getSelectedItem());
            beanVar.setMethod(methodBox.getSelectionModel().getSelectedItem());
            beanVar.setArgs(params.getText() == null ? "" : params.getText().trim());
        } else if (type == BeanVarType.Clazz) {
            beanVar.setPackageParamId(packageName3.getSelectionModel().getSelectedItem().getId());
            beanVar.setClassName(className3.getSelectionModel().getSelectedItem().getName());
            beanVar.setInstanceClassType(instanceType.getSelectionModel().getSelectedItem());
        }
        beanVar.setType(type);
        beanVar.setTreeNodeId(treeItem.getValue().getId());
        check(beanVar);
        return beanVar;
    }

    public void returnListPane(ActionEvent actionEvent) {
        UiUtil.transitionPane(paramPane, listPane, null);
    }

    @FXML
    private void testSqlAct() throws ClassNotFoundException {
        BeanVar beanVar = buildBeanVar();
        String sql = beanVar.getSql();
        String args = beanVar.getArgs();
        Class clazz = Class.forName(beanVar.getClassName());

    }

    @FXML
    private void testMethodAct() {

    }

    private void initContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem edit = new MenuItem("编辑");
        edit.setOnAction(this::editData);
        MenuItem remove = new MenuItem("删除");
        remove.setOnAction(this::removeData);
        contextMenu.getItems().addAll(edit, remove);
        beanVarTableView.setContextMenu(contextMenu);
    }

    public void removeData(ActionEvent actionEvent) {
        BeanVar beanVar = beanVarTableView.getSelectionModel().getSelectedItem();
        if (beanVar == null) return;
        try {
            BeanVarDao.delete(beanVar.getId());
            beanVarTableView.getItems().remove(beanVar);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void editData(ActionEvent actionEvent) {
        BeanVar beanVar = beanVarTableView.getSelectionModel().getSelectedItem();
        if (beanVar == null) return;
        initForm(beanVar);
        editBeanVar = beanVar;
        UiUtil.transitionPane(listPane, paramPane, null);
    }

    private void initForm(BeanVar beanVar) {
        beanVarName.setText(beanVar.getName());
        typeComboBox.getSelectionModel().select(beanVar.getType());
        if (beanVar.getType() == BeanVarType.Sql) {
            RegionUtil.show(sqlGridPane, clazzGridPane, methodGridPane);
            sqlText.setText(beanVar.getSql());
            argsText.setText(beanVar.getArgs());
            databaseName.getSelectionModel().select(DataUtil.getById(beanVar.getDatabaseParamId(), "请选择数据库"));
            packageName.getSelectionModel().select(DataUtil.getById(beanVar.getPackageParamId(), "基础类型"));
            className.getSelectionModel().select(MethodUtil.forClass(beanVar.getClassName()));
        } else if (beanVar.getType() == BeanVarType.Method){
            RegionUtil.show(methodGridPane, sqlGridPane, clazzGridPane);
            packageName2.getSelectionModel().select(DataUtil.getById(beanVar.getPackageParamId(), "基础类型"));
            className2.getSelectionModel().select(MethodUtil.forClass(beanVar.getClassName()));
            instanceType.getSelectionModel().select(beanVar.getInstanceClassType());
            methodBox.getSelectionModel().select(beanVar.getMethod());
            params.setText(beanVar.getArgs());
        } else if (beanVar.getType() == BeanVarType.Clazz){
            RegionUtil.show(clazzGridPane, sqlGridPane, methodGridPane);
            packageName3.getSelectionModel().select(DataUtil.getById(beanVar.getPackageParamId(), "基础类型"));
            className3.getSelectionModel().select(MethodUtil.forClass(beanVar.getClassName()));
            instanceType3.getSelectionModel().select(beanVar.getInstanceClassType());
        }
    }

    private void initClassName(ComboBox<Class> classNameComboBox2, ParamModel paramModel) {
        classNameComboBox2.getItems().clear();
        try {
            if (paramModel == null) return;
            List<Class> classList = null;
            if (paramModel.getName().equals(buildLangParamModel("基础类型").getName())) {
                classList = Arrays.asList(Integer.class, Short.class, Byte.class, BigDecimal.class, Float.class, Double.class, Double.class, Long.class, String.class);
            } else {
                classList = FileUtil.searchClass(paramModel.getValue());
            }
            for (Class clazz : classList) {
                classNameComboBox2.getItems().add(clazz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initMethodName(Class clazz) {
        methodBox.getItems().clear();
        if (clazz == null) return;
        try {
            methodBox.getItems().addAll(MethodUtil.getMethods(clazz));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        new Thread(new Task<Void>(){
            @Override
            protected Void call() throws Exception {
                try {
                    List<BeanVar> beanVars = BeanVarDao.getByTreeNodeId(treeItem.getValue().getId());
                    Platform.runLater(()->{
                        beanVarTableView.getItems().clear();
                        beanVarTableView.getItems().addAll(beanVars);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        }).start();
    }

    private boolean check(BeanVar beanVar) {
        String str = "[a-zA-Z_$][a-zA-Z0-9_$]*";
        if (StringUtils.isEmpty(beanVar.getName()) || !Pattern.matches(str, beanVar.getName())) {
            DialogUtil.alert("变量名称不能为空或不符合要求", Alert.AlertType.ERROR);
            return false;
        }
        if (beanVar.getType() == BeanVarType.Sql) {
            if (beanVar.getDatabaseParamId() == null || StringUtils.isEmpty(beanVar.getSql())) {
                DialogUtil.alert("参数不对", Alert.AlertType.ERROR);
                return false;
            }
        } else if (beanVar.getType() == BeanVarType.Method) {
            if (beanVar.getPackageParamId() == null || StringUtils.isEmpty(beanVar.getClassName()) || beanVar.getInstanceClassType() == null) {
                DialogUtil.alert("参数不对", Alert.AlertType.ERROR);
                return false;
            }
        } else {
            throw new RuntimeException("不支持这种类型");
        }
        return true;
    }

    public static ParamModel buildLangParamModel(String name) {
        ParamModel pm = new ParamModel();
        pm.setName(name);
        pm.setId(-1);
        return pm;
    }

    private BeanVar preRow(int index) {
        BeanVar preRow = null;
        for (BeanVar rowData : beanVarTableView.getItems()) {
            int currentIndex = rowData.getIndex();
            if (currentIndex < index) {
                preRow = rowData;
                continue;
            } else if (currentIndex >= index) {
                break;
            }
        }
        return preRow;
    }

    private BeanVar nextRow(int index) {
        for (BeanVar rowData : beanVarTableView.getItems()) {
            int currentIndex = rowData.getIndex();
            if (currentIndex < index) continue;
            if (currentIndex == index) {
                continue;
            }
            return rowData;
        }
        return null;
    }

    private Integer getMaxIndex() {
        int maxIndex = 0;
        for (BeanVar rowData : beanVarTableView.getItems()) {
            int currentIndex = rowData.getIndex();
            if (currentIndex > maxIndex) continue;
            maxIndex = currentIndex;
        }
        return maxIndex;
    }
}
