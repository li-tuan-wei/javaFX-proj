package ldh.common.testui.controller;

import com.jfoenix.controls.JFXButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.util.Pair;
import ldh.common.testui.cell.MethodDataColumnCell;
import ldh.common.testui.cell.ObjectListCell;
import ldh.common.testui.cell.ObjectTableCellFactory;
import ldh.common.testui.cell.SqlCheckDataColumnCell;
import ldh.common.testui.constant.ParamCategory;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.controller.pane.SqlCheckController;
import ldh.common.testui.dao.SqlCheckDao;
import ldh.common.testui.dao.SqlCheckDataDao;
import ldh.common.testui.dao.TreeDao;
import ldh.common.testui.model.BeanVar;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.*;
import ldh.common.testui.vo.SqlCheck;
import ldh.common.testui.vo.SqlCheckData;
import ldh.common.testui.vo.SqlColumn;
import ldh.common.testui.vo.SqlColumnData;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

/**
 * Created by ldh123 on 2017/6/5.
 */
public class SqlCheckPaneController implements Initializable{

    @FXML private ListView<SqlCheck> sqlListView;
    @FXML private Label sqlShowLabel;
    @FXML private TableView<SqlCheckData> sqlCheckTableView;
    @FXML private VBox showPane;
    @FXML private ScrollPane editPane;
    @FXML private GridPane editGridPane;

    private TreeItem<TreeNode> treeItem;
    private List<SqlColumn> sqlColumnList = null;
    private SqlCheck sqlCheck = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        sqlListView.setCellFactory(new ObjectListCell<>(sqlCheck->sqlCheck.getDatabaseParam().getName() + ":" + sqlCheck.getName()));
        sqlListView.setOnMouseClicked(this::doubleClick);

        initContextMenuForSqlListView();
        initContextMenuForSqlTableView();

        ColumnConstraints  columnConstraints1 = new ColumnConstraints(120);
        columnConstraints1.setHalignment(HPos.RIGHT);
        ColumnConstraints  columnConstraints2 = new ColumnConstraints(200, 200, 1000, Priority.ALWAYS, HPos.LEFT, true);
        ColumnConstraints  columnConstraints3 = new ColumnConstraints(300);
        editGridPane.getColumnConstraints().addAll(columnConstraints1, columnConstraints2, columnConstraints3);
        editGridPane.setVgap(5);
        editGridPane.setHgap(5);
    }

    public void setTreeItem(TreeItem<TreeNode> treeItem) {
        this.treeItem = treeItem;
        sqlColumnList = null;
        loadData();
    }

    public void save(ActionEvent actionEvent) {
    }

    public void addSqlCheck(ActionEvent event) throws IOException {
        addOrEditSql(null);
    }

    public void removeSqlCheck(ActionEvent event) {
    }

    private void addOrEditSql(SqlCheck initSqlCheck) throws IOException {
        Dialog<SqlCheck> dialog = new Dialog<>();
        dialog.setTitle("添加SQL验证信息");
        dialog.setHeaderText("按要求填写SQL验证信息");
        ButtonType loginButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        FXMLLoader fxmlLoader = new FXMLLoader(MainAppController.class.getResource("/fxml/pane/SqlCheckEdit.fxml"));
        Parent root = fxmlLoader.load();
        SqlCheckController sqlPaneController = fxmlLoader.getController();
        sqlPaneController.setTreeItem(treeItem);
        sqlPaneController.setInitData(initSqlCheck);

        dialog.getDialogPane().setContent(root);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                SqlCheck sqlCheck = sqlPaneController.buildSqlCheck();
                sqlCheck.setTreeNodeId(treeItem.getValue().getId());
                return sqlCheck;
            }
            return null;
        });

        Optional<SqlCheck> result = dialog.showAndWait();
        result.ifPresent(data -> {
            Integer id = data.getId();
            SqlCheckDao.save(data);
            if (id != null) {
                sqlListView.refresh();
            } else {
                sqlListView.getItems().add(data);
            }
        });
    }

    private void loadData() {
        new Thread(new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<SqlCheck> sqlCheckList = SqlCheckDao.getByTreeNodeId(treeItem.getValue().getId());
                Platform.runLater(()->{
                    sqlListView.getItems().addAll(sqlCheckList);
                    sqlListView.getSelectionModel().select(0);
                    doubleClick(null);
                });
                return null;
            }
        }).start();
    }

    private void doubleClick(MouseEvent mouseEvent) {
        SqlCheck sqlCheck = sqlListView.getSelectionModel().getSelectedItem();
        if (sqlCheck == null) return;
        sqlShowLabel.setText("SQL:" + sqlCheck.getSql().replaceAll("(\\r\\n|\\r|\\n|\\n\\r)", " ") + "   参数：" + sqlCheck.getArgs());
        if (mouseEvent != null && mouseEvent.getClickCount() != 2) return;
        SqlCheck sqlCheckt = sqlListView.getSelectionModel().getSelectedItem();
        if (sqlCheckt == null) return;
        this.sqlCheck = sqlCheckt;
        try {
            List<SqlColumn> sqlColumns = SqlCheckDao.getSqlStructForSql(sqlCheck.getSqlStruct(), sqlCheck.getDatabaseParam());
            sqlCheckTableView.getItems().clear();
            sqlCheckTableView.getColumns().clear();
            initTableColumn(sqlColumns);
            this.sqlColumnList = sqlColumns;
            sqlColumnList = sqlColumns;

            UiUtil.transitionPane(editPane, showPane, null);

            List<SqlCheckData> sqlColumnDatas = SqlCheckDataDao.getSqlCheckData(sqlCheck);
            sqlCheckTableView.getItems().addAll(sqlColumnDatas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initTableColumn(List<SqlColumn> sqlColumns) {
        boolean isFirst = true;
        for (SqlColumn sqlColumn : sqlColumns) {
            if (isFirst) {
                TableColumn<SqlCheckData, SqlColumnData> indexColumn = new TableColumn<>("index");
                indexColumn.setCellValueFactory(new SqlCheckDataColumnCell(sqlColumn.getColumnName()));
                indexColumn.setPrefWidth(50);
                indexColumn.setCellFactory(new ObjectTableCellFactory<>(sqlColumnData->{
                    return sqlColumnData.getIndex();
                }, Pos.CENTER));
                isFirst = false;
                sqlCheckTableView.getColumns().add(indexColumn);
            }
            TableColumn<SqlCheckData, SqlColumnData> tableColumn = new TableColumn<>(sqlColumn.getColumnName());
            tableColumn.setPrefWidth(150);
            tableColumn.setCellFactory(new ObjectTableCellFactory<>(sqlColumnData->{
                VBox vbox = new VBox();
                Label label = new Label(sqlColumnData.getDesc());
                if (VarUtil.isPutVar(sqlColumnData.getExpectValue())) {
                    Label expect = new Label("设置变量:" + sqlColumnData.getExpectValue());
                    Label value = new Label("实际值:" + (sqlColumnData.getValue() == null ? "null" : sqlColumnData.getValue().toString()));
                    vbox.getChildren().addAll(label, expect);
                    return vbox;
                }
                Object changedValue = sqlColumnData.getChangedValue();
                Label expect = new Label("期望值:" + (changedValue == null ? "null" : changedValue.toString()));
                Label value = new Label("实际值:" + (sqlColumnData.getValue() == null ? "null" : sqlColumnData.getValue().toString()));
                Boolean isSuccess = sqlColumnData.getValue() == null || changedValue == null ? false : ObjectUtil.isEqual(changedValue, sqlColumnData.getValue());
                isSuccess = sqlColumnData.getIsEqual() == null ? isSuccess : sqlColumnData.getIsEqual();
                isSuccess = isSuccess == null ? false : isSuccess;
                Label check = new Label("验证结果:" + (sqlColumnData.getValue() == null || changedValue == null ? "未验证" : isSuccess));
                if (!isSuccess) check.setTextFill(Color.RED);
                if (isSuccess) check.setTextFill(Color.GREEN);
                vbox.getChildren().addAll(label, expect, value, check);
                return vbox;
            }));
            tableColumn.setCellValueFactory(new SqlCheckDataColumnCell(sqlColumn.getColumnName()));
            sqlCheckTableView.getColumns().add(tableColumn);
        }
    }

    private void initContextMenuForSqlTableView() {
        ContextMenu tableViewMenu = new ContextMenu();
        MenuItem add = new MenuItem("添加", RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.PLUS), 20, Color.LIGHTBLUE));
        add.setOnAction(this::addSqlCheckData);

        MenuItem edit = new MenuItem("编辑", RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.EDIT), 20, Color.LIGHTBLUE));
        edit.setOnAction(this::editSqlCheckData);

        MenuItem remove = new MenuItem("删除", RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.COPY), 20, Color.LIGHTBLUE));
        remove.setOnAction(this::removeSqlCheckData);

        MenuItem copy = new MenuItem("复制", RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.COPY), 20, Color.LIGHTBLUE));
        copy.setOnAction(this::copySqlCheckViewData);

        tableViewMenu.getItems().addAll(add, edit, remove, copy);
        sqlCheckTableView.setContextMenu(tableViewMenu);
    }

    private void initContextMenuForSqlListView() {
        ContextMenu sqlListViewMenu = new ContextMenu();
        MenuItem add = new MenuItem("添加", RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.PLUS), 20, Color.LIGHTBLUE));
        add.setOnAction(this::addSqlListViewData);

        MenuItem edit = new MenuItem("编辑", RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.EDIT), 20, Color.LIGHTBLUE));
        edit.setOnAction(this::editSqlListViewData);

        MenuItem remove = new MenuItem("删除", RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.COPY), 20, Color.LIGHTBLUE));
        remove.setOnAction(this::removeSqlListViewData);

        MenuItem copy = new MenuItem("复制", RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.COPY), 20, Color.LIGHTBLUE));
        copy.setOnAction(this::copySqlListViewData);

        sqlListViewMenu.getItems().addAll(add, edit, remove, copy);
        sqlListView.setContextMenu(sqlListViewMenu);
    }

    private void editSqlListViewData(ActionEvent event) {
        SqlCheck sqlCheck = sqlListView.getSelectionModel().getSelectedItem();
        if (sqlCheck == null) return;
        try {
            addOrEditSql(sqlCheck);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeSqlListViewData(ActionEvent event) {
        SqlCheck sqlCheck = sqlListView.getSelectionModel().getSelectedItem();
        if (sqlCheck == null) return;
        try {
            SqlCheckDao.delete(sqlCheck.getId());
            sqlListView.getItems().remove(sqlCheck);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void copySqlListViewData(ActionEvent event) {
        SqlCheck sqlCheck = sqlListView.getSelectionModel().getSelectedItem();
        if (sqlCheck == null) return;
        try {
//            final Clipboard clipboard = Clipboard.getSystemClipboard();
//            String str = clipboard.getString();
            SqlCheck newSqlCheck = new SqlCheck();
            newSqlCheck.setName(sqlCheck.getName() + "-copy");
            newSqlCheck.setTreeNodeId(sqlCheck.getTreeNodeId());
            newSqlCheck.setArgs(sqlCheck.getArgs());
            newSqlCheck.setDatabaseParam(sqlCheck.getDatabaseParam());
            newSqlCheck.setDatabaseParamId(sqlCheck.getDatabaseParamId());
            newSqlCheck.setSql(sqlCheck.getSql());
            newSqlCheck.setSqlStruct(sqlCheck.getSqlStruct());
            SqlCheckDao.insert(newSqlCheck);
            sqlListView.getItems().add(newSqlCheck);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addSqlListViewData(ActionEvent event) {
        try {
            addOrEditSql(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addSqlCheckData(ActionEvent event) {
        UiUtil.transitionPane(showPane, editPane, (Void)->initSqlCheckDataForm(sqlColumnList, null));
    }

    public void editSqlCheckData(ActionEvent actionEvent) {
        SqlCheckData sqlCheckData = sqlCheckTableView.getSelectionModel().getSelectedItem();
        if (sqlCheckData == null) return;
        UiUtil.transitionPane(showPane, editPane, (Void)->initSqlCheckDataForm(sqlColumnList, sqlCheckData));
    }

    public void removeSqlCheckData(ActionEvent actionEvent) {
        SqlCheckData sqlCheckData = sqlCheckTableView.getSelectionModel().getSelectedItem();
        if (sqlCheckData == null) return;
        Integer id = sqlCheckData.getId();
        try {
            SqlCheckDataDao.delete(id);
            sqlCheckTableView.getItems().remove(sqlCheckData);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void copySqlCheckViewData(ActionEvent actionEvent) {
        SqlCheckData sqlCheckData = sqlCheckTableView.getSelectionModel().getSelectedItem();
        if (sqlCheckData == null) return;

        try {
            int maxIndex = OrderUtil.maxIndex(sqlCheckTableView.getItems())+1;
            sqlCheckData.setIndex(maxIndex);
            SqlCheckDataDao.insert(sqlCheckData);

            List<SqlCheckData> sqlColumnDatas = SqlCheckDataDao.getSqlCheckData(sqlCheck);
            sqlCheckTableView.getItems().clear();
            sqlCheckTableView.getItems().addAll(sqlColumnDatas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upSqlCheckData() {
        SqlCheckData sqlCheckData = sqlCheckTableView.getSelectionModel().getSelectedItem();
        if (sqlCheckData == null) return;
        long count = sqlCheckTableView.getItems().stream().filter(sqlCheckData1 -> sqlCheckData1.getIndex() == sqlCheckData.getIndex()).count();
        if (count > 1) {
            sqlCheckData.setIndex(OrderUtil.maxIndex(sqlCheckTableView.getItems())+1);
            saveData(sqlCheckData);
            return;
        }

        SqlCheckData preRow = OrderUtil.pre(sqlCheckTableView.getItems(), sqlCheckData.getIndex());
        if (preRow == null) return;
        int index = sqlCheckData.getIndex();
        sqlCheckData.setIndex(preRow.getIndex());
        preRow.setIndex(index);

        saveData(sqlCheckData);
        saveData(preRow);
        sqlCheckTableView.getSelectionModel().select(sqlCheckData.getIndex());
    }

    public void downSqlCheckData() {
        SqlCheckData sqlCheckData = sqlCheckTableView.getSelectionModel().getSelectedItem();
        if (sqlCheckData == null) return;
        SqlCheckData nextRow = OrderUtil.next(sqlCheckTableView.getItems(), sqlCheckData.getIndex());
        if (nextRow == null) return;
        int index = sqlCheckData.getIndex();
        sqlCheckData.setIndex(nextRow.getIndex());
        nextRow.setIndex(index);

        saveData(sqlCheckData);
        saveData(nextRow);

        sqlCheckTableView.getSelectionModel().select(sqlCheckData.getIndex());
    }

    private void initSqlCheckDataForm(List<SqlColumn> sqlColumns, SqlCheckData sqlCheckData) {
        int row = 0;
        editGridPane.getChildren().clear();
        editGridPane.setHgap(5);
        Label title = new Label("添加数据验证数据");
        title.setStyle("-fx-font-size: 18px;");
        GridPane.setConstraints(title, 1, row++, 3, 1);
        editGridPane.getChildren().addAll(title);
        Map<String, TextField[]> sqlCheckDataFieldMap = new HashMap<>();
        for (SqlColumn sqlColumn : sqlColumns) {
            Label label = new Label(sqlColumn.getColumnName());
            TextField textField = new TextField();
            GridPane.setHgrow(textField, Priority.ALWAYS);
            textField.setUserData(sqlColumn);

            TextField descTextField = new TextField();
            descTextField.setPromptText("注释");

            sqlCheckDataFieldMap.put(sqlColumn.getColumnName(), new TextField[]{textField, descTextField});

            Map<String, SqlColumnData> sqlColumnDataMap = sqlCheckData == null ? null : sqlCheckData.getDataMap();
            String columName = sqlColumn.getColumnName();
            if (sqlColumnDataMap != null && (sqlColumnDataMap.containsKey(columName) || sqlColumnDataMap.containsKey(columName.toLowerCase()))) {
                SqlColumnData sqlColumnData = sqlColumnDataMap.get(columName);
                if (sqlColumnData == null) sqlColumnData = sqlColumnDataMap.get(columName.toLowerCase());
                String value = sqlColumnData.getExpectValue().toString();
                textField.setText(value);
                String desc = sqlColumnData.getDesc();
                desc = desc == null ? "" : desc;
                descTextField.setText(desc);
            }

            GridPane.setConstraints(label, 0, row);
            GridPane.setConstraints(textField, 1, row, 1, 1);
            GridPane.setConstraints(descTextField, 2, row, 1, 1);
            editGridPane.getChildren().addAll(label, textField, descTextField);
            row++;
        }
        Button submit = new JFXButton("提交");
        submit.getStyleClass().addAll("btn", "btn-primary");
        Button returnBtn = new JFXButton("返回");
        returnBtn.getStyleClass().addAll("btn", "btn-info");

        Button checkBtn = new JFXButton("检查变量");
        checkBtn.getStyleClass().addAll("btn", "btn-info");
        checkBtn.setOnAction(e->{
            checkValues(checkBtn, sqlCheckDataFieldMap);
        });

        SqlCheckData temp = sqlCheckData;
        submit.setOnAction(e->{
            Map<String, SqlColumnData> dataMap = new HashMap<>();
            for (Map.Entry<String, TextField[]> entry : sqlCheckDataFieldMap.entrySet()) {
                SqlColumn sqlColumn = (SqlColumn) entry.getValue()[0].getUserData();
                SqlColumnData sqlColumnData = new SqlColumnData();
                sqlColumnData.setExpectValue(entry.getValue()[0].getText().trim());
                sqlColumnData.setSqlColumn(sqlColumn);
                sqlColumnData.setDesc(entry.getValue()[1].getText());
                dataMap.put(entry.getKey(), sqlColumnData);
            }
            SqlCheckData sqlCheckData1 = temp;
            if (sqlCheckData1 == null) {
                sqlCheckData1 = new SqlCheckData();
                sqlCheckData1.setIndex(OrderUtil.maxIndex(sqlCheckTableView.getItems())+1);
                sqlCheckData1.setSqlCheckId(sqlCheck.getId());
            }
            sqlCheckData1.setContent(JsonUtil.toJson(dataMap));
            saveData(sqlCheckData1);
            UiUtil.transitionPane(editPane, showPane, null);
        });
        returnBtn.setOnAction(e->UiUtil.transitionPane(editPane, showPane, null));
        HBox hbox = new HBox(submit, returnBtn, checkBtn);
        hbox.setSpacing(10);
        GridPane.setConstraints(hbox, 1, row++, 2, 1);
        editGridPane.getChildren().addAll(hbox);
    }

    private void checkValues(Button checkBtn, Map<String, TextField[]> sqlCheckDataFieldMap) {
        List<String> values = new ArrayList<>();
        for (Map.Entry<String, TextField[]> entry : sqlCheckDataFieldMap.entrySet()) {
            String value = entry.getValue()[0].getText().trim();
            List<String> elValues = VarUtil.getElExpressions(value);
            values.addAll(elValues);
        }

        if (values.size() < 0){
            DialogUtil.alert(String.format("没有变量，不需要检查"), Alert.AlertType.INFORMATION);
            return;
        }
        checkBtn.setDisable(true);

        int index = Integer.MAX_VALUE;
        Task<Void> task = VarUtil.buildCheckVarTask(treeItem, values, index);
        task.setOnFailed(e->{
            checkBtn.setDisable(false);
        });
        task.setOnSucceeded(e->checkBtn.setDisable(false));
        ThreadUtilFactory.getInstance().submit(task);
    }

    private void saveData(SqlCheckData sqlCheckData) {
        SqlCheckDataDao.save(sqlCheckData);
        try {
            sqlCheckTableView.getItems().clear();

            List<SqlCheckData> sqlColumnDatas = SqlCheckDataDao.getSqlCheckData(sqlCheck);
            sqlCheckTableView.getItems().addAll(sqlColumnDatas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
