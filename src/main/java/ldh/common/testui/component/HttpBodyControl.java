package ldh.common.testui.component;

import com.google.gson.Gson;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import ldh.common.testui.controller.TestFormController;
import ldh.common.testui.constant.ContentType;
import ldh.common.testui.dao.TestHttpBodyDao;
import ldh.common.testui.model.TestHttp;
import ldh.common.testui.model.TestHttpBody;
import ldh.common.testui.model.TestHttpParam;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.*;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ldh123 on 2017/6/21.
 */
public class HttpBodyControl extends VBox {

    @FXML private TextArea contentTextArea;
    @FXML private ChoiceBox<String> contentTypeChoiceBox;
    @FXML private Button jsonBtn;
    @FXML private Button varNameBtn;

    private TestHttp testHttp;
    private TestFormController testFormController;
    private TestHttpBody testHttpBody;

    private TreeItem<TreeNode> treeItem;

    public HttpBodyControl() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/control/fxml/HttpBody.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        loader.load();
    }

    @FXML
    public void initialize() {
        ObservableList<String> contentTypes = FXCollections.observableArrayList();
        for (ContentType contentType : ContentType.values()) {
            contentTypes.add(contentType.name());
        }
        contentTypeChoiceBox.setItems(contentTypes);

        contentTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener((ov,oldValue,newValue)->{
            if (newValue != null) {
                changeContentType(newValue);
            }
        });
    }

    @FXML
    private void jsonText() {
        String selectedItem = contentTypeChoiceBox.getSelectionModel().getSelectedItem();
        String text = contentTextArea.getText();
        if (selectedItem != null && selectedItem.equals("Json") && !text.equals("")) {
            String json = JsonUtil.parseJson(text);
            contentTextArea.setText(json);
        }
    }

    @FXML private void varNameCheckAction() {
        String elText = contentTextArea.getText().trim();
        List<String> values = VarUtil.getElExpressions(elText);
        if (values.size() < 0){
            DialogUtil.alert(String.format("没有变量，不需要检查"), Alert.AlertType.INFORMATION);
            return;
        }
        varNameBtn.setDisable(true);

        int index = Integer.MAX_VALUE;
        Task<Void> task = VarUtil.buildCheckVarTask(treeItem, values, index);
        task.setOnFailed(e->{
            varNameBtn.setDisable(false);
        });
        task.setOnSucceeded(e->varNameBtn.setDisable(false));
        ThreadUtilFactory.getInstance().submit(task);
    }

    public void initData(TestHttpBody testHttpBody) {
        this.testHttpBody = testHttpBody;
        contentTextArea.setText(testHttpBody.getBody());
        contentTypeChoiceBox.getSelectionModel().select(testHttpBody.getContentType());
    }

    public void setTestHttp(TestHttp testHttp) {
        this.testHttp = testHttp;
    }

    public void setTestFormController(TestFormController testFormController) {
        this.testFormController = testFormController;
    }

    public void changeContentType(String newValue) {
        if (testFormController != null) {
            ContentType contentType = ContentType.valueOf(newValue);
            testFormController.changeContentType(contentType.getContent());
        }
    }

    @FXML
    public void saveBody(ActionEvent actionEvent) throws SQLException {
        if (testHttp == null) {
            testFormController.saveTest(null);
        }
        String contentType = contentTypeChoiceBox.getSelectionModel().getSelectedItem();
        if (StringUtils.isEmpty(contentType)) {
            DialogUtil.alert("参数不能为空", Alert.AlertType.ERROR);
            return;
        }
        if (testHttpBody == null) {
            testHttpBody = new TestHttpBody();
            testHttpBody.setTestHttpId(testHttp.getId());
        }
        testHttpBody.setContentType(contentType);
        testHttpBody.setBody(contentTextArea.getText().trim());
        try {
            TestHttpBodyDao.save(testHttpBody);
            UiUtil.showMessage("保存成功");
        } catch (SQLException e) {
            e.printStackTrace();
            UiUtil.showMessage("保存失败:" + e.getMessage());
        }
    }

    public TestHttpBody getTestHttpBody() {
        TestHttpBody testHttpBody = new TestHttpBody();
        testHttpBody.setTestHttpId(testHttp.getId());
        testHttpBody.setBody(contentTextArea.getText().trim());
        testHttpBody.setContentType(contentTypeChoiceBox.getSelectionModel().getSelectedItem());
        return testHttpBody;
    }

    public void setTreeItem(TreeItem<TreeNode> treeItem) {
        this.treeItem = treeItem;
    }
}
