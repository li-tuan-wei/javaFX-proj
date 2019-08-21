package ldh.common.testui.cell;

import javafx.beans.NamedArg;
import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import ldh.common.testui.model.BeanData;
import ldh.common.testui.vo.MethodData;

import java.util.Map;

/**
 * Created by ldh on 2018/3/23.
 */
public class MethodDataColumnCell<T> implements Callback<TableColumn.CellDataFeatures<Map,T>, ObservableValue<T>>  {

    private final Object key;

    public MethodDataColumnCell(Object key) {
        this.key = key;
    }

    @Override
    public ObservableValue<T> call(TableColumn.CellDataFeatures<Map, T> cdf) {
        Map map = cdf.getValue();
        Object value = map.get(key);

        // ideally the map will contain observable values directly, and in which
        // case we can just return this observable value.
        if (value instanceof ObservableValue) {
            return (ObservableValue)value;
        }

        if (value instanceof Boolean) {
            return (ObservableValue<T>) new ReadOnlyBooleanWrapper((Boolean)value);
        } else if (value instanceof Integer) {
            return (ObservableValue<T>) new ReadOnlyIntegerWrapper((Integer)value);
        } else if (value instanceof Float) {
            return (ObservableValue<T>) new ReadOnlyFloatWrapper((Float)value);
        } else if (value instanceof Long) {
            return (ObservableValue<T>) new ReadOnlyLongWrapper((Long)value);
        } else if (value instanceof Double) {
            return (ObservableValue<T>) new ReadOnlyDoubleWrapper((Double)value);
        } else if (value instanceof String) {
            return (ObservableValue<T>) new ReadOnlyStringWrapper((String)value);
        }

        if (value instanceof MethodData) {
            return new ReadOnlyObjectWrapper<T>((T)((MethodData) value));
        }

        if (value instanceof BeanData) {
            return new ReadOnlyObjectWrapper<T>((T)((BeanData) value));
        }

        // fall back to an object wrapper
        return new ReadOnlyObjectWrapper<T>((T)value);
    }
}
