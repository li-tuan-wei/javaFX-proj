package ui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class CaretColorizer extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        TextField redCaretTextField = new TextField("Red Caret");
        redCaretTextField.setSkin(
                new TextFieldCaretControlSkin(
                        redCaretTextField,
                        Color.RED
                )
        );

        TextField noCaretTextField = new TextField("No Caret");
        noCaretTextField.setSkin(
                new TextFieldCaretControlSkin(
                        noCaretTextField,
                        Color.TRANSPARENT
                )
        );

        TextField normalTextField = new TextField("Standard Caret");

        VBox layout = new VBox(
                10,
                redCaretTextField,
                noCaretTextField,
                normalTextField
        );

        layout.setPadding(new Insets(10));
        stage.setScene(new Scene(layout));

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
