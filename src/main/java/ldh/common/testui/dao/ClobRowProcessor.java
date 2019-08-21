package ldh.common.testui.dao;

import org.apache.commons.dbutils.BasicRowProcessor;

import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by ldh on 2019/1/28.
 */
public class ClobRowProcessor extends BasicRowProcessor {

    public Map<String, Object> toMap(ResultSet rs) throws SQLException {
        Map<String, Object> result = new ClobRowProcessor.CaseInsensitiveHashMap();
        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();

        for(int i = 1; i <= cols; ++i) {
            String columnName = rsmd.getColumnLabel(i);
            if (rsmd.getColumnTypeName(i).equalsIgnoreCase("clob")) {
                String clob = rs.getString(i);
                if(null == columnName || 0 == columnName.length()) {
                    columnName = rsmd.getColumnName(i);
                }

                System.out.println("content:" + clob.toString());
                result.put(columnName, clob.toString());
            } else {
                if(null == columnName || 0 == columnName.length()) {
                    columnName = rsmd.getColumnName(i);
                }

                result.put(columnName, rs.getObject(i));
            }
        }

        return result;
    }

    private static class CaseInsensitiveHashMap extends LinkedHashMap<String, Object> {
        private final Map<String, String> lowerCaseMap;
        private static final long serialVersionUID = -2848100435296897392L;

        private CaseInsensitiveHashMap() {
            this.lowerCaseMap = new HashMap();
        }

        public boolean containsKey(Object key) {
            Object realKey = this.lowerCaseMap.get(key.toString().toLowerCase(Locale.ENGLISH));
            return super.containsKey(realKey);
        }

        public Object get(Object key) {
            Object realKey = this.lowerCaseMap.get(key.toString().toLowerCase(Locale.ENGLISH));
            return super.get(realKey);
        }

        public Object put(String key, Object value) {
            Object oldKey = this.lowerCaseMap.put(key.toLowerCase(Locale.ENGLISH), key);
            Object oldValue = super.remove(oldKey);
            super.put(key, value);
            return oldValue;
        }

        public void putAll(Map<? extends String, ?> m) {
            Iterator i$ = m.entrySet().iterator();

            while(i$.hasNext()) {
                Map.Entry<? extends String, ?> entry = (Map.Entry)i$.next();
                String key = (String)entry.getKey();
                Object value = entry.getValue();
                this.put(key, value);
            }

        }

        public Object remove(Object key) {
            Object realKey = this.lowerCaseMap.remove(key.toString().toLowerCase(Locale.ENGLISH));
            return super.remove(realKey);
        }
    }
}
