package ldh.common.testui.util;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

import java.sql.*;
import java.util.*;

public class DatabaseUtil {

    private static Map<String, Map<String, Map<String, String>>> databasseInfo = new HashMap<>();
    private static Map<String, Map<String, List<String>>> feigonMaps = new HashMap<>();

    private DatabaseUtil() {

    }

    public static Map<String, Map<String, String>> getDatabaseInfo(String databaseName, Connection connection) {
        if (!databasseInfo.containsKey(databaseName)) {
            getTableNameByCon(databaseName, connection);
        }
        return databasseInfo.get(databaseName);
    }

    public static Map<String, List<String>> getFeigonMaps(String databaseName, Connection connection) {
        if (!databasseInfo.containsKey(databaseName)) {
            getTableNameByCon(databaseName, connection);
        }
        return feigonMaps.get(databaseName);
    }

    public static void executeSql(Connection jdbcTemplate, String file) {

    }

    public static void getTableNameByCon(String databaseName, Connection connection) {
        try {
            DatabaseMetaData meta = connection.getMetaData();
            Map<String, Map<String, String>> tableMap = new HashMap<>();
            Map<String, List<String>> tableForignMap = new HashMap();
            ResultSet rs = meta.getTables(databaseName, null, "%", new String[] { "TABLE" });
            while (rs.next()) {
                Map<String, String> tableInfo = new LinkedHashMap<String, String>();
                String tableName = rs.getString(3);
//                System.out.println("表名：" + tableName);
                tableMap.put(tableName, tableInfo);

                ResultSet colRet = meta.getColumns(null, "%", tableName, "%");
                while (colRet.next()) {
                    String columnName = colRet.getString("COLUMN_NAME");
//                    String columnType = colRet.getString("TYPE_NAME");
//                    int datasize = colRet.getInt("COLUMN_SIZE");
//                    int digits = colRet.getInt("DECIMAL_DIGITS");
//                    int nullable = colRet.getInt("NULLABLE");
                    tableInfo.put(columnName, columnName);
                }
                List<String> foreignTables = getForeignKey(connection, databaseName, null, tableName);
                tableForignMap.put(tableName, foreignTables);
            }
            databasseInfo.put(databaseName, tableMap);
            feigonMaps.put(databaseName, tableForignMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String exportToSql(Connection connection, String databaseName, String table, String sql, Object...param) throws SQLException {
        getDatabaseInfo(databaseName, connection);
        Map tableInfo = (Map)databasseInfo.get(databaseName).get(table);
        if (table.equalsIgnoreCase("order")) {
            table = "`" + table + "`";
        }
        QueryRunner queryRunner = new QueryRunner();
        List<Map<String, Object>> dataList =  queryRunner.query(connection, sql, new MapListHandler());
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> map : dataList) {
            String insertSql = buildSql(table, tableInfo, map);
            sb.append(insertSql).append("\r\n");
        }
        return sb.toString();
    }

    public static String buildSql(String tableName, Map<String, String> tableInfo, Map<String, Object> map) {
        String sql = "insert into " + tableName + "(";
        String values = "";
        for (Map.Entry<String, String> entry : tableInfo.entrySet()) {
            sql += entry.getKey() + ", ";
            values += getValue(map, entry.getKey()) + ", ";
        }
        values = values.substring(0, values.length() - 2);
        sql = sql.substring(0, sql.length() - 2) + ") values(" + values + ");";
        return sql;
    }

    public static Object getValue(Map<String, Object> map, String columnName) {
        Object value = map.get(columnName);
        if (value == null) return "NULL";
        return "'" + value + "'";
    }

    public static List<String[]> getAllTable(Connection connection, String tableOwer, String dbName) {
        List<String[]> tableNames = new ArrayList<String[]>();
        ResultSet tableRet = null;
        try {
            DatabaseMetaData dbMeta = connection.getMetaData();
            tableRet = dbMeta.getTables(dbName, null, "%", new String[]{"TABLE"});
            while(tableRet.next()) {
                String talbeName = tableRet.getString("TABLE_NAME");
                String talbeComment = tableRet.getString("REMARKS");
                String tableRole = tableRet.getString(2);
                String[] tables = new String[]{talbeName, talbeComment};
                if (tableOwer != null) {
                    if (tableOwer.equalsIgnoreCase(tableRole))
                        tableNames.add(tables);
                } else {
                    tableNames.add(tables);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.close(null, tableRet);
        }
        for (String[] tn : tableNames) {
            String comment = getTableComment(connection, tn[0]);
            tn[1] = comment;
        }
        return tableNames;
    }

    //获取mysql的表类型
    private static String getTableComment(Connection connection, String tableName){
        Statement st=null;
        ResultSet rs=null;
        String result="";
        try {
            st = connection.createStatement();
            rs = st.executeQuery("SELECT TABLE_COMMENT FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME='"+tableName+"'");
            while(rs.next()){
                result = rs.getString(1);
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.close(null, st, rs);
        }
        return result;
    }

    public static List<String> getForeignKey(Connection connection, String db, String schemaName, String tableName) {
        List<String> tables = new ArrayList<>();
        ResultSet pkRSet = null;
        try {
            DatabaseMetaData dbMeta = connection.getMetaData();
            pkRSet = dbMeta.getImportedKeys(db, schemaName, tableName);
            while(pkRSet.next()) {
                String tName = pkRSet.getString(3);
                String keyName = pkRSet.getString(4);
                String columnName = pkRSet.getString(8);
                tables.add(tName);
            }
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.close(null, pkRSet);
        }
        return tables;
    }
}
