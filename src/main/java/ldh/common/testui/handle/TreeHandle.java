package ldh.common.testui.handle;

import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import ldh.common.testui.constant.*;
import ldh.common.testui.controller.MainAppController;
import ldh.common.testui.dao.*;
import ldh.common.testui.model.*;

import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import ldh.common.testui.swagger.Parameter;
import ldh.common.testui.swagger.PathInfo;
import ldh.common.testui.swagger.Swagger;
import ldh.common.testui.swagger.Tag;
import ldh.common.testui.util.*;
import ldh.common.testui.vo.SqlCheck;
import ldh.common.testui.vo.SqlCheckData;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by ldh on 2018/3/17.
 */
public class TreeHandle {

    private final static Logger LOGGER = Logger.getLogger(TreeHandle.class.getSimpleName());

    private TreeView<TreeNode> treeView;
    private TabPane tabPane;
    private ContextMenu contextMenu = new ContextMenu();

    public TreeHandle(TreeView<TreeNode> treeView, TabPane tabPane) {
        this.tabPane = tabPane;
        this.treeView = treeView;
    }

    public void doubleClick(TreeItem<TreeNode> treeItem) {
        if (treeItem == null || treeItem.getValue() == null) return;
        if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Param) {
            Tab tab = getExistedTab(treeItem.getValue());
            if (tab == null) {
                tab = new Tab("属性管理");
                tab.setUserData(treeItem.getValue());
                tabPane.getTabs().add(tab);
                tabPane.getSelectionModel().select(tab);
                Parent parent = RegionUtil.paramPane(treeItem);
                tab.setContent(parent);
            } else {
                tabPane.getSelectionModel().select(tab);
            }
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Data) {
            openTab(treeItem, RegionUtil.sqlPane(treeItem));
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Http) {
            openTab(treeItem, RegionUtil.httpPane(treeItem));
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.SqlCheckData) {
            openTab(treeItem, RegionUtil.sqlCheckPane(treeItem));
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Method) {
            openTab(treeItem, RegionUtil.classPane(treeItem));
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.BeanVar) {
            openTab(treeItem, RegionUtil.beanVarPane(treeItem));
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Bean) {
            openTab(treeItem, RegionUtil.beanPane(treeItem));
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Function) {
            openTab(treeItem, RegionUtil.functionPane(treeItem));
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.ExportData) {
            openTab(treeItem, RegionUtil.exportDataPane(treeItem));
        }
    }

    public void initEvent(TreeItem<TreeNode> treeItem) {
        contextMenu.getItems().clear();
        if (treeItem == null) {
            MenuItem addGroup = new MenuItem("添加项目", buildGraphic(new FontAwesomeIconView(FontAwesomeIcon.PRODUCT_HUNT)));
            addGroup.setOnAction(e->addGroup(TreeNodeType.Node, true, null));
            contextMenu.getItems().add(addGroup);
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Node){
            ParamModel springParamModel = SpringInitFactory.getInstance().getSpringParamModel(treeItem);
            if (springParamModel != null) {
                MenuItem runSpring = new MenuItem("运行Spring", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.PLAY)));
                runSpring.setOnAction(this::runSpring);
                contextMenu.getItems().add(runSpring);
            }

            if (!TreeUtil.hasTreeNodeType(treeItem, TreeNodeType.Node)) {
                MenuItem addTest = new MenuItem("添加测试场景", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.PLUS)));
                addTest.setOnAction(te->addCase(te));
                contextMenu.getItems().add(addTest);

                if (!TreeUtil.hasTreeNodeType(treeItem, TreeNodeType.Param)) {
                    MenuItem addParam = new MenuItem("添加属性", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.PARKING)));
                    addParam.setOnAction(this::addParam);
                    contextMenu.getItems().add(addParam);
                }
            } else {
                MenuItem addGroup = new MenuItem("添加组", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.PLUS)));
                addGroup.setOnAction(e0->addGroup(TreeNodeType.Node, false, null));
                contextMenu.getItems().add(addGroup);
            }
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Case) {
            if (DebugCacheFactory.getInstance().isDebug()) {
                MenuItem cleanDebug = new MenuItem("清理Debug缓存", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.FORMAT_CLEAR)));
                cleanDebug.setOnAction(te->cleanDebug(te));
                contextMenu.getItems().add(cleanDebug);
            }
            MenuItem runCase = new MenuItem("运行", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.PLAY)));
            runCase.setOnAction(te->runCase(te, false));
            contextMenu.getItems().add(runCase);

            MenuItem debugRunCase = new MenuItem(debugText(treeItem.getValue()), buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.BUG)));
            debugRunCase.setOnAction(te->runCase(te, !DebugCacheFactory.getInstance().isDebug()));
            contextMenu.getItems().add(debugRunCase);

            MenuItem importSwagger = new MenuItem("swagger导入", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.IMPORT)));
            importSwagger.setOnAction(te->importSwagger(te));
            contextMenu.getItems().add(importSwagger);

            if (!TreeUtil.hasTreeNodeType(treeItem, TreeNodeType.Param)) {
                MenuItem addParam = new MenuItem("添加属性", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.PARKING)));
                addParam.setOnAction(this::addParam);
                contextMenu.getItems().add(addParam);
            }

            if (!TreeUtil.hasTreeNodeType(treeItem, TreeNodeType.BeanVar)) {
                MenuItem addBeanVar = new MenuItem("添加变量", buildGraphic(new FontAwesomeIconView(FontAwesomeIcon.INFO)));
                addBeanVar.setOnAction(this::addBeanVar);
                contextMenu.getItems().add(addBeanVar);
            }

            if (!TreeUtil.hasTreeNodeType(treeItem, TreeNodeType.Function)) {
                MenuItem addFun = new MenuItem("添加公用方法", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.FUNCTION)));
                addFun.setOnAction(this::addFun);
                contextMenu.getItems().add(addFun);
            }

            MenuItem addPrecondition = new MenuItem("添加前置SQL", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.DATABASE_PLUS)));
            addPrecondition.setOnAction(this::addPrecondition);
            contextMenu.getItems().add(addPrecondition);

            MenuItem addTest = new MenuItem("添加测试用例", buildGraphic(new FontAwesomeIconView(FontAwesomeIcon.TUMBLR)));
            addTest.setOnAction(this::addTest);
            contextMenu.getItems().add(addTest);

            MenuItem afterSql = new MenuItem("添加后置SQL", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.DATABASE_MINUS)));
            afterSql.setOnAction(this::addLastCondition);
            contextMenu.getItems().add(afterSql);

            MenuItem checkVarName = new MenuItem("检查变量", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.VIEW_ARRAY)));
            checkVarName.setOnAction(this::checkVarName);
            contextMenu.getItems().add(checkVarName);
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Test) {
            if (DebugCacheFactory.getInstance().isDebug()) {
                MenuItem cleanDebug = new MenuItem("清理Debug缓存", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.FORMAT_CLEAR)));
                cleanDebug.setOnAction(te->cleanDebug(te));
                contextMenu.getItems().add(cleanDebug);
            }
            MenuItem runTest = new MenuItem("运行", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.PLAY)));
            runTest.setOnAction(te->runTest(te));
            contextMenu.getItems().add(runTest);

            MenuItem debugRunTest = new MenuItem(debugText(treeItem.getValue()), buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.BUG)));
            debugRunTest.setOnAction(te->debugRunTest(te));
            contextMenu.getItems().add(debugRunTest);

            if (!TreeUtil.hasTreeNodeType(treeItem, TreeNodeType.Param)) {
                MenuItem addParam = new MenuItem("添加属性", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.PARKING)));
                addParam.setOnAction(this::addParam);
                contextMenu.getItems().add(addParam);
            }

            MenuItem addPrecondition = new MenuItem("添加前置SQL", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.DATABASE_PLUS)));
            addPrecondition.setOnAction(this::addPrecondition);
            contextMenu.getItems().add(addPrecondition);

            MenuItem addMethod = new MenuItem("添加方法测试", buildGraphic(new FontAwesomeIconView(FontAwesomeIcon.MAXCDN)));
            addMethod.setOnAction(this::addMethodInvoke);
            contextMenu.getItems().add(addMethod);

            MenuItem addHttp = new MenuItem("添加接口测试", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.CAST_CONNECTED)));
            addHttp.setOnAction(this::addHttp);
            contextMenu.getItems().add(addHttp);

            MenuItem afterSql = new MenuItem("添加后置SQL", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.DATABASE_MINUS)));
            afterSql.setOnAction(this::addLastCondition);
            contextMenu.getItems().add(afterSql);

            MenuItem exportData = new MenuItem("导出数据", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.EXPORT)));
            exportData.setOnAction(this::exportData);
            contextMenu.getItems().add(exportData);
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Method || treeItem.getValue().getTreeNodeType() == TreeNodeType.Http) {
            if (DebugCacheFactory.getInstance().isDebug()) {
                MenuItem cleanDebug = new MenuItem("清理Debug缓存", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.FORMAT_CLEAR)));
                cleanDebug.setOnAction(te->cleanDebug(te));
                contextMenu.getItems().add(cleanDebug);
            }

            MenuItem runTest = new MenuItem("debug运行", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.BUG)));
            runTest.setOnAction(te->debugRunNode(treeItem));
            contextMenu.getItems().add(runTest);

            MenuItem checkResultData = new MenuItem("验证对象验证", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.COMPARE)));
            checkResultData.setOnAction(this::checkResultData);
            contextMenu.getItems().add(checkResultData);

            MenuItem checkData = new MenuItem("数据库数据验证", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.DATABASE)));
            checkData.setOnAction(this::checkData);
            contextMenu.getItems().add(checkData);

            MenuItem addMethod = new MenuItem("添加方法测试", buildGraphic(new FontAwesomeIconView(FontAwesomeIcon.MAXCDN)));
            addMethod.setOnAction(this::addMethodInvoke);
            contextMenu.getItems().add(addMethod);
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Bean
                || treeItem.getValue().getTreeNodeType() == TreeNodeType.SqlCheckData
                || treeItem.getValue().getTreeNodeType() == TreeNodeType.ExportData) {
            if (DebugCacheFactory.getInstance().isDebug()) {
                MenuItem runTest = new MenuItem("debug运行", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.BUG)));
                runTest.setOnAction(te->debugRunNode(treeItem));
                contextMenu.getItems().add(runTest);
            }
            if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Bean) {
                MenuItem checkBean = new MenuItem("添加对象验证", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.COMPARE)));
                checkBean.setOnAction(this::checkResultData);
                contextMenu.getItems().add(checkBean);
            }
        }

        if (treeItem != null && treeItem.getValue().getEnable()) {
            MenuItem disable = new MenuItem("无效", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.SHUFFLE_DISABLED)));
            disable.setOnAction(e1->disable(e1));
            contextMenu.getItems().add(disable);
        } else {
            MenuItem enable = new MenuItem("有效效", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.RAY_END_ARROW)));
            enable.setOnAction(e1->enable(e1));
            contextMenu.getItems().add(enable);
        }

        if (treeItem != null && treeItem.getValue().getTreeNodeType() != TreeNodeType.Node) {
            MenuItem copy = new MenuItem("复制", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.CONTENT_COPY)));
            copy.setOnAction(e1->copy(e1));
            contextMenu.getItems().add(copy);
        }

        if (treeItem != null && !(treeItem.getValue().getTreeNodeType() == TreeNodeType.Param
                || treeItem.getValue().getTreeNodeType() == TreeNodeType.BeanVar)) {
            MenuItem paste = new MenuItem("粘贴", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.CONTENT_PASTE)));
            paste.setOnAction(e1->paste(e1));
            contextMenu.getItems().add(paste);
        }


        MenuItem edit = new MenuItem("编辑", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.PENCIL)));
        edit.setOnAction(e1->edit(e1));
        contextMenu.getItems().add(edit);

        MenuItem remove = new MenuItem("删除", buildGraphic(new MaterialDesignIconView(MaterialDesignIcon.DELETE)));
        remove.setOnAction(e2->remove(e2));
        contextMenu.getItems().add(remove);

        treeView.setContextMenu(contextMenu);
    }

    public void addGroup(TreeNodeType treeNodeType, boolean isRoot, Function<TreeItem<TreeNode>, Object> function) {
       addOrEditGroup(null, treeNodeType, isRoot, function);
    }

    public void addOrEditGroup(TreeNode treeNode, TreeNodeType treeNodeType, boolean isRoot, Function<TreeItem<TreeNode>, Object> function) {
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<Pair<String, String>>();
        dialog.setTitle("节点信息");
        dialog.setHeaderText("按要求填写节点信息");

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        // Create the username and password labels and fields.
        GridPane grid = new GridPane();
        grid.setGridLinesVisible(false);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 10, 10, 10));

        TextField name = new TextField();
        name.setPromptText("节点名称");
        name.setPrefWidth(200);
        if (treeNode != null) name.setText(treeNode.getName());

        grid.add(new Label("      节点名称:"), 0, 0);
        grid.add(name, 1, 0);

        TextArea textArea = new TextArea();
        if (treeNode != null) textArea.setText(treeNode.getDesc());
        grid.add(new Label("     备注:"), 0, 1);
        grid.add(textArea, 1, 1);

        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        if (treeNode == null) loginButton.setDisable(true);
        name.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });
        dialog.getDialogPane().setContent(grid);
        Platform.runLater(() -> name.requestFocus());
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<String, String>(name.getText(), textArea.getText());
            }
            return null;
        });

        Optional<Pair<String, String>> result = dialog.showAndWait();

        result.ifPresent(nameAndTime -> {
            TreeItem<TreeNode> treeItem = new TreeItem<>();
            TreeNode treeNode2 = new TreeNode(nameAndTime.getKey(), treeNodeType);
            treeNode2.setDesc(nameAndTime.getValue());
            try {
                if (treeNode != null) {
                    treeNode.setName(treeNode2.getName());
                    treeNode.setDesc(treeNode2.getDesc());
                    TreeDao.save(treeNode);
                    treeItem.setValue(treeNode);
                    treeView.refresh();
                } else {
                    TreeItem<TreeNode> selectNode = treeView.getSelectionModel().getSelectedItem();
                    if (selectNode != null && !isRoot) {
                        treeNode2.setParentId(selectNode.getValue().getId());
                        treeNode2.setIndex(selectNode.getChildren().size() + 1);
                        treeNode2.setPath(selectNode.getValue().getPath() + "-" + selectNode.getValue().getId());
                        selectNode.getChildren().add(treeItem);
                        selectNode.setExpanded(true);
                    } else {
                        treeNode2.setIndex(1);
                        MaterialDesignIconView icon = new MaterialDesignIconView(MaterialDesignIcon.HOME);
                        treeItem.setGraphic(icon);
                        MainAppController.treeRoot.getChildren().add(treeItem);
                    }
                    treeItem.setValue(treeNode2);
                    TreeDao.save(treeNode2);
                    treeItem.setValue(treeNode2);
                    treeView.refresh();
                }

                if (function != null) {
                    function.apply(treeItem);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @FXML
    public void remove(ActionEvent actionEvent) {
        TreeItem<TreeNode> treeItem = treeView.getSelectionModel().getSelectedItem();
        if (treeItem == null) {
            return;
        }
        TreeItem<TreeNode> parent = treeItem.getParent();
        try {
            TreeDao.delete(treeItem.getValue());
            parent.getChildren().remove(treeItem);
            closeTab(treeItem.getValue());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addHttp(ActionEvent actionEvent) {
        addGroup(TreeNodeType.Http, false, (treeItem)->{
            Tab tab = new Tab(treeItem.getValue().getName());
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            tab.setUserData(treeItem.getValue());
            tab.setContent(RegionUtil.httpPane(treeItem));
            return null;
        });
    }

    private void addCase(ActionEvent actionEvent) {
        addGroup(TreeNodeType.Case, false, null);
    }

    private void cleanDebug(ActionEvent actionEvent) {
        DebugCacheFactory.getInstance().clean();
    }

    private void runCase(ActionEvent actionEvent, boolean isDebug) {
        TreeItem<TreeNode> selectNode = treeView.getSelectionModel().getSelectedItem();
        DebugCacheFactory.getInstance().clean();
        RunTreeItem.cleanTreeNode(selectNode);
        DebugCacheFactory.getInstance().debug(isDebug);

//        if (!isDebug) {
//            RunTreeItem.cleanTreeNode(selectNode);
//            UiUtil.showMessage("清除Debug缓存成功");
////            return;
//        }

        taskRun(selectNode, isDebug);
    }

    private void runSpring(ActionEvent actionEvent) {
        TreeItem<TreeNode> selectNode = treeView.getSelectionModel().getSelectedItem();
        taskRunSpring(selectNode);
    }

    private void runTest(ActionEvent actionEvent) {
        TreeItem<TreeNode> selectNode = treeView.getSelectionModel().getSelectedItem();
        taskRun(selectNode, false);
    }

    private void debugRunTest(ActionEvent actionEvent) {
        TreeItem<TreeNode> selectNode = treeView.getSelectionModel().getSelectedItem();
        DebugCacheFactory.getInstance().clean();
        DebugCacheFactory.getInstance().debug(true);

        taskRun(selectNode, true);
    }

    private void taskRun(TreeItem<TreeNode> selectNode, boolean isDebug) {
        Task<Void> task = (new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                if (selectNode == null) return null;
                try {
                    DebugCacheFactory.getInstance().debug(isDebug);
                    RunTreeItem.runTreeItem(selectNode, (treeItem)->{
                        Platform.runLater(()->{
//                            TreeHandle.this.doubleClick(treeNode);
                            treeItem.setExpanded(true);
                            treeView.getSelectionModel().select(treeItem);
                            treeView.scrollTo(treeView.getRow(treeItem));
                        });
                    }, null, isDebug);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        });
        ThreadUtilFactory.getInstance().submit(task);
    }

    private void taskRunSpring(TreeItem<TreeNode> selectNode) {
        new Thread(new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                if (selectNode == null) return null;
                RunTreeItem.runSpring(selectNode);
                return null;
            }
        }).start();
    }

    @FXML
    public void copy(ActionEvent actionEvent) {
        TreeItem<TreeNode> treeItem = treeView.getSelectionModel().getSelectedItem();
        if (treeItem == null) {
            return;
        }
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(treeItem.getValue().getId()+"");
        clipboard.setContent(content);
    }

    @FXML
    public void paste(ActionEvent actionEvent)  {
        TreeItem<TreeNode> treeItem = treeView.getSelectionModel().getSelectedItem();
        if (treeItem == null) {
            return;
        }

        try {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            String content = clipboard.getString();
            if (content == null) return;
            TreeItem<TreeNode> copyTreeItem = selectTreeItem(Integer.parseInt(content));
            if (copyTreeItem == null) return;

            TreeNodeType type = copyTreeItem.getValue().getTreeNodeType();
            if (type == TreeNodeType.BeanVar || type == TreeNodeType.Param || type == TreeNodeType.Bean) {
                long count = treeItem.getChildren().stream().filter(treeItem1 -> treeItem1.getValue().getTreeNodeType() == type).count();
                if (count > 0) {
                    DialogUtil.alert("节点已经存在，不需要copy", Alert.AlertType.ERROR);
                    return;
                }
            }

            TreeNode copyTreeNode = copyTreeItem.getValue();
            int index = treeItem.getChildren().size() > 0 ? treeItem.getChildren().get(treeItem.getChildren().size()-1).getValue().getIndex()+1 : 1;
            TreeNode newTreeNode = copyTreeNode(copyTreeNode, treeItem.getValue(), index, copyTreeNode.getName() + "-copy");
            TreeDao.insert(newTreeNode);
            TreeItem<TreeNode> newTreeItem = new TreeItem<>(newTreeNode);
            treeItem.getChildren().add(newTreeItem);
            copyTreeNodeData(copyTreeNode, newTreeNode);
//            LOGGER.info("node:" + JsonUtil.toJson(newTreeNode));
            copyChildrenTreeItem(copyTreeItem, newTreeNode, newTreeItem);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void run(ActionEvent actionEvent) {
    }

    private void checkVarName(ActionEvent actionEvent) {
        TreeItem<TreeNode> treeItem = treeView.getSelectionModel().getSelectedItem();
        if (treeItem == null) {
            return;
        }
        Task<Void> task = new Task() {

            @Override
            protected Object call() throws Exception {
                try {
                    List<String> result = VarUtil.checkVarNames(treeItem);
                    String line = result.stream().collect(Collectors.joining(","));
                    if (!StringUtils.isEmpty(line)) {
                        Platform.runLater(()->DialogUtil.alert("这些变量没有值：" + line, Alert.AlertType.ERROR));
                        return null;
                    }
                    Platform.runLater(()->DialogUtil.alert("检查成功", Alert.AlertType.INFORMATION));
                    UiUtil.showMessage("检查成功");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        ThreadUtilFactory.getInstance().submit(task);
    }

    @FXML
    public void edit(ActionEvent actionEvent) {
        TreeItem<TreeNode> treeItem = treeView.getSelectionModel().getSelectedItem();
        if (treeItem == null) {
            return;
        }
        addOrEditGroup(treeItem.getValue(), TreeNodeType.Test, false, null);
    }

    private void importSwagger(ActionEvent actionEvent) {
        TreeItem<TreeNode> treeItem = treeView.getSelectionModel().getSelectedItem();
        if (treeItem == null) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("导入swagger");
        dialog.setHeaderText("输入swagger url");
        dialog.setContentText("swagger url");
        dialog.setWidth(400);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String url = result.get();
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    try {
                        String newUrl = url;
                        int t = newUrl.indexOf("swagger-ui.html");
                        if (t > 0) {
                            newUrl = newUrl.substring(0, t) + "v2/api-docs";
                        }
                        loadSwaggerData(newUrl, treeItem);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                    return null;
                }
            };
            task.setOnSucceeded(e->{
                UiUtil.showMessage("加载成功");
            });
            task.setOnFailed(e->{
                UiUtil.showMessage("加载失败");
            });
            ThreadUtilFactory.getInstance().submit(task);

        }
    }

    private void loadSwaggerData(String url, TreeItem<TreeNode> treeItem) throws SQLException {
        String jsonTxt = HttpClientUtil.getInstance().sendHttpGet(url);
        Swagger swagger = JsonUtil.toObject(jsonTxt, Swagger.class);
        TreeItem<TreeNode> paramTreeItem = null;
        for(TreeItem<TreeNode> child : treeItem.getChildren()) {
            if (child.getValue().getTreeNodeType() == TreeNodeType.Param) {
                paramTreeItem = child;
                break;
            }
        }
        List<ParamModel> paramModelList = new ArrayList<>();
        if (paramTreeItem == null) {
            paramTreeItem = createTreeItem(treeItem, TreeNodeType.Param, "参数配置", "参数配置", 0);
        } else {
            paramModelList = DataUtil.getData(paramTreeItem.getValue().getId());
        }
        ParamModel paramModel = new ParamModel();
        paramModel.setIndex(paramModelList.size());
        paramModel.setTreeNodeId(paramTreeItem.getValue().getId());
        paramModel.setName("httpUrl");
        paramModel.setParamCategory(ParamCategory.Constant);
        paramModel.setClassName(String.class.getName());
        paramModel.setDesc("swagger host");
        String host = swagger.getHost()  + swagger.getBasePath();
        if (!host.startsWith("http://") || !host.startsWith("https://")) {
            host = "http://" + host;
        }
        paramModel.setValue(host);
        ParamDao.save(paramModel);

        if (swagger.getTags() == null) return;
        for (Tag tag : swagger.getTags()) {
            TreeItem<TreeNode> testTreeItem = null;
            for(Map.Entry<String, Map<String, PathInfo>> entry : swagger.getPaths().entrySet()) {
                PathInfo pathInfo = entry.getValue().values().iterator().next();
                if (!pathInfo.getTags().contains(tag.getName()))continue;
                if (testTreeItem == null) {
                    testTreeItem = createTreeItem(treeItem, TreeNodeType.Test, tag.getName() , tag.getDescription(), -1);
                }
                String method = entry.getValue().keySet().iterator().next();
                TreeItem<TreeNode> httpTreeItem = createTreeItem(testTreeItem, TreeNodeType.Http, entry.getKey() , pathInfo.getSummary(), -1);
                TestHttp testHttp = new TestHttp();
                testHttp.setTreeNodeId(httpTreeItem.getValue().getId());
                testHttp.setUrl("${httpUrl}" + parseSwaggerUrl(entry.getKey()));

                testHttp.setMethod(HttpMethod.valueOf(method.substring(0, 1).toUpperCase() + method.substring(1).toLowerCase()));
                TestHttpDao.save(testHttp);

                if (pathInfo == null || pathInfo.getParameters() == null) continue;
                for(Parameter parameter : pathInfo.getParameters()) {
                    if (parameter.getIn() == null) continue;
                    if (parameter.getIn().equals("header") || parameter.getIn().equals("path") || parameter.getIn().equals("query")) {
                        TestHttpParam testHttpParam = new TestHttpParam();
                        testHttpParam.setParamType(ParamType.Param);
                        if (parameter.getIn().equals("header")) {
                            testHttpParam.setParamType(ParamType.Header);
                        }
                        testHttpParam.setTestHttpId(testHttp.getId());
                        testHttpParam.setName(parameter.getName());
                        testHttpParam.setContent(parameter.getDescription());
                        TestHttpParamDao.save(testHttpParam);
                    } else if (parameter.getIn().equals("body")) {
                        TestHttpBody body = new TestHttpBody();
                        body.setTestHttpId(testHttp.getId());
                        body.setContentType(ContentType.Json.getContent());
                        TestHttpBodyDao.save(body);
                    }
                }
            }
        }
    }

    private String parseSwaggerUrl(String url) {
        return url.replace("{", "${");
    }

    public void addParam(ActionEvent actionEvent) {
        createTreeItem(TreeNodeType.Param, "属性管理", 1, (treeItem)->{
            Tab tab = new Tab("属性管理");
            tab.setUserData(treeItem.getValue());

            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            Parent parent = RegionUtil.paramPane(treeItem);
            tab.setContent(parent);
            return "";
        });
    }

    public void addFun(ActionEvent actionEvent) {
        createTreeItem(TreeNodeType.Function, "方法管理", null, (treeItem)->{
            Tab tab = new Tab("方法管理");
            tab.setUserData(treeItem.getValue());

            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            Parent parent = RegionUtil.functionPane(treeItem);
            tab.setContent(parent);
            return "";
        });
    }

    private Tab getExistedTab(TreeNode treeNode) {
        for (Tab tab : tabPane.getTabs()) {
            Object data = tab.getUserData();
            if (data != null && data instanceof TreeNode) {
                TreeNode treeNode1 = (TreeNode) data;
                if (treeNode.getId() == treeNode1.getId()) {
                    return tab;
                }
            }
        }
        return null;
    }

    private void addTest(ActionEvent event) {
        TreeItem<TreeNode> treeItem = treeView.getSelectionModel().getSelectedItem();
        if (treeItem == null) {
            return;
        }

        createTreeItem(TreeNodeType.Test, "测试场景", null, (treeItem1)->{
            return "";
        });
    }

    private void addBeanVar(ActionEvent event) {
        createTreeItem(TreeNodeType.BeanVar, "动态变量设置", null, (treeItem1)->{
            Tab tab = new Tab(treeItem1.getValue().getName());
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            tab.setUserData(treeItem1.getValue());
            tab.setContent(RegionUtil.beanVarPane(treeItem1));
            return "";
        });
    }

    private void addPrecondition(ActionEvent event) {
        TreeItem<TreeNode> treeItem = treeView.getSelectionModel().getSelectedItem();
        if (treeItem == null) {
            return;
        }

//        addGroup(TreeNodeType.Data, false, (treeItem1)->{
//            Tab tab = new Tab(treeItem1.getValue().getName());
//            tabPane.getTabs().add(tab);
//            tabPane.getSelectionModel().select(tab);
//            tab.setUserData(treeItem1.getValue());
//            tab.setContent(RegionUtil.sqlPane(treeItem1));
//            return "";
//        });

        createTreeItem(TreeNodeType.Data, "初始化数据", 1, (treeItem1)->{
            Tab tab = new Tab(treeItem.getValue().getName());
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            tab.setUserData(treeItem.getValue());
            tab.setContent(RegionUtil.sqlPane(treeItem));
            return "";
        });
    }

    private void addLastCondition(ActionEvent event) {
        TreeItem<TreeNode> treeItem = treeView.getSelectionModel().getSelectedItem();
        if (treeItem == null) {
            return;
        }

        createTreeItem(TreeNodeType.Data2, "清理数据", null, (treeItem1)->{
            Tab tab = new Tab(treeItem.getValue().getName());
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            tab.setUserData(treeItem.getValue());
            tab.setContent(RegionUtil.sqlPane(treeItem));
            return "";
        });
    }

    private void exportData(ActionEvent event) {
        createTreeItem(TreeNodeType.ExportData, "导出数据", null, (treeItem)->{
            Tab tab = new Tab(treeItem.getValue().getName());
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            tab.setUserData(treeItem.getValue());
            tab.setContent(RegionUtil.exportDataPane(treeItem));
            return "";
        });
    }

    private void addMethodInvoke(ActionEvent event) {
        createTreeItem(TreeNodeType.Method, "方法测试", null, (treeItem)->{
            Tab tab = new Tab(treeItem.getValue().getName());
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            tab.setUserData(treeItem.getValue());
            tab.setContent(RegionUtil.classPane(treeItem));
            return "";
        });

    }

    private void checkResultData(ActionEvent actionEvent) {
        createTreeItem(TreeNodeType.Bean, "对象验证", null, (treeItem)->{
            Tab tab = new Tab(treeItem.getValue().getName());
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            tab.setUserData(treeItem.getValue());
            tab.setContent(RegionUtil.beanPane(treeItem));
            return "";
        });

    }

    private void checkData(ActionEvent actionEvent) {
        createTreeItem(TreeNodeType.SqlCheckData, "数据库验证", null, (treeItem)->{
            Tab tab = new Tab(treeItem.getValue().getName());
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
            tab.setUserData(treeItem.getValue());
            tab.setContent(RegionUtil.sqlCheckPane(treeItem));
            return "";
        });
    }

    private void disable(ActionEvent actionEvent) {
        TreeItem<TreeNode> selectNode = treeView.getSelectionModel().getSelectedItem();
        if (selectNode == null) return;
        TreeNode treeNode = selectNode.getValue();
        ThreadUtilFactory.getInstance().submit(()->{
            try {
                treeNode.setEnableNoProperty(false);
                TreeDao.save(treeNode);
                Platform.runLater(()->treeNode.setEnable(false));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }, null);

    }

    private void enable(ActionEvent actionEvent) {
        TreeItem<TreeNode> selectNode = treeView.getSelectionModel().getSelectedItem();
        if (selectNode == null) return;
        TreeNode treeNode = selectNode.getValue();
        ThreadUtilFactory.getInstance().submit(()->{
            try {
                treeNode.setEnable(true);
                TreeDao.save(treeNode);
                Platform.runLater(()->treeNode.setEnable(true));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }, null);
    }

    private void openTab(TreeItem<TreeNode> treeItem, Parent pane) {
        pane.setMouseTransparent(false);
        Tab tab = getExistedTab(treeItem.getValue());
        if (tab == null) {
            tab = new Tab(treeItem.getValue().getName());
            tab.setClosable(true);
            tab.setContent(pane);
            tab.setGraphic(treeItem.getGraphic());
            tab.setUserData(treeItem.getValue());
            tabPane.getTabs().add(tab);
            tabPane.getSelectionModel().select(tab);
        } else {
            tab.setContent(pane);
            tabPane.getSelectionModel().select(tab);
        }
    }

    private void closeTab(TreeNode treeNode) {
        Tab removeTab = null;
        for (Tab tab : tabPane.getTabs()) {
            Object data = tab.getUserData();
            if (data != null && data instanceof TreeNode) {
                TreeNode treeNode1 = (TreeNode) data;
                if (treeNode.getId() == treeNode1.getId()) {
                    removeTab = tab;
                    break;
                }
            }
        }
        if (removeTab != null) tabPane.getTabs().remove(removeTab);
    }

    private Node buildGraphic(GlyphIcon icon) {
        icon.setGlyphSize(18);
        icon.setFill(Color.STEELBLUE);
        return icon;
    }

    private TreeNode copyTreeNode(TreeNode treeNode, TreeNode parentNode, int index, String name) {
        TreeNode result = new TreeNode();
        result.setName(name);
        result.setDesc(treeNode.getDesc());
        result.setParentId(parentNode.getId());
        result.setIndex(index);
        result.setPath(parentNode.getPath() + "-" + parentNode.getId());
        result.setTreeNodeType(treeNode.getTreeNodeType());
        return result;
    }

    private TreeItem<TreeNode> selectTreeItem(int treeNodeId) {
        TreeItem<TreeNode> root = treeView.getRoot();
        TreeItem<TreeNode> node = root;
        for (TreeItem<TreeNode> child : node.getChildren()) {
            TreeItem temp = selectTreeItem(treeNodeId, child);
            if (temp != null) {
                return temp;
            }
        }
        return null;
    }

    private TreeItem<TreeNode> selectTreeItem(int treeNodeId, TreeItem<TreeNode> node) {
        if (node.getValue().getId().equals(treeNodeId)) {
            return node;
        }
        for (TreeItem<TreeNode> child : node.getChildren()) {
            if (child.getValue().getId().equals(treeNodeId)) {
                return child;
            }
            TreeItem temp = selectTreeItem(treeNodeId, child);
            if (temp != null) {
                return temp;
            }
        }
        return null;
    }

    private void copyChildrenTreeItem(TreeItem<TreeNode> copyTreeItem, TreeNode treeNode, TreeItem<TreeNode> parentTreeItem) throws SQLException {
        int index = 0;
        for(TreeItem<TreeNode> child : copyTreeItem.getChildren()) {
            TreeNode treeNode2 = child.getValue();
            TreeNode newTreeNode = copyTreeNode(treeNode2, treeNode, index++, treeNode2.getName());
            TreeDao.insert(newTreeNode);
            copyTreeNodeData(treeNode2, newTreeNode);
            TreeItem<TreeNode> newTreeItem = new TreeItem<>(newTreeNode);
            parentTreeItem.getChildren().add(newTreeItem);
            copyChildrenTreeItem(child, newTreeNode, newTreeItem);
        }
    }

    private void copyTreeNodeData(TreeNode copyTreeNode, TreeNode newTreeNode) throws SQLException {
        if (copyTreeNode.getTreeNodeType() == TreeNodeType.Param) {
            List<ParamModel> paramModelList = ParamDao.getByTreeNodeId(copyTreeNode.getId());
            paramModelList.stream().forEach(paramModel -> {
                try {
                    paramModel.setTreeNodeId(newTreeNode.getId());
                    ParamDao.insert(paramModel);
                } catch (Exception e)  {
                    e.printStackTrace();
                }
            });
        } else if (copyTreeNode.getTreeNodeType() == TreeNodeType.BeanVar) {
            List<BeanVar> beanVarList = BeanVarDao.getByTreeNodeId(copyTreeNode.getId());
            beanVarList.stream().forEach(beanVar -> {
                try {
                    beanVar.setTreeNodeId(newTreeNode.getId());
                    BeanVarDao.insert(beanVar);
                } catch (Exception e)  {
                    e.printStackTrace();
                }
            });
        } else if (copyTreeNode.getTreeNodeType() == TreeNodeType.Method) {
            List<TestMethod> testMethods = TestMethodDao.getByTreeNodeId(copyTreeNode.getId());
            testMethods.stream().forEach(testMethod -> {
                try {
                    int testMethodId = testMethod.getId();
                    testMethod.setTreeNodeId(newTreeNode.getId());
                    TestMethodDao.insert(testMethod);

                    List<TestMethodData> datas = TestMethodDataDao.getByTestMethodId(testMethodId);
                    datas.forEach(testMethodData -> {
                        try {
                            testMethodData.setTestMethodId(testMethod.getId());
                            TestMethodDataDao.insert(testMethodData);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else if (copyTreeNode.getTreeNodeType() == TreeNodeType.Http) {
            List<TestHttp> testHttpList = TestHttpDao.getByTreeNodeId(copyTreeNode.getId());
            testHttpList.stream().forEach(testHttp -> {
                try {
                    int testHttpId = testHttp.getId();
                    testHttp.setTreeNodeId(newTreeNode.getId());
                    TestHttpDao.insert(testHttp);

                    List<TestHttpParam> testHttpParams = TestHttpParamDao.getByTestHttpId(testHttpId);
                    testHttpParams.stream().forEach(testHttpParam -> {
                        try {
                            testHttpParam.setTestHttpId(testHttp.getId());
                            TestHttpParamDao.insert(testHttpParam);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    List<TestHttpBody> testHttpBodys = TestHttpBodyDao.getByTestHttpId(testHttpId);
                    testHttpBodys.stream().forEach(testHttpBody -> {
                        try {
                            testHttpBody.setTestHttpId(testHttp.getId());
                            TestHttpBodyDao.insert(testHttpBody);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } catch (Exception e)  {
                    e.printStackTrace();
                }
            });
        } else if (copyTreeNode.getTreeNodeType() == TreeNodeType.SqlCheckData) {
            List<SqlCheck> sqlCheckList = SqlCheckDao.getByTreeNodeId(copyTreeNode.getId());
            sqlCheckList.forEach(sqlCheck -> {
                try {
                    List<SqlCheckData> sqlCheckDataList = SqlCheckDataDao.getBySqlCheckId(sqlCheck.getId());

                    sqlCheck.setTreeNodeId(newTreeNode.getId());
                    SqlCheckDao.insert(sqlCheck);

                    sqlCheckDataList.forEach(sqlCheckData -> {
                        sqlCheckData.setSqlCheckId(sqlCheck.getId());
                        SqlCheckDataDao.insert(sqlCheckData);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } else if (copyTreeNode.getTreeNodeType() == TreeNodeType.Bean) {
            BeanCheck beanCheck = BeanCheckDao.getByTreeNodeId(copyTreeNode.getId());
            beanCheck.setTreeNodeId(newTreeNode.getId());
            BeanCheckDao.insert(beanCheck);
        }  else if (copyTreeNode.getTreeNodeType() == TreeNodeType.Data || copyTreeNode.getTreeNodeType() == TreeNodeType.Data2) {
            List<SqlData> sqlDataList = SqlDataDao.getByTreeNodeId(copyTreeNode.getId());
            for (SqlData sqlData : sqlDataList) {
                sqlData.setTreeNodeId(newTreeNode.getId());
                SqlDataDao.insert(sqlData);
            }
        } else {
            LOGGER.warning("复制不支持这种节点数据:" + copyTreeNode.getTreeNodeType());
        }
    }

    private String debugText(TreeNode treeNode) {
//        if (DebugCacheFactory.getInstance().isDebug()) {
//            return "清除Debug缓存";
//        }
        return "Debug运行";
    }

    private void debugRunNode(TreeItem<TreeNode> treeItem) {
        Map<String, Object> paramMap = null;
        TestLog testLog = null;
        DebugCacheFactory.getInstance().debug(true);
        if (DebugCacheFactory.getInstance().contain(treeItem.getValue())) {
            testLog = DebugCacheFactory.getInstance().getTestLog(treeItem.getValue());
            paramMap = DebugCacheFactory.getInstance().getCache(treeItem.getValue());
        } else {
            TreeItem<TreeNode> parent = treeItem.getParent();
            if (DebugCacheFactory.getInstance().contain(parent.getValue())) {
                testLog = DebugCacheFactory.getInstance().getTestLog(parent.getValue());
                paramMap = DebugCacheFactory.getInstance().getCache(parent.getValue());
            }
        }
        if (paramMap == null) {
            try{
                paramMap = RunTreeItem.initRun(treeItem);  // 加载公用方法 // 加载公用方法
                testLog = TestLog.buildTestLog(treeItem.getValue().getName(), treeItem.getValue().getTreeNodeType().name());
                TestLogDao.insert(testLog);
                UiUtil.addTestLog(testLog);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        Map<String, Object> map = paramMap;
        TestLog log = testLog;
        Task<Void> task = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                try {
                    RunTreeItem.runTreeItem(treeItem, map, null, null, log, true);
                } catch (Exception e) {
                    LogUtil.log(e.getMessage());
                    e.printStackTrace();
                }
                return null;
            }
        };
        ThreadUtilFactory.getInstance().submit(task);
    }

    private void createTreeItem(TreeNodeType treeNodeType, String treeNodeName, Integer childIndex, Function<TreeItem<TreeNode>, Object> function) {
        TreeItem<TreeNode> selectNode = treeView.getSelectionModel().getSelectedItem();
        if (selectNode == null) return;

        TreeItem<TreeNode> newTreeItem = new TreeItem<>();
        TreeNode treeNode = new TreeNode(treeNodeName, treeNodeType);
        try {
            treeNode.setParentId(selectNode.getValue().getId());
            treeNode.setPath(selectNode.getValue().getPath() + "-" + selectNode.getValue().getId());
            int index = 1;
            int size = selectNode.getChildren().size();
            if (size > 0) {
                index = selectNode.getChildren().get(size-1).getValue().getIndex()+1;
            }
            if (childIndex != null) {
                index = childIndex;
                long c = selectNode.getChildren().stream().filter(treeItem -> treeItem.getValue().getIndex() == childIndex).count();
                if (c > 0) {
                    addIndexForTreeItems(selectNode.getChildren(), childIndex);
                }
            }
            treeNode.setIndex(index);
            TreeDao.save(treeNode);
            newTreeItem.setValue(treeNode);
            selectNode.getChildren().add(index-1, newTreeItem);
            selectNode.setExpanded(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        function.apply(newTreeItem);
    }

    private void addIndexForTreeItems(ObservableList<TreeItem<TreeNode>> children, Integer childIndex) {
        ThreadUtilFactory.getInstance().submit(()->{
            try {
                for (TreeItem<TreeNode> treeItem : children) {
                    if (treeItem.getValue().getIndex() >= childIndex) {
                        treeItem.getValue().setIndex(treeItem.getValue().getIndex() + 1);
                        TreeDao.update(treeItem.getValue());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }, null);
    }

    private TreeItem<TreeNode> createTreeItem(TreeItem<TreeNode> parentTreeItem, TreeNodeType treeNodeType, String name, String desc, int index) throws SQLException {
        TreeItem<TreeNode> newTreeItem = new TreeItem<>();
        TreeNode treeNode = new TreeNode(name, treeNodeType);
        treeNode.setDesc(desc);
        treeNode.setParentId(parentTreeItem.getValue().getId());
        treeNode.setPath(parentTreeItem.getValue().getPath() + "-" + parentTreeItem.getValue().getId());
        if (index > 0) {
            treeNode.setIndex(index);
        } else {
            int size = parentTreeItem.getChildren().size();
            treeNode.setIndex(1);
            if (size > 0) {
                treeNode.setIndex(parentTreeItem.getChildren().get(size-1).getValue().getIndex()+1);
            }
        }
        TreeDao.save(treeNode);
        newTreeItem.setValue(treeNode);
        Platform.runLater(()->{
            if (index > 0) {
                parentTreeItem.getChildren().add(index, newTreeItem);
            } else {
                parentTreeItem.getChildren().add(newTreeItem);
            }
            parentTreeItem.setExpanded(true);
        });
        return newTreeItem;
    }
}
