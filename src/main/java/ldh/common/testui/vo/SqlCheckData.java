package ldh.common.testui.vo;

import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import ldh.common.testui.util.JsonUtil;
import ldh.common.testui.util.OrderUtil;
import ldh.common.testui.util.StringUtil;
import lombok.Data;

import java.util.Map;
import java.util.HashMap;
/**
 * Created by ldh on 2018/3/28.
 */
@Data
public class SqlCheckData extends OrderUtil.OrderAble {

    @Expose
    private Integer id;
    @Expose
    private Integer sqlCheckId;
    @Expose
    private String content;
    @Expose
    private Integer index;

    private Map<String, SqlColumnData> dataMap;

    public Map<String, SqlColumnData> toSqlColumnDataMap() {
        Map<String, SqlColumnData> dataMap = JsonUtil.toObject(content, new TypeToken<Map<String, SqlColumnData>>(){}.getType());
        dataMap.entrySet().stream().forEach(entry->{
            entry.getValue().setId(this.getId());
            entry.getValue().setIndex(index);
        });
        return dataMap;
    }

    public Map<String, SqlColumnData> getDataMap() {
        if (dataMap != null) return dataMap;
        dataMap = new HashMap<String, SqlColumnData>();
        if (content != null) {
            Map<String, SqlColumnData> map = toSqlColumnDataMap();
            map.forEach((key, value)->{
                if (StringUtil.isUpperString(key)) {
                    dataMap.put(key.toLowerCase(), value);
                } else {
                    dataMap.put(key, value);
                }
            });
        }
        return dataMap;
    }

    public void clean() {
        Map<String, SqlColumnData> map = toSqlColumnDataMap();
        map.forEach((key, value)->{
            value.setValue(null);
            value.setIsEqual(null);
            value.setChangedValue(null);
        });
        content = JsonUtil.toJson(map);
    }

}
