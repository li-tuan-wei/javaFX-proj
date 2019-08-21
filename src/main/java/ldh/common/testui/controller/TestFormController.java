package ldh.common.testui.controller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ldh.common.testui.component.CodeTextArea;
import ldh.common.testui.constant.HttpMethod;
import ldh.common.testui.component.HttpBodyControl;
import ldh.common.testui.component.HttpParamControl;
import ldh.common.testui.constant.ParamType;
import ldh.common.testui.dao.*;
import ldh.common.testui.handle.RunTreeItem;
import ldh.common.testui.model.*;
import ldh.common.testui.util.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * Created by ldh123 on 2017/6/5.
 */
public class TestFormController extends BaseController implements Initializable{

    @FXML private ChoiceBox<HttpMethod> methodChoiceBox;
    @FXML private TextField urlTextField;

    @FXML private HttpParamControl headerControl;
    @FXML private HttpParamControl paramControl;
//    @FXML private HttpParamControl cookieControl;
    @FXML private HttpBodyControl bodyControl;
//    @FXML private CodeTextArea testHttpResultTextArea;
    @FXML private TextArea testHttpResultTextArea;
    @FXML private Tab bodyTab;
    @FXML private TabPane paramTabPane;
    @FXML private Button runBtn;

    private TestHttp testHttp = null;

    public void saveTest(ActionEvent actionEvent) throws SQLException {
        if (testHttp == null) {
            testHttp = new TestHttp();
            testHttp.setTreeNodeId(treeItem.getValue().getId());
        }
        testHttp.setUrl(urlTextField.getText().trim());
        testHttp.setMethod(methodChoiceBox.getSelectionModel().getSelectedItem());
        TestHttpDao.save(testHttp);
        setTestHttpTest(testHttp);
    }

    public void runTest(ActionEvent actionEvent) throws SQLException {
        Task<Void> task = new Task() {

            @Override
            protected Void call() throws Exception {
                try {
                    saveTest(new ActionEvent());
                    TestLog testLog = TestLog.buildTestLog(treeItem.getValue().getName(), treeItem.getValue().getTreeNodeType().name());
                    TestLogDao.insert(testLog);
                    Map<String, Object> paramMap = TreeUtil.buildParamMap(treeItem);
                    RunTreeItem.runTreeItem(treeItem, paramMap, null, (result)->{
                        Platform.runLater(()->{
                            if (result != null) {
                                try {
                                    if (result.startsWith("{") || result.startsWith("[")) {
                                        String json = JsonUtil.parseJson(result);
                                        testHttpResultTextArea.setText(json);
                                    } else {
                                        testHttpResultTextArea.setText(result);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }, testLog, false);
                    return null;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        task.setOnFailed((e)-> runBtn.setDisable(false));
        task.setOnSucceeded(e->runBtn.setDisable(false));
        runBtn.setDisable(true);
        ThreadUtilFactory.getInstance().submit(task);
//        new Thread(task).start();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        methodChoiceBox.setItems(FXCollections.observableArrayList(Arrays.asList(HttpMethod.values())));
        bodyControl.setTestFormController(this);

        headerControl.setParamType(ParamType.Header);
        paramControl.setParamType(ParamType.Param);
//        cookieControl.setParamType(ParamType.Cookie);

        testHttpResultTextArea.setWrapText(true);
        testHttpResultTextArea.setFocusTraversable(false);
    }

    public void changeContentType(String contentType) {
        headerControl.changeContentType(contentType);
    }

    @Override
    public void setTreeItem(TreeItem<TreeNode> treeItem) {
        this.treeItem = treeItem;
        bodyControl.setTreeItem(treeItem);
        new Thread(new Task<Void>(){

            @Override
            protected Void call() throws Exception {
                loadData();
                return null;
            }
        }).start();
    }

    private void loadData() {
        try {
            List<TestHttp> testHttpList = TestHttpDao.getByTreeNodeId(treeItem.getValue().getId());
            if (testHttpList.size() < 1) return;
            testHttp = testHttpList.get(0);
            Platform.runLater(()->{
                methodChoiceBox.getSelectionModel().select(testHttp.getMethod());
                urlTextField.setText(testHttp.getUrl());

                setTestHttpTest(testHttp);
            });

            List<TestHttpParam> testHttpParams = TestHttpParamDao.getByTestHttpId(testHttp.getId());
            Platform.runLater(()->{
                for (TestHttpParam testHttpParam : testHttpParams) {
                    if (testHttpParam.getParamType() == ParamType.Header) {
                        headerControl.initData(testHttpParam);
                    } else if (testHttpParam.getParamType() == ParamType.Param) {
                        paramControl.initData(testHttpParam);
                    } else if (testHttpParam.getParamType() == ParamType.Cookie) {
//                        cookieControl.initData(testHttpParam);
                    } else {
                        new RuntimeException("not support");
                    }
                }
            });
            List<TestHttpBody> testHttpBodys = TestHttpBodyDao.getByTestHttpId(testHttp.getId());
            Platform.runLater(()->{
                for (TestHttpBody testHttpBody : testHttpBodys) {
                    bodyControl.initData(testHttpBody);
                }
                if (testHttpBodys.size() > 0) paramTabPane.getSelectionModel().select(bodyTab);
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setTestHttpTest(TestHttp testHttp) {
        headerControl.setTestHttp(testHttp);
        paramControl.setTestHttp(testHttp);
//        cookieControl.setTestHttp(testHttp);
        bodyControl.setTestHttp(testHttp);
    }
}
