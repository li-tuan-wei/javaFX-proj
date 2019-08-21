package ldh.common.testui.cell;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import ldh.common.testui.vo.MethodData;

import java.util.Map;

/**
 * Created by ldh on 2018/3/22.
 */
public class CheckColumnCell implements Callback<TableColumn<Map<String, MethodData>,MethodData>, TableCell<Map<String, MethodData>,MethodData>> {

    @Override
    public TableCell<Map<String, MethodData>, MethodData> call(TableColumn<Map<String, MethodData>, MethodData> param) {
        return new TableCell<Map<String, MethodData>, MethodData>() {
            @Override
            protected void updateItem(MethodData item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty || item.equals("")) {
                    setText(null);
                    setStyle("");
                } else {
                    setText("");
                    if (item.getData().equals("true")) {
                        setTextFill(Color.WHITE);
                        setStyle("-fx-background-color: green");
                    } else if (item.getData().equals("false")){
                        setTextFill(Color.WHITE);
                        setStyle("-fx-background-color: RED");
                    } else {
                        setTextFill(Color.GRAY);
                    }
                }
            }
        };
    }
}
