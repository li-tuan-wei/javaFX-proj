package ldh.common.testui.dao;

import ldh.common.testui.model.TestLog;
import ldh.common.testui.model.TestLogData;
import ldh.common.testui.util.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;

public class TestLogDataDao {

    public static void save(TestLogData testLogData) throws SQLException {
        if (testLogData.getId() != null && testLogData.getId() != 0) {

        } else {
            insert(testLogData);
        }
    }

    public static void insert(TestLogData testLogData) throws SQLException {
        ResultSetHandler<Long> h = new ScalarHandler();
        String insertSql = "insert into test_log_data(name, test_log_id, type, content) values(?, ?, ?, ?)";
        Long id = DbUtils.getQueryRunner().insert(insertSql, h, testLogData.getName(), testLogData.getTestLogId(), testLogData.getType().name(), testLogData.getContent());
        testLogData.setId(id.intValue());
    }

    public static List<TestLogData> getByTestLogId(int testLogId) throws SQLException {
        String sql = "select id, name, test_log_id testLogId, type, content from test_log_data where test_log_id = ? ";
        ResultSetHandler<List<TestLogData>> beanHandler = new BeanListHandler<TestLogData>(TestLogData.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, testLogId);
    }

    public static void delete(TestLogData testLogData) throws SQLException {
        DbUtils.getQueryRunner().update("delete from test_log_data where id = ?", testLogData.getId());
    }

    public static void deleteByTestLogId(Integer testLogId) throws SQLException {
        DbUtils.getQueryRunner().update("delete from test_log_data where test_log_id = ?", testLogId);
    }
}
