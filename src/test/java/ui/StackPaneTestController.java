package ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public class StackPaneTestController {

    @FXML Region leftPane;

    public void showPane(ActionEvent actionEvent) {
        if (leftPane.isVisible()) {
            leftPane.setVisible(false);
        } else {
            leftPane.setVisible(true);
        }
    }

    public void sizeShowPane(ActionEvent actionEvent) {
        double w =leftPane.getWidth();
        leftPane.setPrefWidth(w+20);
        leftPane.setMaxWidth(w+20);
//        leftPane.setMaxWidth(Double.MAX_VALUE);
    }
}
