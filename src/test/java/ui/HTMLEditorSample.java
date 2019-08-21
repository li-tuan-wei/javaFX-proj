package ui;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;

/**
 * Created by ldh on 2018/4/4.
 */
public class HTMLEditorSample extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        HTMLEditor htmlEditor = new HTMLEditor();
        htmlEditor.setHtmlText("adfadfas<br/><h1>adfas</h1><span style='display:inline-block;text-indent:50px;color: red;'>sdfasfdas</span>");
//        for (Node toolBar = htmlEditor.lookup(".tool-bar"); toolBar != null; toolBar = htmlEditor.lookup(".tool-bar")) {
//            ((Pane) toolBar.getParent()).getChildren().remove(toolBar);
////            ((Pane) toolBar.getParent()).setVisible(false);
//        }
        Node node = htmlEditor.lookup(".top-toolbar");
        System.out.println("Node:" + node);
        Scene scene = new Scene(htmlEditor);
        scene.getStylesheets().add(HTMLEditorSample.class.getResource("/css/htmleditor.css").toExternalForm());
        primaryStage.setScene(scene);

        primaryStage.show();

        node = htmlEditor.lookup(".top-toolbar");
        System.out.println("Node:" + node);
    }
}
