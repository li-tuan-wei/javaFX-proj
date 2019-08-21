package ldh.common.testui;

import com.alibaba.druid.pool.DruidDataSource;
import com.jfoenix.controls.JFXDecorator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import ldh.common.testui.assist.template.beetl.BeetlFactory;
import ldh.common.testui.util.*;
import ldh.fx.transition.FadeInRightBigTransition;
import ldh.fx.transition.FadeOutRightBigTransition;
import ldh.fx.transition.FadeOutUpBigTransition;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.io.File;
import java.sql.*;
import org.h2.tools.Server;


public class TestUIMainApp extends Application {

    public void start(Stage primaryStage) throws Exception {
        UiUtil.STAGE = primaryStage;
        Parent root = FXMLLoader.load(TestUIMainApp.class.getResource("/fxml/MainApp.fxml"));
        JFXDecorator jfxDecorator = new JFXDecorator(primaryStage, root);
        Scene scene = new Scene(jfxDecorator, 1200, 600);
        scene.setFill(null);

        scene.getStylesheets().add(TestUIMainApp.class.getResource("/css/JFXTableView.css").toExternalForm());
        scene.getStylesheets().add(TestUIMainApp.class.getResource("/css/bootstrapfx.css").toExternalForm());
        scene.getStylesheets().add(TestUIMainApp.class.getResource("/css/common.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle("自动测试框架");
        jfxDecorator.setOpacity(0);

        jfxDecorator.setOnCloseButtonAction(()->{
            FadeOutUpBigTransition fadeOutUpBigTransition = new FadeOutUpBigTransition(jfxDecorator);
            fadeOutUpBigTransition.setOnFinished(e->{
                close();
            });
            fadeOutUpBigTransition.playFromStart();

        });
        primaryStage.setOnCloseRequest(e->close());
        primaryStage.show();

        UiUtil.initTagPaneContextMenu();

        FadeInRightBigTransition fadeInRightTransition = new FadeInRightBigTransition(jfxDecorator);
        fadeInRightTransition.setDelay(new Duration(800));
        fadeInRightTransition.playFromStart();
    }

    private void close() {
        try {
            UiUtil.DATA_SOURCE.close();
            SpringInitFactory.getInstance().close();
            ThreadUtilFactory.getInstance().shutdown();
            LibLoaderFactory.getInstance().close();
            System.exit(-1);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    public static void startDb(String[] args) throws Exception {
        Class.forName("org.h2.Driver");
        String file = FileUtil.getSourceRoot() + "/data";
        File f = new File(file);
        while(!f.exists()) {
            f.mkdir();
        }
        file += "/db";
//        file = "E:\\logs\\maker";
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl("jdbc:h2:" + file);
//        dataSource.setUrl("jdbc:h2:tcp://localhost/mem:test");
        dataSource.setPassword("");
        dataSource.setUsername("sa");
        dataSource.setMaxActive(2);
//        dataSource.setKeepAlive(true);
        initDb(dataSource);
        UiUtil.DATA_SOURCE = dataSource;
    }

    private static void initDb(DataSource dataSource) throws Exception {
        String sql = "select id, name, desc, type as treeNodeType, parent_id as parentId, index, path, enable from tree limit 10";
        Connection conn = dataSource.getConnection();
        Statement statement = conn.createStatement();
        boolean hasExist = true;
        try {
//            deleteTables(statement, "tree_node", "db_info");
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                try {
                    resultSet.getBoolean("enable");
                } catch (Exception e) {
                    newSql(dataSource);
                }
            }
        } catch (Exception e) {
            hasExist = false;
        }
        if (hasExist) {
            statement.close();
            return;
        }
        try {
            sql = FileUtil.loadJarFile("/data.sql");
            System.out.println("sql:::::::" + sql);
            statement.execute(sql);
            statement.close();
        }  finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public static void deleteTables(Statement statement, String... tables) throws Exception {
        for (String table : tables) {
            statement.execute("DROP table " + table);
        }
    }

    public static void newSql(DataSource dataSource) throws Exception {
        Connection conn = dataSource.getConnection();
        Statement statement = conn.createStatement();
        try {
            String sql = FileUtil.loadJarFile("/newSql.sql");
            System.out.println("sql:::::::" + sql);
            statement.execute(sql);
            statement.close();
        }  finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public static void start(String[] args) throws Exception {
        startDb(args);
        Application.launch(TestUIMainApp.class, args);
    }

    public static void start(String[] args, ApplicationContext applicationContext) throws Exception {
        startDb(args);
        SpringInitFactory.getInstance().setApplicationContext(applicationContext);
        Application.launch(TestUIMainApp.class, args);
    }

    public static void main(String[] args) throws Exception {
        start(args);
    }
}
