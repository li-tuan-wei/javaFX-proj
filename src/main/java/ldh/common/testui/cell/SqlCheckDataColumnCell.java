package ldh.common.testui.cell;

import javafx.beans.property.*;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import ldh.common.testui.model.BeanData;
import ldh.common.testui.vo.MethodData;
import ldh.common.testui.vo.SqlCheckData;
import ldh.common.testui.vo.SqlColumnData;

import java.util.Map;

/**
 * Created by ldh on 2018/3/23.
 */
public class SqlCheckDataColumnCell<T> implements Callback<TableColumn.CellDataFeatures<SqlCheckData,T>, ObservableValue<T>>  {

    private final Object key;

    public SqlCheckDataColumnCell(Object key) {
        this.key = key;
    }

    @Override
    public ObservableValue<T> call(TableColumn.CellDataFeatures<SqlCheckData, T> cdf) {
        SqlCheckData sqlCheckData = cdf.getValue();
        Map<String, SqlColumnData> map = sqlCheckData.getDataMap();
        if (map == null) return null;
        SqlColumnData value = map.get(key);
        if (value == null) value = map.get(key.toString().toLowerCase()); // 支持h2

        // ideally the map will contain observable values directly, and in which
        // case we can just return this observable value.
        if (value instanceof ObservableValue) {
            return (ObservableValue)value;
        }

        // fall back to an object wrapper
        return new ReadOnlyObjectWrapper<T>((T)value);
    }
}
