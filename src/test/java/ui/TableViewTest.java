package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import ldh.common.testui.model.TreeNode;

import java.util.ArrayList;
import java.util.List;

public class TableViewTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        TableView<TreeNode> tableView = new TableView();
        TableColumn<TreeNode, Integer> idColumn = new TableColumn<>("id");
        idColumn.setCellValueFactory(new PropertyValueFactory("id"));

        TableColumn<TreeNode, Integer> nameColumn = new TableColumn<>("name");
        nameColumn.setCellValueFactory(new PropertyValueFactory("name"));

        TableColumn<TreeNode, Integer> descColumn = new TableColumn<>("desc");
        descColumn.setCellValueFactory(new PropertyValueFactory("desc"));

        tableView.getColumns().addAll(idColumn, nameColumn, descColumn);

        List<TreeNode> treeNodes = createTreeNodes(10);
        tableView.getItems().addAll(treeNodes);

        Scene scene = new Scene(tableView, 500, 300);
//        scene.getStylesheets().add(this.getClass().getResource("/css/TableViewTest.css").toExternalForm());
        scene.getStylesheets().add(this.getClass().getResource("/css/tableview2.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private List<TreeNode> createTreeNodes(int n) {
        List<TreeNode> treeNodes = new ArrayList<>();
        for (int i=0; i<n; i++) {
            TreeNode treeNode = new TreeNode();
            treeNode.setId(i);
            treeNode.setName("test_" + i);
            treeNode.setDesc("desc_" + i);
            treeNodes.add(treeNode);
        }

        return treeNodes;
    }


}
