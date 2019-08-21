package ui;

import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIconView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import ldh.common.testui.component.IconPane;

/**
 * Created by ldh on 2018/4/18.
 */
public class IconTest2 extends Application{

    @Override
    public void start(Stage primaryStage) throws Exception {
        MaterialIconView icon = new MaterialIconView(MaterialIcon.BUG_REPORT);
        Label label = new Label("asdasfasd");
        label.setGraphic(icon);
        icon.setGlyphSize(30);
        icon.setFill(Color.YELLOW);
        VBox vBox = new VBox();

        TextField textField = new TextField();
        textField.setText("dsfadfasdfasdfasdfa");

//        textField.positionCaret(5);
//        textField.replaceText(5,6, "");
//        textField.requestFocus();

        TextField textField2 = new TextField();
        textField2.setText("dsfadfasdfas3333dfasdfa");
        textField2.positionCaret(5);
//        textField2.requestFocus();

        vBox.getChildren().addAll(icon, textField, textField2);
        Scene scene = new Scene(vBox, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.show();

        textField2.focusedProperty().addListener((b, o, n)->{
//            textField2.selectRange(5, 5);
        });
        textField2.requestFocus();
//        textField2.selectRange(5, 6);
//        textField2.replaceText(5, 6, "");
        textField2.positionCaret(2);
        System.out.println("tt:" + textField2.getCaretPosition());
    }
}
