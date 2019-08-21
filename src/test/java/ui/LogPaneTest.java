package ui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ldh.common.testui.TestUIMainApp;
import ldh.common.testui.component.LogPane;
import ldh.common.testui.constant.TestLogType;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.dao.TestLogDao;
import ldh.common.testui.dao.TestLogDataDao;
import ldh.common.testui.model.TestLog;
import ldh.common.testui.model.TestLogData;
import ldh.common.testui.util.ThreadUtilFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class LogPaneTest extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        TestUIMainApp.startDb(new String[]{});
        LogPane logPane = new LogPane();
        Scene scene = new Scene(logPane, 800, 600);
        scene.getStylesheets().add(this.getClass().getResource("/css/LogPane.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        TestLog testLog = TestLogDao.getById(382); // 144, 82, 118, 188, 382
        if (testLog == null) return;
        logPane.addTitle(testLog.getName(), null);

        int type = 1;
        if (testLog.getType().equals(TreeNodeType.Case.name())) {
            type = 2;
        } else if (testLog.getType().equals(TreeNodeType.Test.name())) {
            type = 3;
        }

        int tt = type;
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    handleTestLog(logPane, testLog, tt, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        ThreadUtilFactory.getInstance().submit(task);
    }

    private void handleTestLog(LogPane logPane, TestLog testLog, int type, boolean isFirst) throws SQLException {
        if (isFirst) {
            Platform.runLater(()->logPane.addTitle(testLog.getName(), null));
        }
        //处理子节点
        List<TestLog> testLogs = TestLogDao.getByParentId(testLog.getId());
        for(TestLog log : testLogs) {
            TestLog t = log;
            Platform.runLater(()->logPane.addData(t));
            handleTestLog(logPane, log, type,false);
        }

        // 处理本节点下面的数据
        List<TestLogData> testLogDataList = TestLogDataDao.getByTestLogId(testLog.getId());
        for (int i=0, l=testLogDataList.size(); i<l; i++) {
            TestLogData d1 = testLogDataList.get(i);
            if (i != l-1) {
                if (testLogDataList.get(i+1).getName().startsWith(d1.getName())
                        && (d1.getType() == TestLogType.http || d1.getType() == TestLogType.method)) {
                    TestLogData d2 = testLogDataList.get(i+1);
                    Platform.runLater(()->logPane.addData(d1, d2));
                    i++;
                    continue;
                }
            }
            Platform.runLater(()->logPane.addData(d1));
        }
    }

    private void deday(int time, Supplier<Object> function) {
        new Thread(()->{
            try {
                Thread.sleep(time * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.runLater(()->{
                function.get();
            });
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
