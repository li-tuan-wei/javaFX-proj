package ldh.common.testui.cell;

import javafx.util.StringConverter;
import ldh.common.testui.util.MethodUtil;

import java.util.function.Function;

/**
 * Created by ldh on 2018/3/22.
 */
public class ObjectStringConverter<T> extends StringConverter<T> {

    private Function<T, Object> function;

    public ObjectStringConverter(Function<T, Object> function) {
        this.function = function;
    }

    @Override
    public String toString(T object) {
        Object value = function.apply(object);
        return value != null ? value.toString() : "";
    }

    @Override
    public T fromString(String string) {
        return null;
    }
}
