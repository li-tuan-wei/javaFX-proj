package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ldh.common.testui.TestUIMainApp;

public class StackPaneTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(TestUIMainApp.class.getResource("/fxml/StackPaneTest.fxml"));
        Scene scene = new Scene(root, 1200, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("测试");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
