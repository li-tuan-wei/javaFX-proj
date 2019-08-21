package ldh.common.testui.vo;

import ldh.common.testui.assist.convert.ConvertFactory;
import ldh.common.testui.assist.template.beetl.BeetlFactory;
import ldh.common.testui.constant.JdbcType;
import lombok.Data;

import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by ldh on 2018/4/6.
 */
@Data
public class SqlColumnData {

    private final static Logger LOGGER = Logger.getLogger(SqlColumnData.class.getSimpleName());

    private Integer id;
    private SqlColumn sqlColumn;
    private String expectValue;
    private Object value;
    private Boolean isNull;
    private Object changedValue;
    private Boolean isEqual;
    private String desc;
    private Integer index;

    public Object getChangedValue() {
        String initValue = expectValue;
        if (expectValue.contains("$")) {
            if (changedValue == null) return expectValue;
            return changedValue;
        }
        JdbcType jdbcType = JdbcType.forCode(sqlColumn.getColumnType());
        if (initValue.equals("")) return initValue;
        try {
            return ConvertFactory.getInstance().get(jdbcType.CONVERT).parse(initValue);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info("sql value: " + initValue);
        }
        return expectValue;
    }

    public String getDesc() {
        if (desc == null) {
            return "";
        }
        return desc;
    }

}
