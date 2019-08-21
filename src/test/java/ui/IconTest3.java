package ui;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import de.jensd.fx.glyphs.weathericons.WeatherIcon;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import ldh.common.testui.component.IconPane;

import java.awt.*;


/**
 * Created by ldh on 2018/4/18.
 */
public class IconTest3 extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox vbox = new VBox();
        Popup popup = new Popup();
        IconPane iconPane = new IconPane(FontAwesomeIcon.class, MaterialDesignIcon.class, OctIcon.class, WeatherIcon.class);

        Canvas canvas = new Canvas();

        Button b1 = new Button("show");
        b1.setOnAction(e->{
            StackPane stackPane = new StackPane();
            stackPane.setPrefSize(800, 600);
            stackPane.getChildren().add(iconPane);
            stackPane.setStyle("-fx-background-color: whitesmoke");
            popup.getContent().add(stackPane);
            popup.show(primaryStage.getScene().getWindow(), 200, 200);
        });
        vbox.getChildren().addAll(b1, canvas);
        Scene scene = new Scene(vbox, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        Font f = new Font("宋体", Font.BOLD, 12);
        FontMetrics fm = sun.font.FontDesignMetrics.getMetrics(f);
        // 高度
        System.out.println(fm.getHeight());
        // 单个字符宽度
        System.out.println(fm.charWidth('A'));
        // 整个字符串的宽度
        System.out.println(fm.stringWidth("宋A"));
    }
}
