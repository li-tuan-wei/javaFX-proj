package ldh.common.testui.util;

import ldh.common.testui.model.ParamModel;
import ldh.common.testui.vo.DatabaseParam;
import org.apache.commons.dbutils.QueryRunner;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ldh123 on 2017/6/7.
 */
public class DbUtils {

    public static QueryRunner getQueryRunner() {
        return new QueryRunner(UiUtil.DATA_SOURCE);
    }

    public static Connection getConnection(ParamModel paramModel) throws Exception {
        DatabaseParam databaseParam = JsonUtil.toObject(paramModel.getValue(), DatabaseParam.class);
        if (!StringUtils.isEmpty(databaseParam.getSpringDatabaseName())) {
            DataSource dataSource = (DataSource) SpringInitFactory.getInstance().getBean(databaseParam.getSpringDatabaseName());
            return dataSource.getConnection();
        }
        Class.forName(getDriverName(databaseParam));
        return DriverManager.getConnection(databaseParam.getUrl(), databaseParam.getUserName(), databaseParam.getPassword());
    }

    public static void close(Connection connection) {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getDriverName(DatabaseParam databaseParam) {
        if (databaseParam.getUrl().startsWith("")) {
            return "com.mysql.jdbc.Driver";
        }
        throw new RuntimeException("不支持这种数据库");
    }


    public static Connection getConnection(String driverName, String url, String userName, String password) throws Exception {
        Class.forName(driverName);
        Connection con = DriverManager.getConnection(url,userName,password);
        return con;
    }

    public static void close(Connection connection, ResultSet tableRet) {
        if (tableRet != null) {
            try {
                tableRet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        close(connection);
    }

    public static void close(Connection connection, Statement statement, ResultSet tableRet) {
        if (tableRet != null) {
            try {
                tableRet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        close(connection);
    }

}
