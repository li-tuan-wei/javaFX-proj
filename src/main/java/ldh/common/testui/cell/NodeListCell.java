package ldh.common.testui.cell;

import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;

import java.util.function.Function;

/**
 * Created by ldh on 2018/3/22.
 */
public class NodeListCell<T> implements Callback<ListView<T>, ListCell<T>> {

    private Function<T, Node> function;

    public NodeListCell(Function<T, Node> function) {
        this.function = function;
    }

    @Override
    public ListCell<T> call(ListView<T> param) {
        return new ListCell<T>() {

            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Node node = function.apply(item);
                    setGraphic(node);
                    setText(null);
                }
            }
        };
    }
}
