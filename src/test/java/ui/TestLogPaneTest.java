package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ldh.common.testui.TestUIMainApp;
import ldh.common.testui.component.LogPane;
import ldh.common.testui.component.TestLogPane;
import ldh.common.testui.constant.TestLogType;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.dao.TestLogDao;
import ldh.common.testui.dao.TestLogDataDao;
import ldh.common.testui.model.TestLog;
import ldh.common.testui.model.TestLogData;

import java.sql.SQLException;
import java.util.List;
import java.util.function.Supplier;

public class TestLogPaneTest extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        TestUIMainApp.startDb(new String[]{});
        TestLog testLog = TestLogDao.getById(382); // 144, 82, 118,229,230, 382
        TestLogPane logPane = new TestLogPane(testLog);
        Scene scene = new Scene(logPane, 800, 600);
        scene.getStylesheets().add(this.getClass().getResource("/css/LogPane.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
