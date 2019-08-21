package ldh.common.testui.util;

import com.alibaba.druid.pool.DruidDataSource;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import ldh.common.testui.model.TestLog;
import org.controlsfx.control.StatusBar;
import org.h2.tools.Server;
//import org.h2.tools.Server;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.function.Consumer;

/**
 * Created by ldh on 2017/2/26.
 */
public class UiUtil {

    public static Stage STAGE;
    public static DruidDataSource DATA_SOURCE;

    public volatile static StatusBar STATUSBAR;
    public static ListView<TestLog> TEST_LOG;

    public static TabPane MAIN_TAB_PANE;

    public static void showMessage(String msg) {
        if (STATUSBAR != null) {
            Platform.runLater(()->STATUSBAR.setText(msg.replace("\t", "")));
        }
    }

    public static void showProgress(double progress) {
        if (STATUSBAR != null) {
            Platform.runLater(()->STATUSBAR.setProgress(progress));
        }
    }

    public static void transitionPane(Node node1, Node node2, Consumer consumer) {
        FadeTransition f1 = new FadeTransition(Duration.millis(300), node1);
        f1.setByValue(1);
        f1.setToValue(0);
        f1.setOnFinished(e->node1.setVisible(false));
        FadeTransition f2 = new FadeTransition(Duration.millis(200), node2);
        f2.setByValue(0);
        f2.setToValue(1);
        f2.setOnFinished(e->node2.setVisible(true));
        SequentialTransition sequential = new SequentialTransition(f1, f2);
        sequential.setOnFinished((e)-> {
            if (consumer != null) {
                consumer.accept(null);
            }
        });
        sequential.playFromStart();
    }

    public static void addTestLog(TestLog testLog) {
        Platform.runLater(()->TEST_LOG.getItems().add(0, testLog));
    }

    public static void initTagPaneContextMenu() {
        StackPane region = (StackPane) MAIN_TAB_PANE.lookup(".tab-header-area");
        if (region != null) {
            region.setOnContextMenuRequested(ee->{
                ContextMenu tabContextMenu = new ContextMenu();
                Tab tab = MAIN_TAB_PANE.getSelectionModel().getSelectedItem();
                if (tab == null) {
                    MenuItem closeAll = new MenuItem("关闭所有");
                    closeAll.setOnAction(e->{
                        int length = MAIN_TAB_PANE.getTabs().size();
                        MAIN_TAB_PANE.getTabs().remove(1, length);
                    });
                } else {
                    MenuItem closeLeft = new MenuItem("关闭左边所有");
                    closeLeft.setOnAction(e->{
                        int index = MAIN_TAB_PANE.getTabs().indexOf(tab);
                        MAIN_TAB_PANE.getTabs().remove(1, index);
                    });
                    MenuItem closeRight = new MenuItem("关闭右边所有");
                    closeRight.setOnAction(e->{
                        int index = MAIN_TAB_PANE.getTabs().indexOf(tab);
                        int length = MAIN_TAB_PANE.getTabs().size();
                        MAIN_TAB_PANE.getTabs().remove(index+1, length);
                    });

                    MenuItem closeAll = new MenuItem("关闭所有");
                    closeAll.setOnAction(e->{
                        int length = MAIN_TAB_PANE.getTabs().size();
                        MAIN_TAB_PANE.getTabs().remove(1, length);
                    });
                    tabContextMenu.getItems().addAll(closeLeft, closeRight, closeAll);
                }

                tabContextMenu.show(region, ee.getScreenX(), ee.getScreenY());
                ee.consume();
            });
        }
    }
}
