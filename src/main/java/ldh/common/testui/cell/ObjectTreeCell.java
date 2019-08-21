package ldh.common.testui.cell;

import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.util.Callback;
import ldh.common.testui.model.TreeNode;

import java.util.function.Function;

/**
 * Created by ldh on 2018/3/26.
 */
public class ObjectTreeCell<T> implements Callback<TreeView<T>, TreeCell<T>> {

    private Function<T, Object> function;

    public ObjectTreeCell(Function<T, Object> function) {
        this.function = function;
    }

    @Override
    public TreeCell<T> call(TreeView<T> param) {
        return new TreeCell<T>() {

            @Override protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Object value = function.apply(item);
                    if (value instanceof Object[]) {
                        Object[] datas = (Object[]) value;
                        setText(datas[0].toString());
                        setGraphic((Node)datas[1]);
                    } else if (value != null) {
                        setText(value.toString());
                    }
                }
            }
        };
    }
}
