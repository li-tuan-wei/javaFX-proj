package ldh.common.testui.component;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Pair;
import ldh.common.testui.constant.ParamType;
import ldh.common.testui.dao.TestHttpParamDao;
import ldh.common.testui.model.TestHttp;
import ldh.common.testui.model.TestHttpParam;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

/**
 * Created by ldh123 on 2017/6/21.
 */
public class HttpParamControl extends HBox {

    @FXML
    private TableView<TestHttpParam> tableView;

    private TestHttp testHttp;
    private ParamType paramType;
    private ObservableList<TestHttpParam> tableValues = FXCollections.observableArrayList();

    public HttpParamControl() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/control/fxml/HttpParam.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        loader.load();

        tableView.setItems(tableValues);
    }

    @FXML
    public void initialize() {

    }

    public void initData(TestHttpParam testHttpParam) {
        tableValues.add(testHttpParam);
    }

    public void setTestHttp(TestHttp testHttp) {
        this.testHttp = testHttp;
    }

    public void setParamType(ParamType paramType) {
        this.paramType = paramType;
    }

    @FXML public void add() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("添加键值");
        dialog.setHeaderText("添加键值");

        ButtonType loginButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField key = new TextField();
        key.setPromptText("Key");
        TextArea value = new TextArea();
        value.setPromptText("Value");

        grid.add(new Label("Key:"), 0, 0);
        grid.add(key, 1, 0);
        grid.add(new Label("Value:"), 0, 1);
        grid.add(value, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        key.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> key.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                Pair pair = new Pair<>(key.getText(), value.getText());
                return pair;
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(values -> {
            TestHttpParam testHttpParam = new TestHttpParam();
            testHttpParam.setName(values.getKey());
            testHttpParam.setContent(values.getValue());
            testHttpParam.setParamType(paramType);
            testHttpParam.setTestHttpId(testHttp.getId());
            try {
                TestHttpParamDao.save(testHttpParam);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            tableValues.add(testHttpParam);
            refresh();
        });
    }

    @FXML public void edit() {
        TestHttpParam httpParam = tableView.getSelectionModel().getSelectedItem();
        if (httpParam == null) {
            return;
        }
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("修改键值");
        dialog.setHeaderText("修改键值");

        ButtonType loginButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField key = new TextField();
        key.setText(httpParam.getName());
        TextArea value = new TextArea();
        value.setText(httpParam.getContent());

        grid.add(new Label("Key:"), 0, 0);
        grid.add(key, 1, 0);
        grid.add(new Label("Value:"), 0, 1);
        grid.add(value, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);

        key.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!httpParam.getName().equals(oldValue)) {
                loginButton.setDisable(newValue.trim().isEmpty());
            }
        });

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> key.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(key.getText(), value.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(values -> {
            httpParam.setName(values.getKey());
            httpParam.setContent(values.getValue());
            httpParam.setParamType(paramType);
            httpParam.setTestHttpId(testHttp.getId());
            try {
                TestHttpParamDao.save(httpParam);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            refresh();
        });
    }

    @FXML public void remove() {
        TestHttpParam keyValue = tableView.getSelectionModel().getSelectedItem();
        if (keyValue != null) {
            tableValues.remove(keyValue);
        }
    }

    public void changeContentType(String contentType) {
        for (TestHttpParam httpParam : tableValues) {
            if (httpParam.getName().equals("contentType")) {
                httpParam.setContent(contentType);
                refresh();
                return;
            }
        }
        TestHttpParam httpParam = new TestHttpParam();
        httpParam.setName("contentType");
        httpParam.setContent(contentType);
        httpParam.setParamType(paramType);
        tableValues.add(httpParam);
        refresh();
    }

    private void refresh() {
        tableView.setItems(tableValues);
        System.out.println("size: " + tableValues.size());
        tableView.refresh();
    }

    public Map<String, Object> getAllParams() {
        Map<String, Object> allParams = new HashMap<>();
        for(TestHttpParam keyValue : tableView.getItems() ) {
            allParams.put(keyValue.getName(), keyValue.getContent());
        }
        return allParams;
    }
}
