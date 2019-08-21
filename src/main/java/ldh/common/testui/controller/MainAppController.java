package ldh.common.testui.controller;

import com.google.gson.reflect.TypeToken;
import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ldh.common.testui.assist.template.beetl.BeetlFactory;
import ldh.common.testui.cell.DraggableTreeCell;
import ldh.common.testui.cell.NodeListCell;
import ldh.common.testui.component.TestLogPane;
import ldh.common.testui.constant.BeanType;
import ldh.common.testui.constant.ParamCategory;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.dao.*;
import ldh.common.testui.handle.TreeHandle;
import ldh.common.testui.model.*;
import ldh.common.testui.util.*;
import ldh.common.testui.vo.MethodData;
import ldh.common.testui.vo.VarModel;
import org.apache.commons.dbutils.QueryRunner;
import org.controlsfx.control.StatusBar;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by ldh123 on 2017/6/5.
 */
public class MainAppController implements Initializable {

    public static final TreeNode treeData = new TreeNode("root", TreeNodeType.Root);
    public static final TreeItem<TreeNode> treeRoot = new TreeItem(treeData);
    private TreeHandle treeHandle;

    private double startMoveX = -1;
    private double startMoveY = -1;
    private Boolean dragging = false;
    private double lastX = 0.0d;
    private double lastY = 0.0d;
    private double lastWidth = 0.0d;
    private double lastHeight = 0.0d;

    @FXML TabPane tabPane;
    @FXML TreeView<TreeNode> treeView;
//    @FXML TextArea logTextArea;
//    @FXML HTMLEditor logTextArea;
    @FXML WebView logTextArea;
    @FXML Button createProjectBtn;
    @FXML StatusBar statusBar;
//    @FXML MasterDetailPane masterDetailPane;
    @FXML ListView<TestLog> logList;
    @FXML Region westEdgePane;
    @FXML HBox logContainerPane;
    @FXML MenuItem exportItem;
    @FXML MenuItem importItem;
    @FXML Menu fileMenu;
    @FXML Menu functionMenu;

    private FileChooser fileChooser = new FileChooser();
    private DirectoryChooser directoryChooser = new DirectoryChooser();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        treeHandle = new TreeHandle(treeView, tabPane);
        treeRoot.setValue(treeData);

//        treeView.setCellFactory(new ObjectTreeCell<TreeNode>((treeNode)->{
//            return new Object[] {treeNode.getName(), buildTreeItemGraphic(treeNode)};
//        }));
        treeView.setCellFactory((treeView->new DraggableTreeCell(treeView)));
        treeView.setRoot(treeRoot);
        treeRoot.setExpanded(true);
        treeView.setShowRoot(false);

        treeView.setOnMouseClicked(e->treeClick(e));
        loadData();
//        LogThread.getInstance().setTextArea(logTextArea);
        LogUtil.setTextArea(logTextArea);


        createProjectBtn.setGraphic(RegionUtil.createIcon(new MaterialDesignIconView(MaterialDesignIcon.HOME), 25, Color.STEELBLUE));
        exportItem.setGraphic(RegionUtil.createIcon(new MaterialDesignIconView(MaterialDesignIcon.EXPORT), 20, Color.STEELBLUE));
        importItem.setGraphic(RegionUtil.createIcon(new MaterialDesignIconView(MaterialDesignIcon.IMPORT), 20, Color.STEELBLUE));
        fileMenu.setGraphic(RegionUtil.createIcon(new MaterialDesignIconView(MaterialDesignIcon.FILE), 20, Color.STEELBLUE));
        functionMenu.setGraphic(RegionUtil.createIcon(new MaterialDesignIconView(MaterialDesignIcon.FUNCTION), 20, Color.STEELBLUE));

        UiUtil.STATUSBAR = statusBar;
        UiUtil.TEST_LOG = logList;

        logList.setCellFactory(new NodeListCell<TestLog>(testLog -> {
            HBox hBox = new HBox();
            hBox.setSpacing(10);
            Label headLabel = new Label("", RegionUtil.createIcon(new MaterialDesignIconView(MaterialDesignIcon.HELP), 20, Color.GREEN));
            Label titleLabel = new Label(testLog.getName());
            Region hgrow = new Region();
            Label createTimeLabel = new Label(DateUtil.format(testLog.getCreateTime(), "yyyy-MM-dd HH:mm"));
            hBox.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(hgrow, Priority.ALWAYS);

            testLogItemIcon(testLog, headLabel);
            testLog.getSuccess().addListener((ob, o, n)->{
                testLogItemIcon(testLog, headLabel);
            });

            hBox.getChildren().addAll(headLabel, titleLabel, hgrow, createTimeLabel);
            return hBox;
        }));

        logList.setOnMouseClicked((MouseEvent e) ->{
            if (e.getClickCount() != 2) return;
            TestLog testLog1 = logList.getSelectionModel().getSelectedItem();
            if (testLog1 == null) return;
            try {
                showLogReport(testLog1);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        });

        ContextMenu testLogContextMenu = new ContextMenu();
        MenuItem removeTestLog = new MenuItem("删除");
        removeTestLog.setOnAction(e->removeTestLog());
        testLogContextMenu.getItems().addAll(removeTestLog);
        logList.setContextMenu(testLogContextMenu);

        westEdgePane.setOnMouseDragged(e->changeWestEdgeSize(e));
        westEdgePane.setOnMouseReleased(e->endChangeWestEdgeSize(e));
//        westEdgePane.setOnDragDetected(e->startChangeWestEdgeSize(e));
        westEdgePane.setOnMouseEntered(e->westEdgeChangeSize(e));
        westEdgePane.setOnMousePressed(e->startWestEdgeChangeSize(e));

        UiUtil.MAIN_TAB_PANE = tabPane;
    }

    @FXML void createProject(ActionEvent event) {
        treeHandle.addGroup(TreeNodeType.Node, true, null);
    }

    @FXML void clearConsole(ActionEvent event) {
        LogUtil.clean();
//        logTextArea.setHtmlText("");
        logTextArea.getEngine().loadContent("");
    }

    @FXML void showLog() {
        if(logContainerPane.isVisible()) {
            logContainerPane.setVisible(false);
        } else {
            logContainerPane.setVisible(true);
        }
    }

    private void treeClick(MouseEvent ee) {
        TreeItem<TreeNode> treeItem = treeView.getSelectionModel().getSelectedItem();
        treeHandle.initEvent(treeItem);
        if (ee.getClickCount() == 2) {
            treeHandle.doubleClick(treeItem);
        }
    }

    private void removeTestLog() {
        try {
            UiUtil.showMessage("开始删除日志");
            TestLog testLog = logList.getSelectionModel().getSelectedItem();
            if (testLog == null) return;
            logList.getItems().remove(testLog);
            TestLogDao.delete(testLog);
            UiUtil.showMessage("删除日志成功");
        } catch (Exception e) {
            e.printStackTrace();
            UiUtil.showMessage("删除日志失败：" + e.getMessage());
        }
    }

    private void loadData() {
        Task<Void> loadDataTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                List<TreeNode> treeNodeList = TreeDao.getAll();
                List<TreeNode> treeTreeNode = TreeUtil.tree(treeNodeList);
                TreeItem<TreeNode> root = treeView.getRoot();
                root.setExpanded(true);
                boolean isManyNodes = treeTreeNode.size() > 1;
                for (TreeNode tree : treeTreeNode) {
                    TreeItem<TreeNode> parent = new TreeItem(tree);
                    root.getChildren().add(parent);
                    if (!isManyNodes) parent.setExpanded(true);
                    handleChildren(parent, tree.getChildren());
                }

                List<TestLog> testLogs = TestLogDao.getLastedNum(100);
                logList.getItems().addAll(testLogs);

//                loadVarData(treeRoot); // 加载变量
                return null;
            }
        };
        loadDataTask.setOnFailed(e->e.getSource().getException().printStackTrace());
        ThreadUtilFactory.getInstance().submit(loadDataTask);
    }

    public static void loadVarData(TreeItem<TreeNode> treeItem) {
        handleVarTreeNode(treeItem);
        TreeItem<TreeNode> tmp = treeItem;
        for (TreeItem<TreeNode> child : tmp.getChildren()) {
//            if (child.getValue().getTreeNodeType() != TreeNodeType.Method) {
//                loadVarData(child);
//                continue;
//            }
            handleVarTreeNode(child);
            loadVarData(child);
        }
    }

    private static void handleVarTreeNode(TreeItem<TreeNode> treeItem) {
        try {
            if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Method) {
                List<TestMethod> testMethods = TestMethodDao.getByTreeNodeId(treeItem.getValue().getId());
                for (TestMethod testMethod : testMethods) {
                    List<TestMethodData> testMethodDataList = TestMethodDataDao.getByTestMethodId(testMethod.getId());
                    Class testClazz = MethodUtil.forClass(testMethod.getClassName());
                    Method checkMethod = MethodUtil.forMethod(testClazz, testMethod.getMethodName());

                    for (TestMethodData testMethodData : testMethodDataList) {
                        Map<String, MethodData> data = JsonUtil.toObject(testMethodData.getData(), new TypeToken<Map<String, MethodData>>(){}.getType());
                        for (Map.Entry<String, MethodData> entry : data.entrySet()) {
                            if (VarUtil.isPutVar(entry.getValue().getData())) {
                                Class clazz = checkMethod.getReturnType();
                                Method method = checkMethod;

                                try {
                                    MethodData methodData = entry.getValue();
                                    String key = methodData.getKey();
                                    if (methodData.getConvert() == null) { // 返回类型
                                        String[] keyArray = key.split(" ");
                                        if (keyArray.length == 2) {
                                            method = testClazz.getDeclaredMethod(keyArray[1]);
                                            clazz = method.getReturnType();
                                        }
                                    }

                                    VarModel varModel = new VarModel(clazz, method.getGenericReturnType());
                                    varModel.setVarName(VarUtil.getPutVarName(entry.getValue().getData()));
                                    VarFactory.getInstance().cache(treeItem, varModel);
                                } catch (Exception e) {
//                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Bean) {
                BeanCheck beanCheck = BeanCheckDao.getByTreeNodeId(treeItem.getValue().getId());
                if (beanCheck == null) return;
                String varName = VarUtil.getVarName(beanCheck.getCheckName());
                if (!VarFactory.getInstance().isHave(treeItem, varName)) return;
                VarModel rootVarModel = VarFactory.getInstance().getCache(treeItem, varName);
                if (beanCheck.getContent() != null && beanCheck.getContent().startsWith("[")) {
                    if (beanCheck.getBeanType() == BeanType.Object) {
                        List<Map<String, BeanData>> lists = JsonUtil.toObject(beanCheck.getContent(), new TypeToken<List<Map<String, BeanData>>>() {}.getType());
                        for(Map<String, BeanData> map : lists) {
                            map.forEach((key, value)->{
                                if (!VarUtil.isPutVar(value.getExceptedValue())) return;
                                VarModel varModel = null;
                                String varPutName = VarUtil.getPutVarName(value.getExceptedValue());
                                try {
                                    if (rootVarModel.isMap() || rootVarModel.isListMap()) {
                                        String methodName = key;
                                        Method method = rootVarModel.getBeanClazz().getDeclaredMethod(methodName, new Class[]{});
                                        varModel = new VarModel(varPutName, method.getReturnType(), method.getGenericReturnType(), rootVarModel.getTypeVariableMap());
                                    } else if (rootVarModel.isBean()) {
                                        String methodName = key;
                                        Method method = rootVarModel.getBeanClazz().getDeclaredMethod(methodName, new Class[]{});
                                        varModel = new VarModel(varPutName, method.getReturnType(), method.getGenericReturnType(), rootVarModel.getTypeVariableMap());
                                    } else if (rootVarModel.isTBean()) {
                                        String methodName = key;
                                        Method method = rootVarModel.getBeanClazz().getDeclaredMethod(methodName, new Class[]{});
                                        varModel = new VarModel(varPutName, method.getReturnType(), method.getGenericReturnType(), rootVarModel.getTypeVariableMap());
                                    } else if (rootVarModel.isListOrSet() || rootVarModel.isArray()) {
                                        String methodName = key;
                                        Method method = rootVarModel.getBeanClazz().getDeclaredMethod(methodName, new Class[]{});
                                        varModel = new VarModel(varPutName, method.getReturnType(), method.getGenericReturnType(), rootVarModel.getTypeVariableMap());
                                    }
                                    VarFactory.getInstance().cache(treeItem, varModel);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    } else if (beanCheck.getBeanType() == BeanType.Json) {
                        List<BeanData> lists = JsonUtil.toObject(beanCheck.getContent(), new TypeToken<List<BeanData>>() {}.getType());
                        // TODO 忽略
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleChildren(TreeItem<TreeNode> root, List<TreeNode> children) {
        if (children == null) return;
        for (TreeNode treeNode : children) {
            TreeItem<TreeNode> parent = new TreeItem(treeNode);
            root.getChildren().add(parent);
            loadFunction(parent);
            if (treeNode.getPath() == null) {
                String rootPath = root.getValue().getPath() == null ? "" : root.getValue().getPath() + "-";
                treeNode.setPath(rootPath + root.getValue().getId());
                try {
                    TreeDao.save(treeNode);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            parent.setExpanded(false);
            handleChildren(parent, treeNode.getChildren());
        }
    }

    private void loadFunction(TreeItem<TreeNode> treeItem) {
        try {
            if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Param) {
                List<ParamModel> paramModels = ParamDao.getByTreeNodeId(treeItem.getValue().getId());
                for (ParamModel paramModel : paramModels) {
                    if (paramModel.getParamCategory() != ParamCategory.Other_jar) continue;
                    String dir = paramModel.getValue() + File.separator;
                    LibLoaderFactory.getInstance().loadLib(dir);
                }
            }
            if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Function) {
                List<CommonFun> commonFuns = CommonFunDao.getByTreeNodeId(treeItem.getValue().getId());
                for (CommonFun commonFun : commonFuns) {
                    BeetlFactory.getInstance().addVarClass(commonFun.getName(), MethodUtil.forClass(commonFun.getClassName()).newInstance());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLogReport(TestLog testLog) throws SQLException {
        System.out.println("testLogId:" + testLog.getId());
        Stage stage = new Stage();
        TestLogPane logPane = new TestLogPane(testLog);
        Scene scene = new Scene(logPane, 1000, 400);
        scene.getStylesheets().add(this.getClass().getResource("/css/LogPane.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("测试报表");
        stage.show();

        logPane.calcPos();
    }

    private void testLogItemIcon(TestLog testLog, Label headLabel) {
        GlyphIcon icon = testLogItemIcon(testLog);
        headLabel.setGraphic(icon);
    }

    private GlyphIcon testLogItemIcon(TestLog testLog) {
        int successNum = testLog.getSuccessNum();
        int failureNum = testLog.getFailureNum();
        if (successNum > 0 && failureNum == 0) { // 无失败为成功
            return RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.CHECK), 20, Color.GREEN);
        } else if (failureNum > 0 && successNum != 0 && failureNum >= successNum) {  // 失败个数大于成功个数，为失败
            return RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.CLOSE), 20, Color.RED);
        } else {
            return RegionUtil.createIcon(new FontAwesomeIconView(FontAwesomeIcon.EXCLAMATION_CIRCLE), 20, Color.GREENYELLOW);
        }
    }

    private void westEdgeChangeSize(MouseEvent e) {
        westEdgePane.setCursor(Cursor.H_RESIZE);
    }

    private void endChangeWestEdgeSize(MouseEvent e) {
        if (dragging) {
            dragging = false;
        }
    }

    private void changeWestEdgeSize(MouseEvent evt) {
        if (dragging) {
            double endMoveX = evt.getScreenX();
            double endMoveY = evt.getScreenY();
            double changeW = endMoveX - this.startMoveX;
            double changeH = endMoveY - this.startMoveY;
            logContainerPane.setPrefWidth(lastWidth - changeW);
            logContainerPane.setMaxWidth(lastWidth - changeW);
        }
        evt.consume();
    }

    private void startWestEdgeChangeSize(MouseEvent evt) {
        startMoveX = evt.getScreenX();
        startMoveY = evt.getScreenY();
        dragging = true;
        lastHeight = logContainerPane.getHeight();
        lastWidth = logContainerPane.getPrefWidth();
        lastX = logContainerPane.getScene().getWindow().getX();
        lastY = logContainerPane.getScene().getWindow().getY();
        evt.consume();
    }

    public void exportData(ActionEvent actionEvent) {
        directoryChooser.setTitle("选择需要保存的目录");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        File file = directoryChooser.showDialog(UiUtil.STAGE.getOwner());
        if (file == null) {
            DialogUtil.alert("请选择保存的目录", Alert.AlertType.WARNING);
            return;
        }
        Task<Void> exportTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                QueryRunner queryRunner = DbUtils.getQueryRunner();
                H2Util.exportAllTable(queryRunner, file.getPath(), "test-" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + ".sql");
                Platform.runLater(()->{
                    DialogUtil.alert("导出成功", Alert.AlertType.INFORMATION);
                });
                return null;
            }
        };
        ThreadUtilFactory.getInstance().submit(exportTask);
    }

    public void importData(ActionEvent actionEvent) {
        fileChooser.setTitle("选择需要导入的SQL文件");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("sql", "*.sql"));
        File file = fileChooser.showOpenDialog(UiUtil.STAGE.getOwner());
        if (file == null) {
            DialogUtil.alert("请选择需要导入的文件", Alert.AlertType.WARNING);
            return;
        }
        DialogUtil.confirm("导入前，最好做好备份。确定要导入数据吗？", (Void)->{
            Task<Void> exportTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    QueryRunner queryRunner = DbUtils.getQueryRunner();
                    H2Util.importSystemData(queryRunner, file.getPath());
                    loadData();
                    Platform.runLater(()->{

                        DialogUtil.alert("导入成功", Alert.AlertType.INFORMATION);
                    });
                    return null;
                }
            };
            ThreadUtilFactory.getInstance().submit(exportTask);
        });
    }
}
