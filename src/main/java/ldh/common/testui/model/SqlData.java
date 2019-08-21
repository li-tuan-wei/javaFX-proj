package ldh.common.testui.model;

import com.google.gson.reflect.TypeToken;
import ldh.common.testui.constant.SqlDataType;
import ldh.common.testui.constant.SqlHandleType;
import ldh.common.testui.util.DataUtil;
import ldh.common.testui.util.JsonUtil;
import ldh.common.testui.util.OrderUtil;
import lombok.Data;

import java.io.File;
import java.util.List;

@Data
public class SqlData {
    private Long id;
    private Integer treeNodeId;
    private Integer databaseParamId;
    private SqlDataType dataType;
    private SqlHandleType handleType;
    private String data;

    private ParamModel databaseParam;

    public ParamModel getParamModel() {
        if (databaseParam == null && databaseParamId != null) {
            databaseParam = DataUtil.getById(databaseParamId);
        }
        return databaseParam;
    }

    public <T> T getObjectData() {
        if (dataType == SqlDataType.sql) {
            return (T) data;
        } else if (dataType == SqlDataType.file_sql) {
            return JsonUtil.toObject(data, new TypeToken<List<SqlFileData>>() {}.getType());
        }  else if (dataType == SqlDataType.csv) {
            return JsonUtil.toObject(data, new TypeToken<List<SqlFileData>>() {}.getType());
        } else {
            throw new RuntimeException("不支持这个类型：" + dataType);
        }
    }

    @Data
    public static class SqlFileData extends OrderUtil.OrderAble {
        private String file;
    }
}
