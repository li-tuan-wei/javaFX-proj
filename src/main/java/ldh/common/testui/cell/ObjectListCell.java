package ldh.common.testui.cell;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.util.Callback;
import ldh.common.testui.util.MethodUtil;

import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by ldh on 2018/3/22.
 */
public class ObjectListCell<T> implements Callback<ListView<T>, ListCell<T>> {

    private Function<T, Object> function;

    public ObjectListCell(Function<T, Object> function) {
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
                    Object value = function.apply(item);
                    if (value != null) {
                        setText(value.toString());
                    }
//                    setText(MethodUtil.buildMethodName(item));
                }
            }
        };
    }
}
