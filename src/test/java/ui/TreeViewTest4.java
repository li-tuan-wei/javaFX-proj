package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import ldh.common.testui.cell.ObjectTreeCell;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.model.TreeNode;

/**
 * Created by ldh on 2018/3/27.
 */
public class TreeViewTest4 extends Application{

    public final TreeItem<String> treeRoot = new TreeItem("root");

    @Override
    public void start(Stage primaryStage) throws Exception {
        TreeView<String> treeView = new TreeView();
        treeView.setRoot(treeRoot);
        treeView.setShowRoot(true);

        loadData(treeView);
        Scene scene = new Scene(treeView, 600, 300);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadData(TreeView<String> treeView) {
        for (int i=0; i<5; i++) {
            TreeItem<String> treeItem = new TreeItem<>("test" + i);
            treeRoot.getChildren().add(treeItem);
            initChild(treeItem);
        }
    }

    private void initChild(TreeItem<String> treeItem) {
        for (int i=0; i<5; i++) {
            TreeItem<String> treeItem2 = new TreeItem<>("child" + i);
            treeItem.getChildren().add(treeItem2);
        }
    }

    public static void main(String[] args) throws Exception {
        Application.launch(args);
    }
}
