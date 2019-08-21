package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by ldh on 2019/3/1.
 */
public class PrettyListViewTest extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        SplitPane splitPane = new SplitPane();
        ListView<String> listView = new PrettyListView<>();
        ListView<String> listView2 = new ListView<>();
        List<String> list = new ArrayList();
        for (int i=0; i<50; i++) {
            list.add("test " + i);
        }
        listView.getItems().addAll(list);
        listView2.getItems().addAll(list);

        splitPane.getItems().addAll(listView, listView2);
        Scene scene = new Scene(splitPane, 200, 400);
        scene.getStylesheets().add(PrettyListView.class.getResource("/css/PrettyListView.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
