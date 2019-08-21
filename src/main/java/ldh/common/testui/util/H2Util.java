package ldh.common.testui.util;

import ldh.common.testui.dao.ClobRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;

import javax.sql.DataSource;
import java.io.*;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Created by ldh on 2019/1/28.
 */
public class H2Util {

    private static final Logger LOGGER = Logger.getLogger(H2Util.class.getName());

    public static final AtomicBoolean isInit = new AtomicBoolean(false);

    private static Map<String, Map<String, String>> databaseInfo = new HashMap();

    /**
     * 加载项目中的sql 数据
     *
     * @param obj
     * @param fileName
     */
    public static void importData(QueryRunner queryRunner, Object obj, String fileName) {
        String sqlFile = obj.getClass().getResource(fileName).getFile();
        executeSql(queryRunner, sqlFile);
    }

    public static void importSystemData(QueryRunner queryRunner, String sqlFile) {
        executeSql(queryRunner, sqlFile);
    }


    public static void exportAllTable(QueryRunner queryRunner, String dir, String fileName) {
        exportAllTable(queryRunner, dir, fileName, null);
    }

    public static void exportAllTable(QueryRunner queryRunner, String dir, final String fileName, String desc) {
        Map<String, Map<String, String>> databaseInfo = getDatabaseInfo(queryRunner.getDataSource());

        int num = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(num);
        final CountDownLatch countDownLatch = new CountDownLatch(databaseInfo.size());
        final BlockingQueue<String> queue = new LinkedBlockingDeque<String>();

        try {
            deleteFile(dir, fileName);
            writeDescToFile(dir, fileName, desc);

            for (Map.Entry<String, Map<String, String>> entry : databaseInfo.entrySet()) {
                final String tableName = entry.getKey();
                if (isExcludeTable(tableName)) {
                    countDownLatch.countDown();
                    continue;
                }
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String sql = exportTable(queryRunner, tableName);
                            if (sql != null && !sql.trim().equals("")) {
                                queue.add(sql);
                                System.out.println("sql===:" + sql + "," + tableName);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            countDownLatch.countDown();
                        }

                    }
                });
            }
            countDownLatch.await();

            saveFile(queue, dir, fileName, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executorService.shutdown();
        }
    }

    private static void writeDescToFile(String dir, String fileName, String desc) throws Exception {
        if (desc == null || desc.trim().equals("")) return;
        saveFile("-- " + desc + "\r\n", dir, fileName, true);
    }

    private static void deleteFile(String dir, String filePath) {
        String fileURL = dir + "/" + filePath;
        File file = new File(fileURL);
        while (file.delete()) {
        }
    }

    public static String exportTable(QueryRunner queryRunner, String tableName) throws SQLException {
        Map<String, Map<String, String>> databaseInfo = getDatabaseInfo(queryRunner.getDataSource());
        Map<String, String> tableInfo = databaseInfo.get(tableName.toUpperCase());
        if (tableInfo == null || tableInfo.size() < 1) {
            throw new RuntimeException("没有这个表");
        }
        if (isExcludeTable(tableName)) {
            return "";
        }
        String sql = exportToSql(queryRunner, tableName, "");
        return sql;
//        saveFile(sb.toString(), fileName, isAppend);
    }

    private static void saveFile(String sb, String dir, String fileName, boolean isAppend) throws Exception {
        String file = dir + "/" + fileName;
        OutputStream fis = new FileOutputStream(file, isAppend);
        OutputStreamWriter isr = new OutputStreamWriter(fis, Charset.forName("UTF-8"));
        BufferedWriter br = new BufferedWriter(isr);
        br.write(sb);
        br.close();
    }

    private static void saveFile(Queue<String> queue, String dir, String fileName, boolean isAppend) throws Exception {
        String file = dir + "/" + fileName;
        OutputStream fis = new FileOutputStream(file, isAppend);
        OutputStreamWriter isr = new OutputStreamWriter(fis, Charset.forName("UTF-8"));
        BufferedWriter br = new BufferedWriter(isr);
        for (String str : queue) {
            br.write(str);
        }
        br.close();
    }

    /**
     * 这些表数据不导出
     * @return
     */
    public static String excludeTableName() {
        return "demo, demo1";
    }

    public static boolean isExcludeTable(String tableName) {
        String[] excludeTables = excludeTableName().split(",");
        for (String excludeTable : excludeTables) {
            if (tableName.toUpperCase().equals(excludeTable.trim().toUpperCase())) {
                return true;
            }
        }
        return false;
    }

//    public static String getDbPlotDir() {
//        String os = System.getProperty("os.name");
//        if (os.toLowerCase().startsWith("win")) {
//            return "";
//        } else if (os.toLowerCase().startsWith("linux")) {
//            return "";
//        } else {
//            throw new RuntimeException("判断不出当前是哪个系统");
//        }
//    }

    public static List<String> keyword() {
        return Arrays.asList("order");
    }

    public static void load(QueryRunner queryRunner) {
        clean(queryRunner);
        String file = H2Util.class.getResource("/test-schema.sql").getFile();
        importSystemData(queryRunner, file);

        file = H2Util.class.getResource("/test-data.sql").getFile();
        importSystemData(queryRunner, file);
    }

    protected  static void clean(QueryRunner queryRunner) {
        int num = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(num);
        try {
            Map<String, Map<String, String>> databaseInfo = getDatabaseInfo(queryRunner.getDataSource());
            final AtomicInteger inc = new AtomicInteger(0);
            final CountDownLatch countDownLatch = new CountDownLatch(databaseInfo.size());

            for (Map.Entry<String, Map<String, String>> entry : databaseInfo.entrySet()) {
                final String tableName = entry.getKey();
                executorService.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String newtableName = changeTableName(tableName);
                            String sql = "delete from " + newtableName;
                            queryRunner.update(sql);
                            inc.incrementAndGet();
                        } catch (Exception e) {
                            LOGGER.warning("delete table error!");
                        } finally {
                            countDownLatch.countDown();
                        }
                    }
                });
            }
            countDownLatch.await();
            LOGGER.info(String.format("delet table size:%s, all table size:%s ", inc.get(), databaseInfo.size()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            executorService.shutdown();
        }
    }

    private static String changeTableName(String tableName) {
        for (String kw : keyword()) {
            if (tableName.toLowerCase().equals(kw)) {
                tableName = "`" + kw + "`";
            }
        }
        return tableName;
    }

    public static Map<String, Map<String, String>> getDatabaseInfo(DataSource dataSource) {
        if (!isInit.get()) {
            getTableNameByCon(dataSource);
            isInit.set(true);
        }
        return databaseInfo;
    }

    public static void executeSql(QueryRunner queryRunner, String file) {
//        JdbcTestUtils.executeSqlScript(jdbcTemplate, new FileSystemResource(file), false);
        try (Connection connection = queryRunner.getDataSource().getConnection()) {
            ScriptUtils.executeSqlScript(connection, new FileSystemResource(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getTableNameByCon(DataSource dataSource) {
        Connection con = null;
        try {
            con = dataSource.getConnection();
            DatabaseMetaData meta = con.getMetaData();
            ResultSet rs = meta.getTables(null, null, null, new String[] { "TABLE" });
            while (rs.next()) {
                Map<String, String> tableInfo = new LinkedHashMap<String, String>();
                String tableName = rs.getString(3);
//                System.out.println("表名：" + tableName);
                databaseInfo.put(tableName, tableInfo);

                ResultSet colRet = meta.getColumns(null, "%", tableName, "%");
                while (colRet.next()) {
                    String columnName = colRet.getString("COLUMN_NAME");
//                    String columnType = colRet.getString("TYPE_NAME");
//                    int datasize = colRet.getInt("COLUMN_SIZE");
//                    int digits = colRet.getInt("DECIMAL_DIGITS");
//                    int nullable = colRet.getInt("NULLABLE");
                    tableInfo.put(columnName, columnName);
                }
            }
            con.close();
        } catch (Exception e) {
            try {
                con.close();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
    }

    public static String exportToSql(QueryRunner queryRunner, String table, String where, Object...param) throws SQLException {
        Map databaseInfo = getDatabaseInfo(queryRunner.getDataSource());
        Map tableInfo = (Map)databaseInfo.get(table);
        if (table.equalsIgnoreCase("order")) {
            table = "`" + table + "`";
        }
        String sql = "select * from " + table + " " + where;
        MapListHandler beanHandler = new MapListHandler(new ClobRowProcessor());
        List<Map<String, Object>> list = queryRunner.query(sql, beanHandler);
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> map : list) {
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
        if (value instanceof Integer || value instanceof Long) {
            return value;
        }
        String newValue = StringUtils.replace(value.toString(), "'", "''");
        return "'" + newValue + "'";
    }

}
