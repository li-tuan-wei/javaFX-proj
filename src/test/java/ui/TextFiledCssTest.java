package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Created by ldh on 2018/4/24.
 */
public class TextFiledCssTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(TextFiledCssTest.class.getResource("/fxml/TextFieldCss.fxml"));
        Scene scene = new Scene(parent, 600, 400);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
