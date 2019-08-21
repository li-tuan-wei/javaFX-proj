package ui;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;

/**
 * Created by ldh on 2018/4/4.
 */
public class StackPaneSample extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        TabPane tabPane = new TabPane();
        Node node = tabPane.lookup(".tab-header-area");
        System.out.println("Node:" + node);
        Scene scene = new Scene(tabPane);
        scene.getStylesheets().add(StackPaneSample.class.getResource("/css/htmleditor.css").toExternalForm());
        primaryStage.setScene(scene);

        primaryStage.show();

        node = tabPane.lookup(".tab-header-area");
        System.out.println("Node:" + node);

    }
}
