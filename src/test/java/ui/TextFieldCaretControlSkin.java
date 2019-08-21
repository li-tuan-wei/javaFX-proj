package ui;

import com.sun.javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class TextFieldCaretControlSkin extends TextFieldSkin {
    public TextFieldCaretControlSkin(TextField textField, Color caretColor) {
        super(textField);

        setCaretColor(caretColor);
    }

    private void setCaretColor(Color color) {
        caretPath.strokeProperty().unbind();
        caretPath.fillProperty().unbind();

        caretPath.setStroke(color);
        caretPath.setFill(color);
    }
}
