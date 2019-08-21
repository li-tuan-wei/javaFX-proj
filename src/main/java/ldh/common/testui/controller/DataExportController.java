package ldh.common.testui.controller;

import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import ldh.common.testui.cell.ObjectStringConverter;
import ldh.common.testui.constant.ParamCategory;
import ldh.common.testui.dao.DataExportDao;
import ldh.common.testui.model.DataExport;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.*;
import ldh.common.testui.vo.DataExportItem;
import ldh.common.testui.vo.DatabaseParam;
import ldh.common.testui.vo.SqlColumnData;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class DataExportController extends BaseController implements Initializable {

    @FXML
    private ChoiceBox<ParamModel> databaseChoiceBox;
    @FXML
    private TableView<DataExportItem> tableNamesTableView;
    @FXML
    private TableColumn<DataExportItem, Boolean> isSelectedTableColumn;
    @FXML
    private TableColumn<DataExportItem, String> tableNameTableColumn;
    @FXML
    private TableColumn<DataExportItem, String> whereTableColumn;
    @FXML
    private Label fileLabel;
    @FXML
    private TextField nameTextField;

    private DirectoryChooser directoryChooser = new DirectoryChooser();
    private DataExport dataExport = null;

    public void setTreeItem(TreeItem<TreeNode> treeItem) {
        this.treeItem = treeItem;
        List<ParamModel> paramModels = DataUtil.getAllParamModels(treeItem, ParamCategory.Database);
        databaseChoiceBox.getItems().addAll(paramModels);

        loadData();
    }

    private void loadData() {
        ThreadUtilFactory.getInstance().submit(() -> {
            try {
                List<DataExport> dataExportList = DataExportDao.getByTreeNodeId(treeItem.getValue().getId());
                if (dataExportList.size() < 1) return null;
                dataExport = dataExportList.get(0);
                ParamModel paramModel = dataExport.getDatabaseParamModel();

                Platform.runLater(() -> {
                    databaseChoiceBox.getSelectionModel().select(paramModel);
                    fileLabel.setText(dataExport.getDir());
                    nameTextField.setText(dataExport.getName());
                });

                List<DataExportItem> dataExportItemList = JsonUtil.toObjectExpose(dataExport.getData(), new TypeToken<List<DataExportItem>>() {}.getType());
                Map<String, DataExportItem> dataExportItemMap = dataExportItemList.stream()
                        .collect(Collectors.toMap(DataExportItem::getTableName, dataExportItem -> dataExportItem));
                List<String[]> dataList = getAllTableNames(paramModel);
                List<DataExportItem> newDataExportItemList = new ArrayList<>();
                long i = 0;
                boolean hasNews = false;
                for (String[] array : dataList) {
                    DataExportItem item = dataExportItemMap.get(array[0]);
                    if (item == null) {
                        item = new DataExportItem();
                        item.setTableName(array[0]);
                        item.setTableDesc(array[1]);
                        item.setSelected(true);
                        item.setId(++i);
                        hasNews = true;
                    } else {
                        item.setId(++i);
                        item.setSelected(item.getSelected());
                        item.setWhere(item.getWhere());
                        item.setTableDesc(array[1]);
                    }
                    newDataExportItemList.add(item);
                }
                Platform.runLater(() -> {
                    tableNamesTableView.getItems().clear();
                    tableNamesTableView.getItems().addAll(newDataExportItemList);
                });
                if (hasNews) {
                    dataExport.setData(JsonUtil.toJsonExpose(newDataExportItemList));
                    if (dataExport != null) {
                        DataExportDao.save(dataExport);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }, null);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tableNamesTableView.setEditable(true);
        databaseChoiceBox.setConverter(new ObjectStringConverter<>(paramModel -> paramModel.getName()));
        databaseChoiceBox.getSelectionModel().selectedItemProperty().addListener((b, o, n) -> {
            if (dataExport != null && dataExport.getDatabaseParamId().equals(n.getId())) return;
            changeTableContent(n);
        });

        isSelectedTableColumn.setCellFactory(CheckBoxTableCell.forTableColumn(isSelectedTableColumn));
        isSelectedTableColumn.setEditable(true);

        whereTableColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        whereTableColumn.setEditable(true);
    }

    private void changeTableContent(ParamModel paramModel) {
        if (paramModel == null) return;
        tableNamesTableView.getItems().clear();

        loadDatabaseTableNames(paramModel);
    }

    private void loadDatabaseTableNames(ParamModel paramModel) {
        ThreadUtilFactory.getInstance().submit(() -> {
            List<String[]> tableNames = getAllTableNames(paramModel);
            List<DataExportItem> dataList = new ArrayList<>();
            long i = 0;
            for (String[] array : tableNames) {
                DataExportItem item = new DataExportItem();
                item.setTableName(array[0]);
                item.setTableDesc(array[1]);
                item.setSelected(true);
                item.setId(++i);
                dataList.add(item);
            }
            Platform.runLater(() -> tableNamesTableView.getItems().addAll(dataList));
            return null;
        }, (task) -> {
            task.setOnFailed(e -> {
                UiUtil.showMessage("加载数据失败");
            });
            task.setOnSucceeded(e -> {
                UiUtil.showMessage("加载数据成功");
            });
        });
    }

    private List<String[]> getAllTableNames(ParamModel paramModel) {
        List<String[]> dataList = new ArrayList<>();
        Connection connection = null;
        try {
            connection = DbUtils.getConnection(paramModel);
            DatabaseParam databaseParam = JsonUtil.toObject(paramModel.getValue(), DatabaseParam.class);
            String dbName = databaseParam.getDbName();
            dataList = DatabaseUtil.getAllTable(connection, null, dbName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.close(connection);
        }
        return dataList;
    }

    public void saveAction(ActionEvent actionEvent) {
        if (!checkData()) return;
        saveData();
    }

    public void selectDirAction(ActionEvent actionEvent) {
        File file = directoryChooser.showDialog(databaseChoiceBox.getScene().getWindow());
        if (file == null) return;
        fileLabel.setText(file.getPath());

    }

    public void selectAllAction(ActionEvent actionEvent) {
        if (tableNamesTableView.getItems().size() < 1) {
            DialogUtil.alert("无数据，不需要选择所有", Alert.AlertType.INFORMATION);
            return;
        }
        tableNamesTableView.getItems().forEach(dataExportItem -> dataExportItem.setSelected(true));
    }

    public void cancelAllAction(ActionEvent actionEvent) {
        if (tableNamesTableView.getItems().size() < 1) {
            DialogUtil.alert("无数据，不需要取消所有", Alert.AlertType.INFORMATION);
            return;
        }
        tableNamesTableView.getItems().forEach(dataExportItem -> dataExportItem.setSelected(false));
    }

    private void saveData() {
        DataExport tmp = new DataExport();
        tmp.setDatabaseParamId(databaseChoiceBox.getSelectionModel().getSelectedItem().getId());
        tmp.setTreeNodeId(treeItem.getValue().getId());
        tmp.setDir(fileLabel.getText());
        tmp.setName(nameTextField.getText().trim());
        List<DataExportItem> items = tableNamesTableView.getItems();
        tmp.setData(JsonUtil.toJsonExpose(items));
        if (dataExport != null) {
            tmp.setId(dataExport.getId());
        }
        DataExportDao.save(tmp);
        dataExport = tmp;
    }

    private boolean checkData() {
        if (databaseChoiceBox.getSelectionModel().getSelectedItem() == null) {
            DialogUtil.alert("请选择数据库", Alert.AlertType.ERROR);
            return false;
        }
        if (fileLabel.getText().equals("")) {
            DialogUtil.alert("请选择保存路径", Alert.AlertType.ERROR);
            return false;
        }
        if (nameTextField.getText().equals("")) {
            DialogUtil.alert("请填写保存名称", Alert.AlertType.ERROR);
            return false;
        }
        return true;
    }
}
