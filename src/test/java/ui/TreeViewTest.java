package ui;

import com.alibaba.druid.pool.DruidDataSource;
import javafx.application.Application;
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
public class TreeViewTest extends Application{

    public static final TreeNode treeData = new TreeNode("root", TreeNodeType.Root);
    public static final TreeItem<TreeNode> treeRoot = new TreeItem(treeData);

    @Override
    public void start(Stage primaryStage) throws Exception {
        TreeView<TreeNode> treeView = new TreeView();
        treeRoot.setValue(treeData);
        treeView.setRoot(treeRoot);
        treeView.setShowRoot(true);
        treeView.setCellFactory(new ObjectTreeCell<TreeNode>((treeNode)->{
            return treeNode.getName();
        }));

        loadData(treeView);
        Scene scene = new Scene(treeView, 600, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadData(TreeView<TreeNode> treeView) {
        for (int i=0; i<5; i++) {
            TreeNode treeNode = new TreeNode("test" + i, TreeNodeType.Node);
            TreeItem<TreeNode> treeItem = new TreeItem<>(treeNode);
            treeRoot.getChildren().add(treeItem);
            initChild(treeItem);
        }
    }

    private void initChild(TreeItem<TreeNode> treeItem) {
        for (int i=0; i<5; i++) {
            TreeNode treeNode = new TreeNode("child" + i, TreeNodeType.Node);
            TreeItem<TreeNode> treeItem2 = new TreeItem<>(treeNode);
            treeItem.getChildren().add(treeItem2);
        }
    }

    public static void main(String[] args) throws Exception {
        Application.launch(args);
    }
}
