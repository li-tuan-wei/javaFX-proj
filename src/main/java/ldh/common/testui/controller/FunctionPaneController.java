package ldh.common.testui.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import ldh.common.testui.cell.ObjectListCell;
import ldh.common.testui.cell.ObjectStringConverter;
import ldh.common.testui.cell.ObjectTableCellFactory;
import ldh.common.testui.constant.ParamCategory;
import ldh.common.testui.dao.CommonFunDao;
import ldh.common.testui.dao.ParamDao;
import ldh.common.testui.model.CommonFun;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.*;
import ldh.common.testui.vo.ParamInterface;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by ldh on 2018/4/18.
 */
public class FunctionPaneController extends BaseController implements Initializable {

    @FXML private TableView<CommonFun> tableView;
    @FXML private TextField nameField;
    @FXML private ComboBox<ParamModel> packageName;
    @FXML private ComboBox<Class> className;
    @FXML private TextArea descTextArea;
    @FXML private TableColumn<CommonFun, String> classNameColumn;

    @FXML private VBox showPane;
    @FXML private GridPane editPane;

    private CommonFun editParam = null;
    private ValidationSupport validationSupport = new ValidationSupport();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        packageName.setCellFactory(new ObjectListCell<>(paramModel -> paramModel.getName()));
        packageName.setConverter(new ObjectStringConverter<ParamModel>((paramModel)->paramModel.getName()));
        packageName.getSelectionModel().selectedItemProperty().addListener((b,o,n)-> initClassName(className, n));

        className.setCellFactory(new ObjectListCell<>(clazz->clazz.getSimpleName()));
        className.setConverter(new ObjectStringConverter<>(clazz->clazz.getSimpleName()));

        validationSupport.registerValidator(nameField, Validator.createEmptyValidator("名称不能为空"));
        validationSupport.registerValidator(packageName, Validator.createEmptyValidator( "包路径不能为空"));
        validationSupport.registerValidator(className, Validator.createEmptyValidator("类名不能为空"));

        tableView.setOnMouseClicked(e->{
            if (e.getClickCount() != 2) return;
            editFun(new ActionEvent());
        });

        classNameColumn.setCellFactory(new ObjectTableCellFactory(className->{
            if (className.toString().lastIndexOf(".") > 0) {
                String name = className.toString();
                return name.substring(name.lastIndexOf(".") + 1);
            }
            return className;
        }));

        TableViewUtil.alignment(Pos.CENTER_LEFT, tableView, classNameColumn.getText());
    }

    @Override
    public void setTreeItem(TreeItem<TreeNode> treeItem) {
        this.treeItem = treeItem;
        Set<ParamModel> paramModelSet = getParamModels(ParamCategory.Method_Package);
        packageName.getItems().addAll(paramModelSet);
        new Thread(new Task<Void>(){

            @Override
            protected Void call() throws Exception {
                reloadData(null);
                return null;
            }
        }).start();
    }

    public void addFun(ActionEvent actionEvent) {
        initForm();
        UiUtil.transitionPane(showPane, editPane, null);
    }

    public void editFun(ActionEvent e) {
        CommonFun commonFun = tableView.getSelectionModel().getSelectedItem();
        if (commonFun != null) {
            initFormValue(commonFun);
            UiUtil.transitionPane(showPane, editPane, null);
        }
    }

    public void removeFun(ActionEvent actionEvent) {
        CommonFun commonFun = tableView.getSelectionModel().getSelectedItem();
        if (commonFun == null) return;
        try {
            CommonFunDao.delete(commonFun);
            reloadData(null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void saveFun(ActionEvent actionEvent) {
        CommonFun commonFun = new CommonFun();
        commonFun.setName(nameField.getText().trim());
        commonFun.setPackageParamId(packageName.getSelectionModel().getSelectedItem().getId());
        commonFun.setClassName(className.getSelectionModel().getSelectedItem().getName());
        commonFun.setDesc(descTextArea.getText());
        commonFun.setTreeNodeId(treeItem.getValue().getId());

        if (editParam != null) {
            commonFun.setId(editParam.getId());
        }
        CommonFunDao.save(commonFun);
        reloadData(null);
        UiUtil.transitionPane(editPane, showPane,null);
    }

    public void returnShowPane(ActionEvent e) {
        UiUtil.transitionPane(editPane, showPane,null);
    }

    private void initForm() {
        editParam = null;
        nameField.setText("");
        descTextArea.setText("");
//        paramCategoryChoiceBox.getSelectionModel().clearSelection();
    }

    private void initFormValue(CommonFun commonFun) {
        editParam = commonFun;
        nameField.setText(commonFun.getName());
        descTextArea.setText(commonFun.getDesc());
        packageName.getSelectionModel().select(commonFun.getParamModel());
        className.getSelectionModel().select(MethodUtil.forClass(commonFun.getClassName()));
    }

    private void reloadData(CommonFun commonFun) {
        try {
            List<CommonFun> datas = CommonFunDao.getByTreeNodeId(treeItem.getValue().getId());
            Platform.runLater(()->{
                tableView.getItems().clear();
                tableView.getItems().addAll(datas);
            });

            if (commonFun != null) {
                Platform.runLater(()->{
                    tableView.getSelectionModel().select(commonFun);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void initClassName(ComboBox<Class> classNameComboBox, ParamModel paramModel) {
        classNameComboBox.getItems().clear();
        try {
            if (paramModel == null) return;
            if (paramModel.getParamCategory() == ParamCategory.Method_Package) {
                List<Class> classList = FileUtil.searchClass(paramModel.getValue());
                classNameComboBox.getItems().addAll(classList);
            }
//            else if (paramModel.getParamCategory() == ParamCategory.Other_jar) {
//                List<String> classStrList = FileUtil.searchFiles(paramModel.getValue());
//                List<Class> clazzList = new ArrayList<>();
//                String dir = paramModel.getValue() + File.separator;
//                for (String className : classStrList) {
//                    Class clazz = LibLoaderFactory.getInstance().loadClass(dir, className);
//                    clazzList.add(clazz);
//                }
//                classNameComboBox.getItems().addAll(clazzList);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
