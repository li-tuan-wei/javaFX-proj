package ldh.common.testui.cell;

import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import ldh.common.testui.vo.MethodData;

import java.util.Map;
import java.util.function.Function;

/**
 * Created by ldh on 2018/3/27.
 */
public class ObjectTableCellFactory<S, T> implements Callback<TableColumn<S, T>, TableCell<S, T>> {

    private Function<T, Object> function;
    private Pos alignment = Pos.CENTER_LEFT;

    public ObjectTableCellFactory(Function<T, Object> function) {
        this(function, Pos.CENTER_LEFT);
    }

    public ObjectTableCellFactory(Function<T, Object> function, Pos alignment) {
        this.function = function;
        this.alignment = alignment;
    }

    @Override
    public TableCell<S, T> call(TableColumn<S, T> param) {
        return new TableCell<S, T>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty || item.equals("")) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    if (function != null) {
                        Object object = function.apply(item);
                        if (object != null) {
                            StackPane root = new StackPane();
                            root.setAlignment(alignment);
                            if (object instanceof Parent) {
                                Parent parent = (Parent) object;
                                root.getChildren().add(parent);

                            } else {
                                Label label = new Label(object.toString());
                                root.getChildren().add(label);
                            }
                            setGraphic(root);
                            setText("");
                        } else {
                            setText(null);
                            setGraphic(null);
                        }
                    }
                }
            }
        };
    }
}
