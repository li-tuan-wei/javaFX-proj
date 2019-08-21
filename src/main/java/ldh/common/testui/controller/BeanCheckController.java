package ldh.common.testui.controller;

import com.google.gson.reflect.TypeToken;
import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import ldh.common.testui.cell.MethodDataColumnCell;
import ldh.common.testui.cell.ObjectStringConverter;
import ldh.common.testui.cell.ObjectTableCellFactory;
import ldh.common.testui.component.MaskTextField;
import ldh.common.testui.constant.BeanType;
import ldh.common.testui.constant.BeanValueType;
import ldh.common.testui.constant.CompareType;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.dao.BeanCheckDao;
import ldh.common.testui.model.BeanCheck;
import ldh.common.testui.model.BeanData;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.*;
import ldh.common.testui.vo.VarModel;
import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BeanCheckController extends BaseController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(BeanCheckController.class.getName());

    @FXML private TextField checkBeanTextField;
    @FXML private ChoiceBox<BeanType> beanTypeBox;
    @FXML private ChoiceBox<CompareType> textCompareTypeChoiceBox;
    @FXML private ChoiceBox<BeanValueType> beanValueTypeChoiceBox;
    @FXML private TableView dataTableView;

    @FXML private Region tablePane;
    @FXML private Region textPane;
    @FXML private TextArea textTextArea;

    @FXML private ScrollPane listPane;
    @FXML private ScrollPane paramPane;
    @FXML private GridPane editPane;
    @FXML private Region beanListControl;

    private BeanData beanData = null;  // json 修改
    private BeanCheck beanCheck = null;
    private Map<String, BeanData> editBeanDataMap = null; // object 修改

    public void setTreeItem(TreeItem<TreeNode> treeItem) {
        super.setTreeItem(treeItem);
        TreeItem<TreeNode> parentTreeItem = treeItem.getParent();
        if (parentTreeItem.getValue().getTreeNodeType() == TreeNodeType.Http) {
            checkBeanTextField.setText("${result}");
        }
        TextFields.bindAutoCompletion(checkBeanTextField, VarFactory.getInstance().asStringList(treeItem));

        loadData();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        checkBeanTextField.setOnAction(e->initTableColumns());
        checkBeanTextField.focusedProperty().addListener((b,o,n)->{
            if (!n) {
//                initTableColumns();
//                loadData();
            }
        });
        beanTypeBox.setConverter(new ObjectStringConverter<>(beanType -> beanType.getDesc()));
        beanTypeBox.getItems().addAll(BeanType.values());

        beanTypeBox.getSelectionModel().selectedItemProperty().addListener((b, o, n)->{
            dataTableView.getColumns().clear();
            textCompareTypeChoiceBox.setVisible(false);
            beanValueTypeChoiceBox.setVisible(false);
            initTableMenu(n);
            initTableColumns();
        });

        textCompareTypeChoiceBox.setConverter(new ObjectStringConverter<>(compareType -> compareType.getDesc()));
        textCompareTypeChoiceBox.getItems().addAll(CompareType.values());

        beanValueTypeChoiceBox.setConverter(new ObjectStringConverter<>(compareType -> compareType.getDesc()));
        beanValueTypeChoiceBox.getItems().addAll(BeanValueType.values());

        beanValueTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener((b, o, n)->{
            if (!beanValueTypeChoiceBox.isVisible()) return;
            initTableColumns();
        });
    }

    private void initTableMenu(BeanType beanType) {
        dataTableView.setContextMenu(null);
        if (beanType == BeanType.EL || beanType == BeanType.Object || beanType == BeanType.Json) {
            ContextMenu contextMenu = new ContextMenu();
            MenuItem menuItem = new MenuItem("复制", RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.COPY), 20, Color.LIGHTBLUE));
            menuItem.setOnAction(e->copyTableRow(beanType));
            contextMenu.getItems().add(menuItem);
            dataTableView.setContextMenu(contextMenu);
        }

    }

    private void copyTableRow(BeanType beanType) {
        Object selectedRow = dataTableView.getSelectionModel().getSelectedItem();
        if (selectedRow == null) return;
        try {
            if (beanType == BeanType.EL) {
                BeanData selectBeanData = (BeanData) selectedRow;
                Set<BeanData> beanDatas = beanCheck.getBeanDatas();
                selectBeanData.setIndex(beanCheck.getNextIndexForBeanData(beanDatas));
                beanDatas.add(selectBeanData);
                beanCheck.setContent(JsonUtil.toJson(beanDatas));
                BeanCheckDao.save(beanCheck);
                loadData();
            } else if (beanType == BeanType.Object) {
                Map<String, BeanData> selectBeanDatas = (Map<String, BeanData>) selectedRow;
                List<Map<String, BeanData>> beanDatas = beanCheck.getBeanDatasForObject();
                int index = beanCheck.getNextIndexForObject(beanDatas);
                selectBeanDatas.values().forEach(beanData1 -> beanData1.setIndex(index));
                beanDatas.add(selectBeanDatas);
                beanCheck.setContent(JsonUtil.toJson(beanDatas));
                BeanCheckDao.save(beanCheck);
                loadData();
            } else if (beanType == BeanType.Json) {
                BeanData selectBeanData = (BeanData) selectedRow;
                Set<BeanData> beanDatas = beanCheck.getBeanDatas();
                selectBeanData.setIndex(beanCheck.getNextIndexForBeanData(beanDatas));
                beanDatas.add(selectBeanData);
                beanCheck.setContent(JsonUtil.toJson(beanDatas));
                BeanCheckDao.save(beanCheck);
                loadData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initObjectTableColumn() {
        dataTableView.getColumns().clear();
        dataTableView.getItems().clear();
        BeanValueType beanValueType = beanValueTypeChoiceBox.getSelectionModel().getSelectedItem();
        if (beanValueType == null) return;
        String checkName = checkBeanTextField.getText();
        if (checkName == null || checkName.trim().equals("")) return;
        String varName = VarUtil.getVarName(checkName);
        if (varName == null) return;
        VarModel varModel = getVarModel();
        if (varModel == null) {
            LOGGER.warning("#找不到要要验证的变量：" + varName);
            return;
        }

        if (beanValueType == BeanValueType.Not_null) {
            if (varModel.isBean() || varModel.isTBean() || varModel.isArray() || varModel.isListOrSet() || varModel.isCommon()) {
                if (ObjectUtil.commonClass().contains(varModel.getBeanClazz())) {
                    TableColumn<Map<String, BeanData>, BeanData> column = new TableColumn<Map<String, BeanData>, BeanData>(varModel.getBeanClazz().getSimpleName());
                    column.setCellValueFactory(new MethodDataColumnCell(varModel.getBeanClazz().getSimpleName()));
                    column.setCellFactory(new ObjectTableCellFactory<>((beanData -> beanData.getExceptedValue())));
                    column.setPrefWidth(150);
                    dataTableView.getColumns().addAll(column);
                } else {
                    Set<Method> methods = MethodUtil.getGetMethods(varModel.getBeanClazz());
                    for (Method method : methods) {
                        TableColumn<Map<String, BeanData>, BeanData> column = new TableColumn<Map<String, BeanData>, BeanData>(method.getReturnType().getSimpleName() + " " + method.getName());
                        column.setCellValueFactory(new MethodDataColumnCell(method.getName()));
                        column.setCellFactory(new ObjectTableCellFactory<>((beanData -> beanData.getExceptedValue())));
                        column.setPrefWidth(150);
                        dataTableView.getColumns().addAll(column);
                    }
                }

            } else if (varModel.isMap() || varModel.isListMap()) {
                TableColumn<Map<String, BeanData>, BeanData> keyColumn = new TableColumn<Map<String, BeanData>, BeanData>(varModel.getKeyClazz().getSimpleName() + " key");
                keyColumn.setCellValueFactory(new MethodDataColumnCell("key"));
                keyColumn.setCellFactory(new ObjectTableCellFactory<>((beanData -> beanData.getExceptedValue())));
                keyColumn.setPrefWidth(150);

                if (ObjectUtil.commonClass().contains(varModel.getBeanClazz())) {
                    TableColumn<Map<String, BeanData>, BeanData> valueColumn = new TableColumn<Map<String, BeanData>, BeanData>(varModel.getBeanClazz().getSimpleName());
                    valueColumn.setCellValueFactory(new MethodDataColumnCell(varModel.getBeanClazz().getSimpleName()));
                    valueColumn.setCellFactory(new ObjectTableCellFactory<>((beanData -> beanData.getExceptedValue())));
                    valueColumn.setPrefWidth(150);

                    dataTableView.getColumns().addAll(keyColumn, valueColumn);
                } else {
                    Set<Method> valueMethods = MethodUtil.getGetMethods(varModel.getBeanClazz());
                    TableColumn<Map<String, BeanData>, BeanData> valueColumn = new TableColumn<Map<String, BeanData>, BeanData>("value");
                    for (Method method : valueMethods) {
                        TableColumn<Map<String, BeanData>, BeanData> column = new TableColumn<Map<String, BeanData>, BeanData>(method.getReturnType().getSimpleName() + " " + method.getName());
                        column.setCellValueFactory(new MethodDataColumnCell(method.getName()));
                        column.setCellFactory(new ObjectTableCellFactory<>((beanData -> beanData.getExceptedValue())));
                        column.setPrefWidth(150);
                        valueColumn.getColumns().add(column);
                    }
                    dataTableView.getColumns().addAll(keyColumn, valueColumn);
                }
            }
        } else if (beanValueType == BeanValueType.Empty) {
            if (!(Map.class.isAssignableFrom(varModel.getClazz()) || Collection.class.isAssignableFrom(varModel.getClazz()) || varModel.getClazz().isArray())) {
                DialogUtil.alert("不是队列，不能选择为空队列", Alert.AlertType.ERROR);
                return;
            }
            TableColumn<Map<String, BeanData>, BeanData> column = new TableColumn<Map<String, BeanData>, BeanData>(varModel.getVarName());
            column.setCellValueFactory(new MethodDataColumnCell(varModel.getVarName()));
            column.setCellFactory(new ObjectTableCellFactory<>((beanData -> beanData.getExceptedValue())));
            column.setPrefWidth(200);
            dataTableView.getColumns().addAll(column);

            try {
                if (beanCheck == null) {
                    BeanCheck tmp = new BeanCheck();
                    tmp.setCheckName(checkName);
                    tmp.setTreeNodeId(treeItem.getValue().getId());
                    tmp.setBeanType(beanTypeBox.getSelectionModel().getSelectedItem());
                    tmp.setOtherInfo(buildOtherInfo());
                    List<Map<String, BeanData>> list = new ArrayList();
                    Map<String, BeanData> dataMap = new HashMap();
                    BeanData beanData = new BeanData();
                    beanData.setExceptedValue("0");
                    beanData.setIndex(1);
                    beanData.setCompareType(CompareType.Equal);
                    beanData.setClassType(varModel.getClazz().getName());

                    dataMap.put(varModel.getVarName(), beanData);
                    list.add(dataMap);
                    tmp.setContent(JsonUtil.toJson(list));
                    BeanCheckDao.save(tmp);
                    loadData();
//                    dataTableView.getItems().add(dataMap);
                } else {
                    List<Map<String, BeanData>> list = beanCheck.getBeanDatasForObject();
                    if (list.size() == 0) {
                        Map<String, BeanData> dataMap = new HashMap();
                        BeanData beanData = new BeanData();
                        beanData.setExceptedValue("0");
                        beanData.setIndex(1);
                        beanData.setCompareType(CompareType.Equal);
                        beanData.setClassType(varModel.getClazz().getName());

                        dataMap.put(varModel.getVarName(), beanData);
                        list.add(dataMap);
                        beanCheck.setContent(JsonUtil.toJson(list));
                        BeanCheckDao.save(beanCheck);
                        loadData();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (beanValueType == BeanValueType.Null) {
            if (varModel.getClazz().isPrimitive()) {
                DialogUtil.alert("是基本类型，必须有值", Alert.AlertType.ERROR);
                return;
            }
            TableColumn<Map<String, BeanData>, BeanData> column = new TableColumn<Map<String, BeanData>, BeanData>(varModel.getVarName());
            column.setCellValueFactory(new MethodDataColumnCell(varModel.getVarName()));
            column.setCellFactory(new ObjectTableCellFactory<>((beanData -> beanData.getExceptedValue())));
            column.setPrefWidth(200);
            dataTableView.getColumns().addAll(column);

            try {
                if (beanCheck == null) {
                    BeanCheck tmp = new BeanCheck();
                    tmp.setCheckName(checkName);
                    tmp.setTreeNodeId(treeItem.getValue().getId());
                    tmp.setBeanType(beanTypeBox.getSelectionModel().getSelectedItem());
                    tmp.setOtherInfo(buildOtherInfo());
                    List<Map<String, BeanData>> list = new ArrayList();
                    Map<String, BeanData> dataMap = new HashMap();
                    BeanData beanData = new BeanData();
                    beanData.setIndex(1);
                    beanData.setExceptedValue("Null");
                    beanData.setCompareType(CompareType.Equal);
                    beanData.setClassType(varModel.getClazz().getName());
                    dataMap.put(varModel.getVarName(), beanData);

                    list.add(dataMap);
                    tmp.setContent(JsonUtil.toJson(list));
                    BeanCheckDao.save(tmp);
                    loadData();
//                    dataTableView.getItems().add(dataMap);
                } else {
                    List<Map<String, BeanData>> list = beanCheck.getBeanDatasForObject();
                    if (list.size() == 0) {
                        Map<String, BeanData> dataMap = new HashMap();
                        BeanData beanData = new BeanData();
                        beanData.setIndex(1);
                        beanData.setExceptedValue("Null");
                        beanData.setCompareType(CompareType.Equal);
                        beanData.setClassType(varModel.getClazz().getName());
                        dataMap.put(varModel.getVarName(), beanData);

                        dataMap.put(varModel.getVarName(), beanData);
                        list.add(dataMap);
                        beanCheck.setContent(JsonUtil.toJson(list));
                        BeanCheckDao.save(beanCheck);
                        loadData();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Map<String, BeanData> dataMap = new HashMap();
            BeanData beanData = new BeanData();
            beanData.setIndex(1);
            beanData.setExceptedValue("Null");
            beanData.setCompareType(CompareType.Equal);
            beanData.setClassType(varModel.getClazz().getName());
            dataMap.put(varModel.getVarName(), beanData);
            dataTableView.getItems().add(dataMap);
        }
    }

    private void initJsonTableColumn() {
        if (dataTableView.getColumns().size() > 0) return;
        TableColumn<BeanData, String> checkName = new TableColumn<BeanData, String>("Json路径");
        checkName.setCellValueFactory(new PropertyValueFactory("checkName"));
//        checkName.setCellFactory(new ObjectTableCellFactory<>((name -> name)));
        checkName.setPrefWidth(300);

//        TableColumn<BeanData, String> secondCheckName = new TableColumn<BeanData, String>("第二Json路径");
//        secondCheckName.setCellValueFactory(new PropertyValueFactory("secondCheckName"));
////        checkName.setCellFactory(new ObjectTableCellFactory<>((name -> name)));
//        secondCheckName.setPrefWidth(100);

        TableColumn<BeanData, String> checkValue = new TableColumn<BeanData, String>("比较值");
        checkValue.setCellValueFactory(new PropertyValueFactory("exceptedValue"));
//        checkValue.setCellFactory(new ObjectTableCellFactory<>((value -> value)));
        checkValue.setPrefWidth(200);

        TableColumn<BeanData, String> checkDesc = new TableColumn<BeanData, String>("说明");
        checkDesc.setCellValueFactory(new PropertyValueFactory("desc"));
//        checkDesc.setCellFactory(new ObjectTableCellFactory<>((desc -> desc)));
        checkDesc.setPrefWidth(200);

        TableColumn<BeanData, String> className = new TableColumn<BeanData, String>("数据类型");
        className.setCellValueFactory(new PropertyValueFactory("classType"));
        className.setCellFactory(new ObjectTableCellFactory<>((classType -> {
            try {
                if (classType == null) return "";
                return Class.forName(classType.toString()).getSimpleName();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        })));
        className.setPrefWidth(100);

        TableColumn<BeanData, CompareType> compareTypeColumn = new TableColumn<BeanData, CompareType>("比较方式");
        compareTypeColumn.setCellValueFactory(new PropertyValueFactory("compareType"));
        compareTypeColumn.setCellFactory(new ObjectTableCellFactory<>((compareType -> {
            if (compareType == null) return "";
            if (compareType instanceof CompareType) {
                CompareType c = (CompareType) compareType;
                return c.getDesc();
            }
            return "";
        })));
        compareTypeColumn.setPrefWidth(100);

        TableColumn<BeanData, Boolean> isSuccessColumn = new TableColumn<BeanData, Boolean>("检查结果");
        isSuccessColumn.setCellValueFactory(new PropertyValueFactory("success"));
        isSuccessColumn.setCellFactory(new ObjectTableCellFactory<>((isSuccess -> {
            if (isSuccess == null) {
                return new Label("未检测");
            }
            return isSuccess ? new Label("成功") : new Label("失败");
        })));
        isSuccessColumn.setPrefWidth(60);

        dataTableView.getColumns().addAll(checkName, checkValue, checkDesc, className, isSuccessColumn);
    }

    private void initELTableColumn() {
        if (dataTableView.getColumns().size() > 0) return;
        TableColumn<BeanData, String> checkName = new TableColumn<BeanData, String>("el表达式");
        checkName.setCellValueFactory(new PropertyValueFactory("checkName"));
//        checkName.setCellFactory(new ObjectTableCellFactory<>((name -> name)));
        checkName.setPrefWidth(350);

        TableColumn<BeanData, String> valueColumn = new TableColumn<BeanData, String>("比较值");
        valueColumn.setCellValueFactory(new PropertyValueFactory("exceptedValue"));
//        checkName.setCellFactory(new ObjectTableCellFactory<>((name -> name)));
        valueColumn.setPrefWidth(200);

        TableColumn<BeanData, String> checkDesc = new TableColumn<BeanData, String>("说明");
        checkDesc.setCellValueFactory(new PropertyValueFactory("desc"));
//        checkDesc.setCellFactory(new ObjectTableCellFactory<>((desc -> desc)));
        checkDesc.setPrefWidth(200);

        TableColumn<BeanData, String> className = new TableColumn<BeanData, String>("数据类型");
        className.setCellValueFactory(new PropertyValueFactory("classType"));
        className.setCellFactory(new ObjectTableCellFactory<>((classType -> {
            try {
                if (classType == null) return "";
                return Class.forName(classType.toString()).getSimpleName();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";
        })));
        className.setPrefWidth(100);

        TableColumn<BeanData, CompareType> compareTypeColumn = new TableColumn<BeanData, CompareType>("比较方式");
        compareTypeColumn.setCellValueFactory(new PropertyValueFactory("compareType"));
        compareTypeColumn.setCellFactory(new ObjectTableCellFactory<>((compareType -> {
            if (compareType == null) return "";
            if (compareType instanceof CompareType) {
                CompareType c = (CompareType) compareType;
                return c.getDesc();
            }
            return "";
        })));
        compareTypeColumn.setPrefWidth(100);

        TableColumn<BeanData, Boolean> isSuccessColumn = new TableColumn<BeanData, Boolean>("检查结果");
        isSuccessColumn.setCellValueFactory(new PropertyValueFactory("success"));
        isSuccessColumn.setCellFactory(new ObjectTableCellFactory<>((isSuccess -> {
            if (isSuccess == null) {
                return new Label("未检测");
            }
            return isSuccess ? new Label("成功") : new Label("失败");
        })));
        isSuccessColumn.setPrefWidth(100);

        dataTableView.getColumns().addAll(checkName, valueColumn, checkDesc, className, compareTypeColumn, isSuccessColumn);
    }

    public void addBeanData(ActionEvent actionEvent) {
        BeanType beanType = beanTypeBox.getSelectionModel().getSelectedItem();
        if (beanType == null) return;
        boolean check = checkBeanCheck();
        if (!check) return;
        if(beanType == BeanType.Object) {
            BeanValueType beanValueType = beanValueTypeChoiceBox.getSelectionModel().getSelectedItem();
            if (beanValueType == null || beanValueType != BeanValueType.Not_null) {
                DialogUtil.alert("请选择非空对象", Alert.AlertType.ERROR);
                return;
            }

            if (dataTableView.getItems().size() > 0) {
                VarModel varModel = getVarModel();
                if (varModel == null) return;
                if (varModel.isBean()) {
                    DialogUtil.alert("不是列表，不能再次添加", Alert.AlertType.ERROR);
                    return;
                }
            }
        }

        boolean isCanEdit = initEditPane(beanType);
        if (!isCanEdit) return;
        beanData = null;
        editBeanDataMap = null;
        UiUtil.transitionPane(listPane, paramPane, (Void)->{

        });
    }

    public void editBeanData(ActionEvent actionEvent) {
        BeanType beanType = beanTypeBox.getSelectionModel().getSelectedItem();
        if (beanType == null) return;
        boolean isCanEdit = initEditPane(beanType);
        if (!isCanEdit) return;
        Object data = dataTableView.getSelectionModel().getSelectedItem();
        if (data == null) return;
        if (beanType == BeanType.Json) {
            beanData = (BeanData) data;
            UiUtil.transitionPane(listPane, paramPane, (Void)->{
                TextField checkNameTextField = (TextField) editPane.lookup("#jsonPath");
//                TextField secondCheckNameTextField = (TextField) editPane.lookup("#secondJsonPath");
                TextField checkValueTextField = (TextField) editPane.lookup("#checkValue");
                TextField descTextField = (TextField) editPane.lookup("#desc");
                ChoiceBox<Class> classChoiceBox = (ChoiceBox<Class>) editPane.lookup("#classType");
                ChoiceBox<CompareType> compareTypeChoiceBox = (ChoiceBox<CompareType>) editPane.lookup("#compareType");

                checkNameTextField.setText(beanData.getCheckName());
//                secondCheckNameTextField.setText(beanData.getSecondCheckName());
                checkValueTextField.setText(beanData.getExceptedValue());
                descTextField.setText(beanData.getDesc());
                if (beanData.getClassType() != null) {
                    try {
                        classChoiceBox.getSelectionModel().select(Class.forName(beanData.getClassType()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (beanData.getClassType() != null) {
                        classChoiceBox.getSelectionModel().select(Class.forName(beanData.getClassType()));
                    }
                    if (beanData.getCompareType() != null) {
                        compareTypeChoiceBox.getSelectionModel().select(beanData.getCompareType());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else if (beanType == BeanType.Object) {
            editBeanDataMap = (Map<String, BeanData>) data;
            UiUtil.transitionPane(listPane, paramPane, (Void)->{
                for(Map.Entry<String, BeanData> entry : editBeanDataMap.entrySet()) {
                    TextField textField = (TextField) editPane.lookup("#" + entry.getKey());
                    TextField descTextField = (TextField) editPane.lookup("#" + entry.getKey() + "_desc");
                    ChoiceBox<Class> classChoiceBox = (ChoiceBox<Class>) editPane.lookup("#" + entry.getKey() + "_classType");
                    ChoiceBox<CompareType> compareTypeChoiceBox = (ChoiceBox<CompareType>) editPane.lookup("#" + entry.getKey() + "_compareType");

                    BeanData beanData = entry.getValue();
                    if (beanData == null) continue;
                    textField.setText(beanData.getExceptedValue());
                    if (descTextField != null) descTextField.setText(beanData.getDesc());
                    if (classChoiceBox != null) classChoiceBox.getSelectionModel().select(MethodUtil.forClass(beanData.getClassType()));
                    if (compareTypeChoiceBox != null) compareTypeChoiceBox.getSelectionModel().select(beanData.getCompareType());
                }
            });
        } else if (beanType == BeanType.EL) {
            beanData = (BeanData) data;
            UiUtil.transitionPane(listPane, paramPane, (Void)->{
                TextField checkNameTextField = (TextField) editPane.lookup("#el");
                TextField valueTextField = (TextField) editPane.lookup("#el_value");
                TextField descTextField = (TextField) editPane.lookup("#el_desc");
                ChoiceBox<Class> classTypeChoiceBox = (ChoiceBox<Class>) editPane.lookup("#el_classType");
                ChoiceBox<CompareType> compareTypeChoiceBox = (ChoiceBox<CompareType>) editPane.lookup("#el_compareType");

                checkNameTextField.setText(beanData.getCheckName());
                valueTextField.setText(beanData.getExceptedValue());
                descTextField.setText(beanData.getDesc());
                if (!StringUtils.isEmpty(beanData.getClassType())) {
                    classTypeChoiceBox.getSelectionModel().select(MethodUtil.forClass(beanData.getClassType()));
                }
                compareTypeChoiceBox.getSelectionModel().select(beanData.getCompareType());

            });
        }

    }

    public void removeData(ActionEvent actionEvent) {
        Object data = dataTableView.getSelectionModel().getSelectedItem();
        if (data == null || beanCheck == null) return;
        BeanType beanType = beanTypeBox.getSelectionModel().getSelectedItem();

        try {
            if (beanType == BeanType.Json || beanType == BeanType.EL) {
                BeanData beanData = (BeanData) data;
                Set<BeanData> beanDataList = beanCheck.getBeanDatas();
                beanDataList.remove(beanData);
                beanCheck.setContent(JsonUtil.toJson(beanDataList));
                BeanCheckDao.save(beanCheck);
                loadData();
            } else if (beanType == BeanType.Object) {
                Map<String, BeanData> map = (Map<String, BeanData>) data;
                List<Map<String, BeanData>> beanDataList = beanCheck.getBeanDatasForObject();
                beanCheck.removeObjectData(beanDataList, map.values().iterator().next().getIndex());
                beanCheck.setContent(JsonUtil.toJson(beanDataList));
                BeanCheckDao.save(beanCheck);
                loadData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upData() {
        Object selectedRow = dataTableView.getSelectionModel().getSelectedItem();
        if (selectedRow == null) {
            DialogUtil.alert("请选择一行", Alert.AlertType.ERROR);
            return;
        }
        if (dataTableView.getItems().size() <= 1) {
            DialogUtil.alert("不需要移动", Alert.AlertType.ERROR);
            return;
        }
        BeanType beanType = beanTypeBox.getValue();
        if (beanType == BeanType.Object) {
            Map<String, BeanData> currentRow = (Map<String, BeanData>) selectedRow;

            int currentIndex = currentRow.values().iterator().next().getIndex();
            Map<String, BeanData> preRow = preRow(currentIndex);
            if (preRow == null) return;
            int preIndex = preRow.values().iterator().next().getIndex();
            currentRow.values().stream().forEach(beanData1 -> beanData1.setIndex(preIndex));
            preRow.values().stream().forEach(beanData1 -> beanData1.setIndex(currentIndex));
            try {
                beanCheck.setContent(JsonUtil.toJson(dataTableView.getItems()));
                BeanCheckDao.save(beanCheck);
                int ci = dataTableView.getItems().indexOf(currentRow);
                int pi = dataTableView.getItems().indexOf(preRow);
                dataTableView.getItems().remove(currentRow);
                dataTableView.getItems().add(pi, currentRow);
                dataTableView.getSelectionModel().select(pi);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (beanType == BeanType.Json || beanType == BeanType.EL) {
            BeanData currentRow = (BeanData) selectedRow;
            int currentIndex = currentRow.getIndex();
            BeanData preRow = preRowForBeanData(currentIndex);
            if (preRow == null) return;
            int preIndex = preRow.getIndex();
            currentRow.setIndex(preIndex);
            preRow.setIndex(currentIndex);
            try {
                beanCheck.setContent(JsonUtil.toJson(dataTableView.getItems()));
                BeanCheckDao.save(beanCheck);
                int ci = dataTableView.getItems().indexOf(currentRow);
                int pi = dataTableView.getItems().indexOf(preRow);
                dataTableView.getItems().remove(currentRow);
                dataTableView.getItems().add(pi, currentRow);
                dataTableView.getSelectionModel().select(pi);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public void downData() {
        Object selectedRow = dataTableView.getSelectionModel().getSelectedItem();
        if (selectedRow == null) {
            DialogUtil.alert("请选择一行", Alert.AlertType.ERROR);
            return;
        }
        if (dataTableView.getItems().size() <= 1) {
            DialogUtil.alert("不需要移动", Alert.AlertType.ERROR);
            return;
        }

        BeanType beanType = beanTypeBox.getValue();
        if (beanType == BeanType.Object) {
            Map<String, BeanData> currentRow = (Map<String, BeanData>) selectedRow;
            int index = currentRow.values().iterator().next().getIndex();
            Map<String, BeanData> nextRow = nextRow(index);
            if (nextRow == null) return;
            int nextIndex = nextRow.values().iterator().next().getIndex();

            currentRow.values().stream().forEach(beanData1 -> beanData1.setIndex(nextIndex));
            nextRow.values().stream().forEach(beanData1 -> beanData1.setIndex(index));

            try {
                beanCheck.setContent(JsonUtil.toJson(dataTableView.getItems()));
                BeanCheckDao.save(beanCheck);
                int ci = dataTableView.getItems().indexOf(currentRow);
                int ni = dataTableView.getItems().indexOf(nextRow);
                dataTableView.getItems().remove(currentRow);
                dataTableView.getItems().add(ni, currentRow);
                dataTableView.getSelectionModel().select(ni);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (beanType == BeanType.Json || beanType == BeanType.EL) {
            BeanData currentRow = (BeanData) selectedRow;
            int currentIndex = currentRow.getIndex();
            BeanData nextRow = nextRowForBeanData(currentIndex);
            if (nextRow == null) return;
            int preIndex = nextRow.getIndex();
            currentRow.setIndex(preIndex);
            nextRow.setIndex(currentIndex);
            try {
                beanCheck.setContent(JsonUtil.toJson(dataTableView.getItems()));
                BeanCheckDao.save(beanCheck);
                int ci = dataTableView.getItems().indexOf(currentRow);
                int pi = dataTableView.getItems().indexOf(nextRow);
                dataTableView.getItems().remove(currentRow);
                dataTableView.getItems().add(pi, currentRow);
                dataTableView.getSelectionModel().select(pi);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void saveText() {
        if (beanCheck == null) return;
        try {
            beanCheck.setCheckName(checkBeanTextField.getText().trim());
            beanCheck.setBeanType(beanTypeBox.getSelectionModel().getSelectedItem());

            String otherInfo = buildOtherInfo();
            beanCheck.setOtherInfo(otherInfo);
            BeanData beanData = new BeanData();
            beanData.setCheckName(beanCheck.getCheckName());
            beanData.setExceptedValue(textTextArea.getText().trim());
            beanCheck.setContent(JsonUtil.toJson(beanData));

            if (StringUtils.isEmpty(beanCheck.getCheckName()) || StringUtils.isEmpty(beanCheck.getContent())) {
                DialogUtil.alert("请按要求填写参数", Alert.AlertType.ERROR);
                return;
            }
            BeanCheckDao.save(beanCheck);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean initEditPane(BeanType beanType) {
        if (beanType == null) return false;
        if (beanType == BeanType.Json) {
            return initJsonPane();
        } else if (beanType == BeanType.Object) {
            return initObjectPane();
        } else if (beanType == BeanType.EL) {
            return initELPane();
        }
        return false;
    }

    private void loadData() {
        Task<Void> task = new Task() {

            @Override
            protected Object call() throws Exception {
                try {
                    BeanCheck beanCheck = BeanCheckDao.getByTreeNodeId(treeItem.getValue().getId());
                    BeanCheckController.this.beanCheck = beanCheck;
                    Platform.runLater(()->init());
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
//        task.setOnSucceeded(e->{
//            TableViewUtil.autoResizeColumns(dataTableView);
//        });
        ThreadUtilFactory.getInstance().submit(task);
    }

    private void init() {
        if (beanCheck == null) return;
        dataTableView.getItems().clear();
        checkBeanTextField.setText(beanCheck.getCheckName());
        beanTypeBox.getSelectionModel().select(beanCheck.getBeanType());

        if (beanCheck.getBeanType() == BeanType.Json) {
            initJsonTableColumn();
            dataTableView.getItems().addAll(beanCheck.getBeanDatas());
        } else if (beanCheck.getBeanType() == BeanType.String) {
            Map<String, Object> otherInfoMap = JsonUtil.toObject(beanCheck.getOtherInfo(), new TypeToken<Map<String, Object>>(){}.getType());
            String compareType = otherInfoMap.get("compareType").toString();
            textCompareTypeChoiceBox.getSelectionModel().select(CompareType.valueOf(compareType));
            if (beanCheck.getContent().startsWith("{")) {
                BeanData beanData = JsonUtil.toObject(beanCheck.getContent(), BeanData.class);
                textTextArea.setText(beanData.getExceptedValue());
            } else {
                textTextArea.setText(beanCheck.getContent());
            }
        } else if (beanCheck.getBeanType() == BeanType.Object) {
            Map<String, Object> otherInfoMap = JsonUtil.toObject(beanCheck.getOtherInfo(), new TypeToken<Map<String, Object>>(){}.getType());
            if (otherInfoMap != null && otherInfoMap.containsKey("beanValueType")) {
                String beanValueType = (String) otherInfoMap.get("beanValueType");
                beanValueTypeChoiceBox.getSelectionModel().select(BeanValueType.valueOf(beanValueType));
            }
            initObjectTableColumn();

            List<Map<String, BeanData>> beanDatas = beanCheck.getBeanDatasForObject();
            dataTableView.getItems().addAll(beanDatas);
        } else if (beanCheck.getBeanType() == BeanType.EL) {
            initELTableColumn();
            dataTableView.getItems().addAll(beanCheck.getBeanDatas());
        }
    }

    private boolean initObjectPane() {
        editPane.getChildren().clear();
        int row = 0;
        ValidationSupport validationSupport = null;

        String checkName = checkBeanTextField.getText();
        if (checkName == null || checkName.trim().equals("")) return false;
        String varName = VarUtil.getVarName(checkName);
        if (varName == null) return false;
        VarModel varModel = VarFactory.getInstance().getCache(treeItem, varName);

        if (ObjectUtil.commonClass().contains(varModel.getBeanClazz())) {
            if (varModel.isListMap() || varModel.isMap()) {
                if (!ObjectUtil.commonClass().contains(varModel.getKeyClazz())) {
                    DialogUtil.alert("不支持key为对象", Alert.AlertType.WARNING);
                    return false;
                }
                addItemForEditPane(varModel.getKeyClazz().getSimpleName() + " key", "key", row++, validationSupport);
                buildCompareInfo("key", varModel.getKeyClazz());
            }

            addItemForEditPane(varModel.getBeanClazz().getSimpleName(), varModel.getBeanClazz().getSimpleName(), row++, validationSupport);
            buildCompareInfo(varModel.getBeanClazz().getSimpleName(), varModel.getBeanClazz());
        } else {
            if (varModel.isListMap() || varModel.isMap()) {
                if (!ObjectUtil.commonClass().contains(varModel.getKeyClazz())) {
                    DialogUtil.alert("不支持key为对象", Alert.AlertType.WARNING);
                    return false;
                }
                addItemForEditPane(varModel.getKeyClazz().getSimpleName() + " key", "key", row++, validationSupport);
                buildCompareInfo("key", varModel.getKeyClazz());
            }

            Set<Method> methods = MethodUtil.getGetMethods(varModel.getBeanClazz());
            for (Method method : methods) {
                String labelText = method.getReturnType().getSimpleName() + " "+ method.getName();
                if (ObjectUtil.commonClass().contains(method.getReturnType())) {
                    addItemForEditPane(labelText, method.getName(), row++, validationSupport);
                    buildCompareInfo(method.getName(), method.getReturnType());
                } else {
                    addBeanCheckItemForEditPane(labelText, method.getName(), varModel.getBeanClazz().getSimpleName() + "_" + method.getName(), row++, validationSupport);
                }
            }
        }

        addButton(row++, validationSupport);
        return true;
    }

    private void buildCompareInfo(String id, Class clazz) {
        ChoiceBox<Class> classTypeChoiceBox = (ChoiceBox<Class>) editPane.lookup("#" + id + "_classType");
        classTypeChoiceBox.getSelectionModel().select(clazz);
        ChoiceBox<CompareType> compareTypeChoiceBox = (ChoiceBox<CompareType>) editPane.lookup("#" + id + "_compareType");
        compareTypeChoiceBox.getSelectionModel().select(CompareType.Equal);
    }

    private boolean initJsonPane() {
        editPane.getChildren().clear();
        int row = 0;
        ValidationSupport validationSupport = new ValidationSupport();
        addItemForEditPane("Json路径", new TextField(), "jsonPath", row++, validationSupport);
//        addItemForEditPane("二级Json路径", new TextField(), "secondJsonPath", row++, null);
        addItemForEditPane("检查值", new TextField(), "checkValue", row++, validationSupport);
        addItemForEditPane("说明", new TextField(), "desc", row++, validationSupport);
        addClassNameItemForEditPane("数据类型", "classType", row++, validationSupport);
        addCompareTypeItemForEditPane("比较方式", "compareType", row++, validationSupport);

        addButton(row++, validationSupport);
        return true;
    }

    private boolean initELPane() {
        editPane.getChildren().clear();
        int row = 0;
        ValidationSupport validationSupport = new ValidationSupport();
        addItemForEditPane("EL表达式", new TextField(), "el", row++, validationSupport);
        addItemForEditPane("比较值", new TextField(), "el_value", row++, validationSupport);
        addItemForEditPane("说明", new TextField(), "el_desc", row++, validationSupport);
        addItemForEditPane("比较类型", buildClassTypeChoiceBox("el_classType"), "el_classType", row++, validationSupport);
        addItemForEditPane("比较方式", buildCompareTypeChoiceBox("el_compareType"), "el_compareType", row++, validationSupport);

        addButton(row++, validationSupport);
        return true;
    }

    private void addItemForEditPane(String labelText, String textFieldId, int row, ValidationSupport validationSupport) {
        Label label = new Label(labelText);
        TextField textField = new TextField();
        textField.setPromptText("请输入验证值");
        textField.setId(textFieldId);

        TextField descTextField = new TextField();
        descTextField.setPromptText("字段描述");
        descTextField.setId(textFieldId + "_desc");

        ChoiceBox<Class> classTypeChoiceBox = new ChoiceBox<>();
        classTypeChoiceBox.setPrefWidth(150);
        classTypeChoiceBox.setConverter(new ObjectStringConverter<>(clazz -> clazz.getSimpleName()));
        classTypeChoiceBox.getItems().addAll(ObjectUtil.commonClass());
        classTypeChoiceBox.setId(textFieldId + "_classType");

        ChoiceBox<CompareType> compareTypeChoiceBox = new ChoiceBox<>();
        compareTypeChoiceBox.setPrefWidth(150);
        compareTypeChoiceBox.setConverter(new ObjectStringConverter<>(compareType -> compareType.getDesc()));
        compareTypeChoiceBox.getItems().addAll(CompareType.values());
        compareTypeChoiceBox.setId(textFieldId + "_compareType");

        if (validationSupport != null) {
            validationSupport.registerValidator(textField, Validator.createEmptyValidator("不能为空"));
        }

        int column = 0;
        editPane.add(label, column++, row);
        editPane.add(textField, column++, row);
        editPane.add(descTextField, column++, row);
        editPane.add(classTypeChoiceBox, column++, row);
        editPane.add(compareTypeChoiceBox, column++, row);
    }

    private void addBeanCheckItemForEditPane(String labelText, String textFieldId, String value, int row, ValidationSupport validationSupport) {
        Label label = new Label(labelText);
        TextField textField = new TextField();
        textField.setText("{{" + value + "}}");
        textField.setId(textFieldId);

        if (validationSupport != null) {
            validationSupport.registerValidator(textField, Validator.createEmptyValidator("不能为空"));
        }

        int column = 0;
        editPane.add(label, column++, row);
        editPane.add(textField, column++, row);
    }

    private void addItemForEditPane(String labelText, Control control, String textFieldId, int row, ValidationSupport validationSupport) {
        Label label = new Label(labelText);
        control.setId(textFieldId);

        if (validationSupport != null) {
            validationSupport.registerValidator(control, Validator.createEmptyValidator("不能为空"));
        }

        int column = 0;
        editPane.add(label, column++, row);
        editPane.add(control, column++, row);
    }

    private void addClassNameItemForEditPane(String labelText, String textFieldId, int row, ValidationSupport validationSupport) {
        Label label = new Label(labelText);

        ChoiceBox<Class> classChoiceBox = new ChoiceBox<>();
        classChoiceBox.setId(textFieldId);
        classChoiceBox.setConverter(new ObjectStringConverter<>(clazz->clazz.getSimpleName()));
        classChoiceBox.getItems().addAll(ObjectUtil.commonClass());

        validationSupport.registerValidator(classChoiceBox, Validator.createEmptyValidator("不能为空"));

        int column = 0;
        editPane.add(label, column++, row);
        editPane.add(classChoiceBox, column++, row);
    }

    private void addCompareTypeItemForEditPane(String labelText, String textFieldId, int row, ValidationSupport validationSupport) {
        Label label = new Label(labelText);

        ChoiceBox<CompareType> classChoiceBox = new ChoiceBox<>();
        classChoiceBox.setId(textFieldId);
        classChoiceBox.setConverter(new ObjectStringConverter<>(compareType->compareType.getDesc()));
        classChoiceBox.getItems().addAll(CompareType.values());
        classChoiceBox.getSelectionModel().select(CompareType.Equal);

        validationSupport.registerValidator(classChoiceBox, Validator.createEmptyValidator("不能为空"));

        int column = 0;
        editPane.add(label, column++, row);
        editPane.add(classChoiceBox, column++, row);
    }

    private void addButton(int row, ValidationSupport validationSupport) {
        Button saveBtn = new JFXButton("保存");
        saveBtn.getStyleClass().addAll("btn, btn-primary");
        saveBtn.setOnAction(e->saveData(validationSupport));

        Button returnBtn = new JFXButton("返回");
        returnBtn.getStyleClass().addAll("btn,btn-info");
        returnBtn.setOnAction(e->{
            UiUtil.transitionPane(paramPane, listPane, null);
        });

        HBox hBox = new HBox(saveBtn, returnBtn);
        hBox.setSpacing(20);
        hBox.setPadding(new Insets(0, 0, 0, 100));
        editPane.add(hBox, 0, row, 2, 1);
    }

    private void saveData(ValidationSupport validationSupport) {
        if (validationSupport != null && validationSupport.isInvalid()) {
            DialogUtil.alert("请按照要求填写:", Alert.AlertType.WARNING);
            return;
        }
        try {
            BeanType beanType = beanTypeBox.getSelectionModel().getSelectedItem();
            if (beanType == BeanType.Json) {
                saveJsonData(beanType);
            } else if (beanType == BeanType.Object) {
                saveObjectData(beanType);
            } else if (beanType == BeanType.EL) {
                saveELData(beanType);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveJsonData(BeanType beanType) throws SQLException {
        Task<Void> task = new Task() {

            @Override
            protected Object call() throws Exception {
                try {
                    TextField checkNameTextField = (TextField) editPane.lookup("#jsonPath");
//                    TextField secondCheckNameTextField = (TextField) editPane.lookup("#secondJsonPath");
                    TextField checkValueTextField = (TextField) editPane.lookup("#checkValue");
                    TextField descTextField = (TextField) editPane.lookup("#desc");
                    ChoiceBox<Class> classNameChoiceBox = (ChoiceBox<Class>) editPane.lookup("#classType");
                    ChoiceBox<CompareType> compareTypeChoiceBox = (ChoiceBox<CompareType>) editPane.lookup("#compareType");

                    BeanCheck tmpBeanCheck = beanCheck == null ? new BeanCheck() : beanCheck;

                    BeanData tmp = beanData;
                    tmp = tmp == null ? new BeanData() : tmp;

                    tmp.setCheckName(checkNameTextField.getText().trim());
//                    tmp.setSecondCheckName(secondCheckNameTextField.getText() == null ? null : secondCheckNameTextField.getText().trim());
                    tmp.setExceptedValue(checkValueTextField.getText().trim());
                    tmp.setDesc(descTextField.getText().trim());
                    tmp.setClassType(classNameChoiceBox.getSelectionModel().getSelectedItem().getName());
                    tmp.setCompareType(compareTypeChoiceBox.getSelectionModel().getSelectedItem());

                    tmpBeanCheck.setCheckName(checkBeanTextField.getText().trim());
                    tmpBeanCheck.setBeanType(beanType);
                    tmpBeanCheck.setColumns("jsonPath,value,desc");
                    tmpBeanCheck.setTreeNodeId(treeItem.getValue().getId());
                    Set<BeanData> beanDataList = tmpBeanCheck.getBeanDatas();

                    if (beanData == null) { //新增
                        tmp.setIndex(tmpBeanCheck.getNextIndexForBeanData(tmpBeanCheck.getBeanDatas()));
                    } else {
                        tmp.setIndex(beanData.getIndex());
                        beanDataList.remove(beanData);
                    }
                    beanDataList.add(tmp);
                    tmpBeanCheck.setContent(JsonUtil.toJson(beanDataList));
                    if (beanCheck != null) {
                        tmpBeanCheck.setId(beanCheck.getId());
                    }
                    BeanCheckDao.save(tmpBeanCheck);
                    loadData();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        };

        task.setOnFailed(e-> UiUtil.transitionPane(paramPane, listPane, null));
        task.setOnSucceeded(e->UiUtil.transitionPane(paramPane, listPane, null));
        ThreadUtilFactory.getInstance().submit(task);
    }

    private void saveObjectData(BeanType beanType) throws SQLException {
        Task<Void> task = new Task() {

            @Override
            protected Object call() throws Exception {
                try {
                    String checkName = checkBeanTextField.getText();
                    if (checkName == null || checkName.trim().equals("")) return null;
                    String varName = VarUtil.getVarName(checkName);
                    if (varName == null) return null;
                    VarModel varModel = VarFactory.getInstance().getCache(treeItem, varName);

                    BeanCheck tmpBeanCheck = beanCheck == null ? new BeanCheck() : beanCheck;

                    Map<String, BeanData> tmp = editBeanDataMap;
                    tmp = tmp == null ? new HashMap<String, BeanData>() : tmp;

                    List<Map<String, BeanData>> datas = tmpBeanCheck.getBeanDatasForObject();

                    String key = null;
                    int index = -1;
                    String columns = null;

                    if (ObjectUtil.commonClass().contains(varModel.getBeanClazz())) { // 普通对象
                        TextField textField = (TextField) editPane.lookup("#" + varModel.getBeanClazz().getSimpleName());
                        TextField descTextField = (TextField) editPane.lookup("#" + varModel.getBeanClazz().getSimpleName() + "_desc");
                        ChoiceBox<Class> classTypeChoiceBox = (ChoiceBox<Class>)editPane.lookup("#" + varModel.getBeanClazz().getSimpleName() + "_classType");
                        ChoiceBox<CompareType> compareTypeChoiceBox = (ChoiceBox<CompareType>)editPane.lookup("#" + varModel.getBeanClazz().getSimpleName() + "_compareType");

                        String value = textField.getText();
                        columns = varModel.getBeanClazz().getSimpleName();
                        if(!StringUtils.isEmpty(value)) {
                            BeanData beanData = new BeanData();
                            beanData.setExceptedValue(value);
                            beanData.setCheckName(varModel.getBeanClazz().getSimpleName());
                            beanData.setDesc(descTextField.getText());
                            Class clazz = classTypeChoiceBox.getSelectionModel().getSelectedItem();
                            beanData.setClassType(clazz == null ? null : clazz.getName());
                            beanData.setCompareType(compareTypeChoiceBox.getSelectionModel().getSelectedItem());

                            if (editBeanDataMap == null) {// 新增
                                beanData.setIndex(tmpBeanCheck.getNextIndexForObject(tmpBeanCheck.getBeanDatasForObject()));
                            } else {  // 更新
                                index = tmp.values().size() == 0 ? 1 : tmp.values().iterator().next().getIndex();
                                beanData.setIndex(index);
                            }
                            tmp.put(varModel.getBeanClazz().getSimpleName(), beanData);
                        }
                        if (ObjectUtil.commonClass().contains(varModel.getKeyClazz())){
                            key = "key";
                        }
                    } else {  // bean 对象
                        Set<Method> methods = null;
                        if (varModel.isBean() || varModel.isTBean() || varModel.isArray() || varModel.isListOrSet()) {
                            methods = MethodUtil.getGetMethods(varModel.getBeanClazz());
                        } else if (varModel.isListMap() || varModel.isMap()) {
                            key = "key";
                            methods = MethodUtil.getGetMethods(varModel.getBeanClazz());
                        }

                        for (Method method : methods) {
                            BeanData beanDatat = new BeanData();

                            TextField textField = (TextField) editPane.lookup("#" + method.getName());
                            // 普通对象直接验证结果
                            if(ObjectUtil.commonClass().contains(method.getReturnType())) {
                                TextField descTextField = (TextField) editPane.lookup("#" + method.getName() + "_desc");
                                ChoiceBox<Class> classTypeChoiceBox = (ChoiceBox<Class>)editPane.lookup("#" + method.getName() + "_classType");
                                ChoiceBox<CompareType> compareTypeChoiceBox = (ChoiceBox<CompareType>)editPane.lookup("#" + method.getName() + "_compareType");
                                String value = textField.getText();
                                if(StringUtils.isEmpty(value)) continue;

                                beanDatat.setExceptedValue(value);
                                beanDatat.setCheckName(method.getName());
                                beanDatat.setDesc(descTextField.getText());
                                Class clazz = classTypeChoiceBox.getSelectionModel().getSelectedItem();
                                beanDatat.setClassType(clazz == null ? null : clazz.getName());
                                beanDatat.setCompareType(compareTypeChoiceBox.getSelectionModel().getSelectedItem());
                            } else { // 进一步验证
                                String value = textField.getText();
                                if (StringUtils.isEmpty(value)) continue;
                                if (!VarUtil.isPutVar(value)) continue;
                                beanDatat.setExceptedValue(value);

                            }
                            if (editBeanDataMap == null) {// 新增
                                beanDatat.setIndex(tmpBeanCheck.getNextIndexForObject(tmpBeanCheck.getBeanDatasForObject()));
                            } else {  // 更新
                                index = tmp.values().size() > 0 ? tmp.values().iterator().next().getIndex() : 1;
                                beanDatat.setIndex(index);
                            }
                            tmp.put(method.getName(), beanDatat);
                        }

                        columns = methods.stream().map(method->method.getName()).collect(Collectors.joining(","));
                    }

                    if (key != null) {
                        TextField textField = (TextField) editPane.lookup("#key");
                        TextField descTextField = (TextField) editPane.lookup("#key_desc");
                        ChoiceBox<Class> classTypeChoiceBox = (ChoiceBox<Class>)editPane.lookup("#key_classType");
                        ChoiceBox<CompareType> compareTypeChoiceBox = (ChoiceBox<CompareType>)editPane.lookup("#key_compareType");

                        String value = textField.getText();
                        if(value != null) {
                            BeanData beanData = new BeanData();
                            beanData.setExceptedValue(value);
                            beanData.setCheckName(key);
                            beanData.setDesc(descTextField.getText());
                            beanData.setClassType(classTypeChoiceBox.getSelectionModel().getSelectedItem().getName());
                            beanData.setCompareType(compareTypeChoiceBox.getSelectionModel().getSelectedItem());

                            if (editBeanDataMap == null) {// 新增
                                beanData.setIndex(tmpBeanCheck.getNextIndexForObject(tmpBeanCheck.getBeanDatasForObject()));
                            } else {  // 更新
                                index = tmp.values().iterator().next().getIndex();
                                beanData.setIndex(index);
                            }
                            tmp.put(key, beanData);
                        }
                    }

                    if (editBeanDataMap != null) {
                        tmpBeanCheck.removeObjectData(datas, index);
                    }

                    datas.add(tmp);

                    tmpBeanCheck.setCheckName(checkBeanTextField.getText().trim());
                    tmpBeanCheck.setBeanType(beanType);
                    tmpBeanCheck.setColumns(columns);
                    tmpBeanCheck.setTreeNodeId(treeItem.getValue().getId());
                    tmpBeanCheck.setOtherInfo(buildOtherInfo());
                    tmpBeanCheck.setContent(JsonUtil.toJson(datas));
                    if (beanCheck != null) tmpBeanCheck.setId(beanCheck.getId());
                    BeanCheckDao.save(tmpBeanCheck);
                    loadData();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        };

        task.setOnFailed(e-> UiUtil.transitionPane(paramPane, listPane, null));
        task.setOnSucceeded(e->UiUtil.transitionPane(paramPane, listPane, null));
        ThreadUtilFactory.getInstance().submit(task);
    }

    private void saveELData(BeanType beanType) throws SQLException {
        Task<Void> task = new Task() {

            @Override
            protected Object call() throws Exception {
                try {
                    TextField elTextField = (TextField) editPane.lookup("#el");
                    TextField valueTextField = (TextField) editPane.lookup("#el_value");
                    TextField descTextField = (TextField) editPane.lookup("#el_desc");
                    ChoiceBox<Class> classTypeChoiceBox = (ChoiceBox<Class>) editPane.lookup("#el_classType");
                    ChoiceBox<CompareType> compareTypeChoiceBox = (ChoiceBox<CompareType>) editPane.lookup("#el_compareType");

                    BeanCheck tmpBeanCheck = beanCheck == null ? new BeanCheck() : beanCheck;

                    BeanData tmp = beanData;
                    tmp = tmp == null ? new BeanData() : tmp;

                    tmp.setCheckName(elTextField.getText().trim());
                    tmp.setExceptedValue(valueTextField.getText().trim());
                    tmp.setDesc(descTextField.getText().trim());
                    tmp.setClassType(classTypeChoiceBox.getSelectionModel().getSelectedItem().getName());
                    tmp.setCompareType(compareTypeChoiceBox.getSelectionModel().getSelectedItem());

                    tmpBeanCheck.setCheckName(checkBeanTextField.getText().trim());
                    tmpBeanCheck.setBeanType(beanType);
                    tmpBeanCheck.setColumns("el,value,desc,classType,compareType");
                    tmpBeanCheck.setTreeNodeId(treeItem.getValue().getId());
                    Set<BeanData> beanDataList = tmpBeanCheck.getBeanDatas();

                    if (beanData == null) { //新增
                        tmp.setIndex(tmpBeanCheck.getNextIndexForBeanData(beanDataList));
                    } else {
                        tmp.setIndex(beanData.getIndex());
                        beanDataList.remove(beanData);
                    }
                    beanDataList.add(tmp);
                    tmpBeanCheck.setContent(JsonUtil.toJson(beanDataList));
                    if (beanCheck != null) {
                        tmpBeanCheck.setId(beanCheck.getId());
                    }
                    BeanCheckDao.save(tmpBeanCheck);
                    loadData();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        };

        task.setOnFailed(e-> UiUtil.transitionPane(paramPane, listPane, null));
        task.setOnSucceeded(e->UiUtil.transitionPane(paramPane, listPane, null));
        ThreadUtilFactory.getInstance().submit(task);
    }

    public void saveAction(ActionEvent actionEvent) {
        String checkName = checkBeanTextField.getText();
        BeanType beanType = beanTypeBox.getSelectionModel().getSelectedItem();
        boolean check = checkBeanCheck();
        if (!check) return;
        try {
            BeanCheck tmp = beanCheck == null ? new BeanCheck() : beanCheck;
            tmp.setCheckName(checkName);
            tmp.setTreeNodeId(treeItem.getValue().getId());
            tmp.setBeanType(beanType);
            tmp.setOtherInfo(buildOtherInfo());
            if (beanCheck == null) {
                tmp.setContent("");
                tmp.setColumns("");
            }
            BeanCheckDao.save(tmp);
            beanCheck = tmp;

            loadData();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTableColumns() {
        BeanType n = beanTypeBox.getSelectionModel().getSelectedItem();
        if (n == null) return;
        if (n == BeanType.Json) {
            tablePane.setVisible(true);
            textPane.setVisible(false);
            initJsonTableColumn();
        } else if (n == BeanType.String) {
            tablePane.setVisible(false);
            textPane.setVisible(true);
            RegionUtil.show(textCompareTypeChoiceBox, beanValueTypeChoiceBox);
        } else if (n == BeanType.Object) {
            tablePane.setVisible(true);
            textPane.setVisible(false);

            RegionUtil.show(beanValueTypeChoiceBox, textCompareTypeChoiceBox);
            initObjectTableColumn();
            calcBeanListControl();
        } else if (n == BeanType.EL) {
            tablePane.setVisible(true);
            textPane.setVisible(false);
            initELTableColumn();
        }
    }

    private String buildOtherInfo() {
        Map<String, Object> result = new HashMap<>();
        if (textCompareTypeChoiceBox.isVisible()) {
            CompareType compareType = textCompareTypeChoiceBox.getSelectionModel().getSelectedItem();
            if (compareType != null) {
                result.put("compareType", compareType.name());
            }
        }
        if (beanValueTypeChoiceBox.isVisible()) {
            BeanValueType beanValueType = beanValueTypeChoiceBox.getSelectionModel().getSelectedItem();
            if (beanValueType != null) {
                result.put("beanValueType", beanValueType.name());
            }
        }
        return JsonUtil.toJson(result);
    }

    private boolean checkBeanCheck() {
        String checkName = checkBeanTextField.getText();
        BeanType beanType = beanTypeBox.getSelectionModel().getSelectedItem();
        CompareType compareType = textCompareTypeChoiceBox.getSelectionModel().getSelectedItem();
        BeanValueType beanValueType = beanValueTypeChoiceBox.getSelectionModel().getSelectedItem();
        if (!checkCheckName(checkName) || beanType == null) {
            DialogUtil.alert("参数错误", Alert.AlertType.ERROR);
            return false;
        }
        if (beanType == BeanType.String && compareType == null) {
            DialogUtil.alert("参数错误", Alert.AlertType.ERROR);
            return false;
        }
        if (beanType == BeanType.Object && beanValueType == null) {
            DialogUtil.alert("参数错误", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }

    private boolean checkCheckName(String checkName) {
        if (checkName == null || checkName.equals("")) {
            return false;
        }
        if(checkName.equals("${result}")) return true;
        String varName = VarUtil.getVarName(checkName);
        if (varName == null) return false;
        VarUtil.cacheVar(treeItem);
        VarModel varModel = VarFactory.getInstance().getCache(treeItem, varName);
        if (varModel == null) {
            return false;
        }
        return true;
    }

    private VarModel getVarModel() {
        String checkName = checkBeanTextField.getText();
        if (checkName == null || checkName.trim().equals("")) return null;
        String varName = VarUtil.getVarName(checkName);
        if (varName == null) return null;
        VarModel varModel = VarFactory.getInstance().getCache(treeItem, varName);
        return varModel;
    }

    private ChoiceBox<Class> buildClassTypeChoiceBox(String id) {
        ChoiceBox<Class> classTypeChoiceBox = new ChoiceBox<>();
        classTypeChoiceBox.setPrefWidth(150);
        classTypeChoiceBox.setConverter(new ObjectStringConverter<>(clazz -> clazz.getSimpleName()));
        classTypeChoiceBox.getItems().addAll(ObjectUtil.commonClass());
        classTypeChoiceBox.setId(id);
        return classTypeChoiceBox;
    }

    private ChoiceBox<CompareType> buildCompareTypeChoiceBox(String id) {
        ChoiceBox<CompareType> compareTypeChoiceBox = new ChoiceBox<>();
        compareTypeChoiceBox.setPrefWidth(150);
        compareTypeChoiceBox.setConverter(new ObjectStringConverter<>(compareType -> compareType.getDesc()));
        compareTypeChoiceBox.getItems().addAll(CompareType.values());
        compareTypeChoiceBox.setId(id);
        return compareTypeChoiceBox;
    }

    private void calcBeanListControl() {
        VarModel varModel = getVarModel();
        if (varModel == null) return;
        beanListControl.setVisible(false);
        if (varModel.isArray() || varModel.isListOrSet()) {
            beanListControl.setVisible(true);
        }
    }

    public Map<String, BeanData> preRow(int index) {
        Map<String, BeanData> preRow = null;
        for (Object row : dataTableView.getItems()) {
            Map<String, BeanData> rowData = (Map<String, BeanData>) row;
            int currentIndex = rowData.values().iterator().next().getIndex();
            if (currentIndex < index) {
                preRow = rowData;
                continue;
            } else if (currentIndex >= index) {
                break;
            }
        }
        return preRow;
    }

    public Map<String, BeanData> nextRow(int index) {
        for (Object row : dataTableView.getItems()) {
            Map<String, BeanData> rowData = (Map<String, BeanData>) row;
            int currentIndex = rowData.values().iterator().next().getIndex();
            if (currentIndex < index) continue;
            if (currentIndex == index) {
                continue;
            }
            return rowData;
        }
        return null;
    }

    private BeanData preRowForBeanData(int index) {
        BeanData preRow = null;
        for (Object row : dataTableView.getItems()) {
            BeanData rowData = (BeanData) row;
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

    private BeanData nextRowForBeanData(int index) {
        for (Object row : dataTableView.getItems()) {
            BeanData rowData = (BeanData) row;
            int currentIndex = rowData.getIndex();
            if (currentIndex < index) continue;
            if (currentIndex == index) {
                continue;
            }
            return rowData;
        }
        return null;
    }
}
