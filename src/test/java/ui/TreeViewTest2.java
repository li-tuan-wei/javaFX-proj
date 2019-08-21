package ui;

import com.alibaba.druid.pool.DruidDataSource;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import ldh.common.testui.cell.ObjectTreeCell;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.dao.TreeDao;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.FileUtil;
import ldh.common.testui.util.TreeUtil;
import ldh.common.testui.util.UiUtil;

import java.io.File;
import java.util.List;

/**
 * Created by ldh on 2018/3/27.
 */
public class TreeViewTest2 extends Application{

    public static final TreeNode treeData = new TreeNode("root", TreeNodeType.Root);
    public static final TreeItem<TreeNode> treeRoot = new TreeItem(treeData);

    @Override
    public void start(Stage primaryStage) throws Exception {
        TreeView<TreeNode> treeView = new TreeView();
        treeRoot.setValue(treeData);
        treeView.setRoot(treeRoot);
        treeView.setShowRoot(false);
        treeView.setCellFactory(new ObjectTreeCell<TreeNode>((treeNode)->{
            return treeNode.getName();
        }));

        loadData(treeView);
        Scene scene = new Scene(treeView, 600, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadData(TreeView<TreeNode> treeView) {
        Task<Void> loadDataTask = new Task<Void>() {

            @Override
            protected Void call() throws Exception {
                List<TreeNode> treeNodeList = TreeDao.getAll();
                List<TreeNode> treeTreeNode = TreeUtil.tree(treeNodeList);
                treeView.getRoot().setExpanded(true);
                Platform.runLater(()->{
                    for (TreeNode tree : treeTreeNode) {
                        TreeItem<TreeNode> parent = new TreeItem(tree);
                        treeView.getRoot().getChildren().add(parent);
                        handleChildren(parent, tree.getChildren());
                    }
                });

                return null;
            }
        };
        new Thread(loadDataTask).start();
    }

    private void handleChildren(TreeItem<TreeNode> root, List<TreeNode> children) {
        if (children == null) return;
        for (TreeNode treeNode : children) {
            TreeItem<TreeNode> parent = new TreeItem(treeNode);
            root.getChildren().add(parent);
//            parent.setExpanded(true);
            handleChildren(parent, treeNode.getChildren());
        }
    }

    public static void startDb(String[] args) throws Exception {
//        Server server = Server.createTcpServer(args).start();
        Class.forName("org.h2.Driver");
        String file = FileUtil.getSourceRoot() + "/data";
        File f = new File(file);
        while(!f.exists()) {
            f.mkdir();
        }
        file += "/db";
//        file = "E:\\logs\\maker";
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:h2:" + file);
        dataSource.setPassword("");
        dataSource.setUsername("sa");
        dataSource.setMaxActive(2);
        dataSource.setKeepAlive(true);
        UiUtil.DATA_SOURCE = dataSource;
    }

    public static void main(String[] args) throws Exception {
        startDb(args);
        Application.launch(args);
    }
}
