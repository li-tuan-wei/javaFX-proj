package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Created by ldh on 2018/3/27.
 */
public class TreeViewTest3 extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        StackPane root = new StackPane();

//        ImageView folderIcon = new ImageView();
//        Image folderImage = new Image(getClass().getResourceAsStream("folder.png"));
//        folderIcon.setImage(folderImage);
//        folderIcon.setFitWidth(16);
//        folderIcon.setFitHeight(16);

        TreeItem treeItem = new TreeItem<>("根目录");
//        treeItem.setGraphic(folderIcon);
        treeItem.setExpanded(true);

        for(int i = 0;i < 5;i++){
            TreeItem item = new TreeItem<>("节点:" + i);
            treeItem.getChildren().add(item);
        }

        TreeView treeView = new TreeView<>(treeItem);
        root.getChildren().add(treeView);

        primaryStage.setTitle("TreeView的使用");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
