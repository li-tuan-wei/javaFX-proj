package ldh.common.testui.vo;

import ldh.common.testui.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;

import java.beans.Transient;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by ldh on 2018/3/28.
 */
public abstract class AbstractParam<T extends AbstractParam> implements ParamInterface<T> {

    protected transient Map<String, String> errorMap = new HashMap<>();

    public void checkEmpty(String data, String name) {
        if (StringUtils.isEmpty(data)) {
            errorMap.put("name", "不能为空");
        }
    }

    public void checkNull(Object data, String name) {
        if (data == null) {
            errorMap.put("name", "不能为空");
        }
    }

    public Map<String, String> getErrors() {
        return errorMap;
    }

    @Override
    public String demo() {
        return JsonUtil.toJson(this);
    }

    @Override
    public T parse(String data) {
        T t = JsonUtil.toObject(data, this.getClass().getGenericSuperclass());
        return t;
    }
}
