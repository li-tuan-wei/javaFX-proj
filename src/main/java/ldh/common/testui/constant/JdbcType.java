package ldh.common.testui.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ldh on 2018/4/12.
 */
public enum JdbcType {
    ARRAY(2003, ""),
    BIT(-7, "int"),
    TINYINT(-6, "int"),
    SMALLINT(5, "int"),
    INTEGER(4, "int"),
    BIGINT(-5, "long"),
    FLOAT(6, "float"),
    REAL(7, "double"),
    DOUBLE(8, "double"),
    NUMERIC(2, "double"),
    DECIMAL(3, "BigDecimal"),
    CHAR(1, "char"),
    VARCHAR(12, "String"),
    LONGVARCHAR(-1, "String"),
    DATE(91, "Date"),
    TIME(92, "Date"),
    TIMESTAMP(93, "Date"),
    BINARY(-2, ""),
    VARBINARY(-3, ""),
    LONGVARBINARY(-4, ""),
    NULL(0, "null"),
    OTHER(1111, ""),
    BLOB(2004, ""),
    CLOB(2005, ""),
    BOOLEAN(16, ""),
    CURSOR(-10, ""),
    UNDEFINED(-2147482648, ""),
    NVARCHAR(-9, ""),
    NCHAR(-15, ""),
    NCLOB(2011, ""),
    STRUCT(2002, ""),
    JAVA_OBJECT(2000, ""),
    DISTINCT(2001, ""),
    REF(2006, ""),
    DATALINK(70, ""),
    ROWID(-8, ""),
    LONGNVARCHAR(-16, ""),
    SQLXML(2009, ""),
    DATETIMEOFFSET(-155, "");

    public final int TYPE_CODE;
    public final String CONVERT;

    private static Map<Integer, JdbcType> codeLookup = new HashMap();

    private JdbcType(int code, String convert) {
        this.TYPE_CODE = code;
        this.CONVERT = convert;
    }

    public static JdbcType forCode(int code) {
        return (JdbcType)codeLookup.get(Integer.valueOf(code));
    }

    static {
        JdbcType[] var0 = values();
        int var1 = var0.length;

        for(int var2 = 0; var2 < var1; ++var2) {
            JdbcType type = var0[var2];
            codeLookup.put(Integer.valueOf(type.TYPE_CODE), type);
        }

    }
}
