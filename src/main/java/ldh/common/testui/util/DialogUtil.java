package ldh.common.testui.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by ldh on 2018/3/20.
 */
public class DialogUtil {

    public static void alert(String content, Alert.AlertType type) {
        Window owner = UiUtil.STAGE;
        Alert dlg = new Alert(type, "");
        dlg.initOwner(owner);
        dlg.setContentText(content);
        dlg.show();
    }

    public static void confirm(String content, Consumer<?> consumer) {
        Window owner = UiUtil.STAGE;
        Alert dlg = new Alert(Alert.AlertType.CONFIRMATION, "");
        dlg.initOwner(owner);
        dlg.setContentText(content);
        Optional<ButtonType> result = dlg.showAndWait();
        if (result.get() == ButtonType.OK){
            consumer.accept(null);
        }
    }
}
