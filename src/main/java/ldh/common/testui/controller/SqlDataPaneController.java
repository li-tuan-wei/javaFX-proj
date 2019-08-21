package ldh.common.testui.controller;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import ldh.common.testui.cell.ObjectListCell;
import ldh.common.testui.cell.ObjectStringConverter;
import ldh.common.testui.constant.ParamCategory;
import ldh.common.testui.constant.SqlDataType;
import ldh.common.testui.constant.SqlHandleType;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.dao.SqlDataDao;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.SqlData;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.*;
import ldh.common.testui.vo.SqlCheckData;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Created by ldh123 on 2017/6/5.
 */
public class SqlDataPaneController implements Initializable{

    @FXML private RadioButton sqlTextBtn;
    @FXML private RadioButton sqlFileBtn;
//    @FXML private RadioButton sqlCvsBtn;
    @FXML private TextArea sqlTextArea;
    @FXML private ListView<SqlData.SqlFileData> sqlFile;
    @FXML private ListView<SqlData.SqlFileData> sqlCvs;
    @FXML private ChoiceBox<ParamModel> databaseChoiceBox;

    @FXML private Pane sqlTextPane;
    @FXML private BorderPane sqlFilePane;
    @FXML private BorderPane sqlCvsPane;
    @FXML private CheckBox baseOffsetDayCheckBox;
    @FXML private DatePicker baseOffsetDayDatePicker;

    private ToggleGroup toggleGroup = new ToggleGroup();
    private TreeItem<TreeNode> treeItem;

    private SqlData sqlData = null;
    private SqlHandleType handleType;

    private FileChooser fileChooser = new FileChooser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sqlTextBtn.setToggleGroup(toggleGroup);
        sqlFileBtn.setToggleGroup(toggleGroup);
//        sqlCvsBtn.setToggleGroup(toggleGroup);

        databaseChoiceBox.setConverter(new ObjectStringConverter<>(paramModel -> paramModel.getName()));
        sqlFile.setCellFactory(new ObjectListCell<>(sqlFileData->{
            return sqlFileData.getFile() + "--" + sqlFileData.getIndex();
        }));

        sqlCvs.setCellFactory(new ObjectListCell<>(sqlFileData->{
            return sqlFileData.getFile();
        }));
    }

    public void setTreeItem(TreeItem<TreeNode> treeItem) {
        this.treeItem = treeItem;
//        List<String> database = new ArrayList();
        List<ParamModel> paramModels = DataUtil.getAllParamModels(treeItem, ParamCategory.Database);
//        database = paramModels.stream().map(pm->pm.getName()).collect(Collectors.toList());
        databaseChoiceBox.getItems().addAll(paramModels);

        loadData(treeItem);
    }

    public void setSqlHandleType(SqlHandleType sqlHandleType) {
        this.handleType = sqlHandleType;
    }

    private void loadData(TreeItem<TreeNode> treeItem) {
        ThreadUtilFactory.getInstance().submit(()->{
            try {
                List<SqlData> sqlDataList = SqlDataDao.getByTreeNodeId(treeItem.getValue().getId());
                if (sqlDataList.size() == 0) return null;
                sqlData =  sqlDataList.get(0);
                ParamModel pm = sqlData.getParamModel();
                Object objectData = sqlData.getObjectData();
                Platform.runLater(()->{
                    databaseChoiceBox.getSelectionModel().select(pm);
                    if (sqlData.getDataType() == SqlDataType.sql) {
                        RegionUtil.show(sqlTextPane, sqlFilePane, sqlCvsPane);
                        toggleGroup.selectToggle(sqlTextBtn);
                        sqlTextArea.setText(sqlData.getData());
                    } else if (sqlData.getDataType() == SqlDataType.file_sql) {
                        RegionUtil.show(sqlFilePane, sqlTextPane, sqlCvsPane);
                        toggleGroup.selectToggle(sqlFileBtn);
                        List<SqlData.SqlFileData> sqlFileDataList = (List<SqlData.SqlFileData>) objectData;
                        sqlFileDataList.sort((sf1, sf2)->sf1.getIndex().compareTo(sf2.getIndex()));
                        sqlFile.getItems().addAll(sqlFileDataList);
                    } else if (sqlData.getDataType() == SqlDataType.csv) {
                        RegionUtil.show(sqlCvsPane, sqlTextPane, sqlFilePane);
//                        toggleGroup.selectToggle(sqlCvsBtn);
                        List<SqlData.SqlFileData> csvFileDataList = (List<SqlData.SqlFileData>) objectData;
                        sqlCvs.getItems().addAll(csvFileDataList);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(()->{
                    DialogUtil.alert(e.getMessage(), Alert.AlertType.ERROR);
                });
            }
            return null;
        }, null);
    }

    private TreeItem<TreeNode> getParamTreeItem(TreeItem<TreeNode> treeItem) {
        for (TreeItem<TreeNode> treeNodeTreeItem : treeItem.getChildren()) {
            if(treeNodeTreeItem.getValue().getTreeNodeType() == TreeNodeType.Param) {
                return treeNodeTreeItem;
            }
        }
        return null;
    }

    public void selectFilePane(ActionEvent actionEvent) {
        RegionUtil.show(sqlFilePane, sqlTextPane, sqlCvsPane);
    }

    public void selectSqlTextPane(ActionEvent actionEvent) {
        RegionUtil.show(sqlTextPane, sqlFilePane, sqlCvsPane);
    }

    public void selectCvsPane(ActionEvent actionEvent) {
        RegionUtil.show(sqlCvsPane, sqlFilePane, sqlTextPane);
    }

    public void addFile(ActionEvent actionEvent) {
        if (toggleGroup.getSelectedToggle() == sqlFileBtn) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("select sql file","*.sql"));
        } else {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("select sql file","*.csv"));
        }

        File file = fileChooser.showOpenDialog(sqlFilePane.getScene().getWindow());
        if (file == null) return;
        SqlData.SqlFileData sqlFileData = new SqlData.SqlFileData();
        sqlFileData.setFile(file.getPath());
        sqlFileData.setIndex(OrderUtil.maxIndex(sqlFile.getItems()));
        List<SqlData.SqlFileData> newList = new ArrayList<>();
        if (toggleGroup.getSelectedToggle() == sqlFileBtn) {
            sqlFileData.setIndex(sqlFile.getItems().size());
            List<SqlData.SqlFileData> sqlFileDatas = sqlFile.getItems();
            newList.addAll(sqlFileDatas);
            newList.add(sqlFileData);
            saveData(JsonUtil.toJson(newList), ()->{
                Platform.runLater(()->{
                    sqlFile.getItems().add(sqlFileData);
                });
                return null;
            });
        } else {
            sqlFileData.setIndex(sqlFile.getItems().size());
            List<SqlData.SqlFileData> sqlFileDatas = sqlCvs.getItems();
            newList.addAll(sqlFileDatas);
            newList.add(sqlFileData);
            saveData(JsonUtil.toJson(newList), ()->{
                Platform.runLater(()->{
                    sqlCvs.getItems().add(sqlFileData);
                });
                return null;
            });
        }
    }

    public void deleteFile(ActionEvent actionEvent) {
        ThreadUtilFactory.getInstance().submit(()->{
            SqlData.SqlFileData sqlFileData = null;
            List<SqlData.SqlFileData> sqlFileDataList = new ArrayList<>();
            if (sqlData.getDataType() == SqlDataType.file_sql) {
                sqlFileData = sqlFile.getSelectionModel().getSelectedItem();
                sqlFileDataList.addAll(sqlFile.getItems());
            } else if (sqlData.getDataType() == SqlDataType.csv) {
                sqlFileData = sqlCvs.getSelectionModel().getSelectedItem();
                sqlFileDataList.addAll(sqlCvs.getItems());
            }
            if (sqlFileData == null) return null;
            sqlFileDataList.remove(sqlFileData);
            String data = JsonUtil.toJson(sqlFileDataList);
            saveData(data, ()->{
                Platform.runLater(()->{
                    if (sqlData.getDataType() == SqlDataType.file_sql) {
                        SqlData.SqlFileData selectedItem = sqlFile.getSelectionModel().getSelectedItem();
                        sqlFile.getItems().remove(selectedItem);
                    } else if (sqlData.getDataType() == SqlDataType.csv) {
                        SqlData.SqlFileData selectedItem = sqlCvs.getSelectionModel().getSelectedItem();
                        sqlCvs.getItems().remove(selectedItem);
                    }
                });
                return null;
            });
            return null;
        }, null);
    }

    public void toUpRow(ActionEvent actionEvent) {
        SqlData.SqlFileData sqlFileData = sqlFile.getSelectionModel().getSelectedItem();
        if (sqlFileData == null) {
            DialogUtil.alert("请选择一行", Alert.AlertType.ERROR);
            return;
        }
        long count = sqlFile.getItems().stream().filter(sqlCheckData1 -> sqlCheckData1.getIndex() == sqlFileData.getIndex()).count();
        if (count > 1) {
            sqlFileData.setIndex(OrderUtil.maxIndex(sqlFile.getItems())+1);
            saveData(JsonUtil.toJson(sqlFile.getItems()), null);
            return;
        }

        SqlData.SqlFileData preRow = OrderUtil.pre(sqlFile.getItems(), sqlFileData.getIndex());
        if (preRow == null) return;
        int index = sqlFileData.getIndex();
        sqlFileData.setIndex(preRow.getIndex());
        preRow.setIndex(index);

        saveData(JsonUtil.toJson(sqlFile.getItems()), null);
        sqlFile.getItems().remove(sqlFileData);
        int rowIndex = sqlFile.getItems().indexOf(preRow);
        sqlFile.getItems().add(rowIndex, sqlFileData);
        sqlFile.getSelectionModel().select(sqlFileData.getIndex());
    }

    public void toDownRow(ActionEvent actionEvent) {
        SqlData.SqlFileData sqlFileData = sqlFile.getSelectionModel().getSelectedItem();
        if (sqlFileData == null) {
            DialogUtil.alert("请选择一行", Alert.AlertType.ERROR);
            return;
        }
        SqlData.SqlFileData nextRow = OrderUtil.next(sqlFile.getItems(), sqlFileData.getIndex());
        if (nextRow == null) return;
        int index = sqlFileData.getIndex();
        sqlFileData.setIndex(nextRow.getIndex());
        nextRow.setIndex(index);

        saveData(JsonUtil.toJson(sqlFile.getItems()), null);
        int rowIndex = sqlFile.getItems().indexOf(nextRow);
        sqlFile.getItems().remove(sqlFileData);
        sqlFile.getItems().add(rowIndex, sqlFileData);
        sqlFile.getSelectionModel().select(sqlFileData.getIndex());
    }

    public void saveSqlText(ActionEvent actionEvent) {
        saveData(sqlTextArea.getText().trim(), null);
    }

    private void saveData(String data, Supplier<?> supplier) {
        SqlData tmpSqlData = sqlData;
        if (sqlData == null) {
            tmpSqlData = new SqlData();
            tmpSqlData.setTreeNodeId(treeItem.getValue().getId());
            tmpSqlData.setHandleType(handleType);
        } else {
            tmpSqlData.setId(sqlData.getId());
        }
        tmpSqlData.setDatabaseParamId(databaseChoiceBox.getSelectionModel().getSelectedItem().getId());
        RadioButton selectedRadioButton = (RadioButton) toggleGroup.getSelectedToggle();
        if (selectedRadioButton == sqlTextBtn) {
            tmpSqlData.setDataType(SqlDataType.sql);
        } else if (selectedRadioButton == sqlFileBtn) {
            tmpSqlData.setDataType(SqlDataType.file_sql);
        }
//        else if (selectedRadioButton == sqlCvsBtn) {
//            tmpSqlData.setDataType(SqlDataType.csv);
//        }
        tmpSqlData.setData(data);
        SqlData tmp = tmpSqlData;
        ThreadUtilFactory.getInstance().submit(()->{
            SqlDataDao.save(tmp);
            sqlData = tmp;
            if (supplier != null) supplier.get();
            return sqlData;
        }, (task)->{
            task.setOnFailed(e->{
//                Platform.runLater(()-> DialogUtil.alert("操作失败", Alert.AlertType.ERROR));
                UiUtil.showMessage("操作失败");
            });
            task.setOnSucceeded(e->{
//                Platform.runLater(()-> DialogUtil.alert("操作成功", Alert.AlertType.INFORMATION));
                UiUtil.showMessage("操作成功");
            });
        });
    }


    public void isBaseOffsetDay(ActionEvent actionEvent) {
        if (baseOffsetDayCheckBox.isSelected()) {
            baseOffsetDayDatePicker.setDisable(false);
        } else {
            baseOffsetDayDatePicker.setDisable(true);
        }
    }
}
