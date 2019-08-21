package ldh.common.testui.controller;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import ldh.common.testui.cell.ObjectStringConverter;
import ldh.common.testui.cell.ObjectTableCellFactory;
import ldh.common.testui.constant.ParamCategory;
import ldh.common.testui.dao.ParamDao;
import ldh.common.testui.model.BeanVar;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.*;
import ldh.common.testui.vo.ParamInterface;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ldh on 2018/4/18.
 */
public class ParamPaneController extends BaseController implements Initializable {

    @FXML private TableView<ParamModel> tableView;
    @FXML private TextField nameField;
    @FXML private TextArea valueTextArea;
    @FXML private ChoiceBox<ParamCategory> paramCategoryChoiceBox;
    @FXML private ChoiceBox<String> paramClassNameChoiceBox;
    @FXML private TextArea descTextArea;
    @FXML private TableColumn<ParamModel, ParamCategory> paramCategoryColumn;
    @FXML private TableColumn<ParamModel, String> paramClassNameColumn;

    @FXML private VBox showPane;
    @FXML private GridPane editPane;
    @FXML private Label paramClassNameLabel;
    @FXML private Button checkValueBtn;

    private ParamModel editParam = null;
    private ValidationSupport validationSupport = new ValidationSupport();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        showClassNamePane(false);
        valueTextArea.setStyle("-fx-font-size: 16px");
        paramCategoryChoiceBox.setConverter(new ObjectStringConverter<>((paramCategory)-> paramCategory.getDesc()));
        paramCategoryChoiceBox.getItems().addAll(ParamCategory.values());
        paramCategoryChoiceBox.getSelectionModel().selectedItemProperty().addListener((b, o, n)->{
            showClassNamePane(false);
            if (n == ParamCategory.Constant) {
                showClassNamePane(true);
            }
            ParamInterface paramInterface = n.getParamInterface();
            if (paramInterface == null) return;
            valueTextArea.setText(paramInterface.demo());
        });

        validationSupport.registerValidator(nameField, Validator.createEmptyValidator("名称不能为空"));
        validationSupport.registerValidator(paramCategoryChoiceBox, Validator.createEmptyValidator( "类型不能为空"));
        validationSupport.registerValidator(valueTextArea, Validator.createEmptyValidator("值不能为空"));

        tableView.setOnMouseClicked(e->{
            if (e.getClickCount() != 2) return;
            editParam(new ActionEvent());
        });

        paramCategoryColumn.setCellFactory(new ObjectTableCellFactory(paramCategory->{
            if (paramCategory instanceof ParamCategory) {
                ParamCategory pc = (ParamCategory) paramCategory;
                return pc.getDesc();
            }
            return paramCategory;
        }));

        paramClassNameColumn.setCellFactory(new ObjectTableCellFactory(className->{
            if (className.toString().lastIndexOf(".") > 0) {
                String name = className.toString();
                return name.substring(name.lastIndexOf(".") + 1);
            }
            return className;
        }));

        TableViewUtil.alignment(Pos.CENTER_LEFT, tableView, paramCategoryColumn.getText(), paramClassNameColumn.getText());

        initClassNameChoise();

        paramClassNameChoiceBox.setConverter(new ObjectStringConverter<>(className->className.substring(className.lastIndexOf(".") + 1)));
    }

    @Override
    public void setTreeItem(TreeItem<TreeNode> treeItem) {
        this.treeItem = treeItem;


        Task task = new Task<Void>(){

            @Override
            protected Void call() throws Exception {
                List<ParamModel> datas = ParamDao.getByTreeNodeId(treeItem.getValue().getId());
                if (datas == null) {
                    return null;
                }
                Platform.runLater(()->{
                    tableView.getItems().addAll(datas);
                });
                return null;
            }
        };
        ThreadUtilFactory.getInstance().submit(task);
    }

    public void addParam(ActionEvent actionEvent) {
        initForm();
        UiUtil.transitionPane(showPane, editPane, null);
    }

    public void editParam(ActionEvent e) {
        ParamModel paramModel = tableView.getSelectionModel().getSelectedItem();
        if (paramModel != null) {
            initFormValue(paramModel);
            UiUtil.transitionPane(showPane, editPane, null);
        }
    }

    public void removeParam(ActionEvent actionEvent) {
        ParamModel paramModel = tableView.getSelectionModel().getSelectedItem();
        if (paramModel == null) return;
        try {
            ParamDao.delete(paramModel);
            reloadData(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void upParam(ActionEvent e) {
        ParamModel paramModel = tableView.getSelectionModel().getSelectedItem();
        if (paramModel == null) return;
        try {
            int i = 0;
            ParamModel prePm = null;
            for(ParamModel pm : tableView.getItems()) {
                i++;
                if (pm.getId().equals(paramModel.getId()) && i > 1) { // 第一个不能上移
                    prePm = tableView.getItems().get(i-2);
                    break;
                }
            }
            if (prePm != null) exchangeIndex(prePm, paramModel);
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    @FXML
    public void downParam(ActionEvent e) {
        ParamModel paramModel = tableView.getSelectionModel().getSelectedItem();
        if (paramModel == null) return;
        try {
            int i = 0;
            ParamModel prePm = null;
            for(ParamModel pm : tableView.getItems()) {
                i++;
                if (pm.getId().equals(paramModel.getId()) && i < tableView.getItems().size()) { // 最后一个不能下移
                    prePm = tableView.getItems().get(i);
                    break;
                }
            }

            if (prePm != null) exchangeIndex(prePm, paramModel);
        } catch (Exception se) {
            se.printStackTrace();
        }
    }

    private void exchangeIndex(ParamModel pm1, ParamModel pm2) {
        int idx = pm1.getIndex();
        pm1.setIndex(pm2.getIndex());
        pm2.setIndex(idx);
        ParamDao.update(pm2);
        ParamDao.update(pm1);
        reloadData(pm2);
    }

    public void saveParam(ActionEvent actionEvent) {
        ParamModel paramModel = new ParamModel();
        paramModel.setName(nameField.getText().trim());
        paramModel.setValue(valueTextArea.getText().trim());
        paramModel.setParamCategory(paramCategoryChoiceBox.getSelectionModel().getSelectedItem());
        if (paramModel.getParamCategory() == ParamCategory.Constant) {
            String className = paramClassNameChoiceBox.getSelectionModel().getSelectedItem();
            if (StringUtils.isEmpty(className)) {
                DialogUtil.alert("名称重复", Alert.AlertType.ERROR);
                return;
            }
            paramModel.setClassName(className);
        }
        paramModel.setDesc(descTextArea.getText().trim());
        paramModel.setTreeNodeId(treeItem.getValue().getId());
        if (editParam != null) {
            paramModel.setId(editParam.getId());
            paramModel.setIndex(editParam.getIndex());
        }
        ParamInterface paramInterface = paramCategoryChoiceBox.getSelectionModel().getSelectedItem().getParamInterface();
        if (paramInterface != null) {
            boolean isSuccess = paramInterface.check(treeItem, paramModel);
            if (!isSuccess) {
                DialogUtil.alert("没按照要求填写", Alert.AlertType.ERROR);
                return;
            }
        }
        int maxIndex = 1;
        for(ParamModel paramModel1 : tableView.getItems()) {
            if (paramModel1.getIndex() > maxIndex) {
                maxIndex = paramModel1.getIndex();
            }
            if (paramModel1.getName().equals(paramModel.getName()) && editParam == null) {
                DialogUtil.alert("名称重复", Alert.AlertType.ERROR);
                return;
            }
        }
        if (paramModel.getId().equals(0)) {
            paramModel.setIndex(maxIndex+1);
        }
        ParamDao.save(paramModel);
        reloadData(null);
        UiUtil.transitionPane(editPane, showPane,null);
    }

    public void returnShowPane(ActionEvent e) {
        UiUtil.transitionPane(editPane, showPane,null);
    }

    private void initForm() {
        editParam = null;
        nameField.setText("");
        valueTextArea.setText("");
        descTextArea.setText("");
//        paramCategoryChoiceBox.getSelectionModel().clearSelection();
    }

    private void initFormValue(ParamModel paramModel) {
        editParam = paramModel;
        nameField.setText(paramModel.getName());
        descTextArea.setText(paramModel.getDesc());
        paramCategoryChoiceBox.getSelectionModel().select(paramModel.getParamCategory());
        valueTextArea.setText(paramModel.getValue());
        paramClassNameChoiceBox.getSelectionModel().select(paramModel.getClassName());
    }

    private void reloadData(ParamModel pm) {
        List<ParamModel> datas = DataUtil.reLoad(treeItem.getValue().getId());
        tableView.getItems().clear();
        tableView.getItems().addAll(datas);
        if (pm != null) {
            tableView.getSelectionModel().select(pm);
        }
    }

    private void showClassNamePane(boolean isShow) {
        paramClassNameChoiceBox.setVisible(isShow);
        paramClassNameLabel.setVisible(isShow);
    }

    private void initClassNameChoise() {
        List<Class> classList = Arrays.asList(Integer.class, Byte.class, String.class, Long.class, Double.class, BigDecimal.class, Date.class);
        classList.forEach(clazz->{
            paramClassNameChoiceBox.getItems().add(clazz.getName());
        });
    }

    public void checkValue() {
        String elText = valueTextArea.getText().trim();
        List<String> values = VarUtil.getElExpressions(elText);
        if (values.size() < 0){
            DialogUtil.alert(String.format("没有变量，不需要检查"), Alert.AlertType.INFORMATION);
            return;
        }
        checkValueBtn.setDisable(true);

        int index = editParam != null ? editParam.getIndex() : Integer.MAX_VALUE;
        Task<Void> task = VarUtil.buildCheckVarTask(treeItem, values, index);
        task.setOnFailed(e->{
            checkValueBtn.setDisable(false);
        });
        task.setOnSucceeded(e->checkValueBtn.setDisable(false));
        ThreadUtilFactory.getInstance().submit(task);
    }
}
