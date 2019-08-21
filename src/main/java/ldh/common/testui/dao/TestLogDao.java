package ldh.common.testui.dao;

import ldh.common.testui.model.TestLog;
import ldh.common.testui.model.TestLogData;
import ldh.common.testui.util.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;

public class TestLogDao {

    public static void save(TestLog testLog) throws SQLException {
        if (testLog.getId() != null && testLog.getId() != 0) {
            update(testLog);
        } else {
            insert(testLog);
        }
    }

    public static void insert(TestLog testLog) throws SQLException {
        ResultSetHandler<Long> h = new ScalarHandler();
        String insertSql = "insert into test_log(name, create_time, type, parent_id, success, success_num, failure_num) values(?, ?, ?, ?, ?, ?, ?)";
        Long id = DbUtils.getQueryRunner().insert(insertSql, h, testLog.getName(), testLog.getCreateTime(), testLog.getType(), testLog.getParentId(), testLog.getRunSuccess(),
                testLog.getSuccessNum(), testLog.getFailureNum());
        testLog.setId(id.intValue());
    }

    public static List<TestLog> getLastedNum(int num) throws SQLException {
        String sql = "select id, name, create_time createTime, type, parent_id parentId, success runSuccess, success_num successNum, failure_num failureNum from test_log where parent_id = 0 order by create_time desc limit " + num;
        ResultSetHandler<List<TestLog>> beanHandler = new BeanListHandler<TestLog>(TestLog.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler);
    }

    public static List<TestLog> getByParentId(int parentId) throws SQLException {
        String sql = "select id, name, create_time createTime, type, parent_id parentId, success runSuccess, success_num successNum, failure_num failureNum from test_log where parent_id = ? order by create_time";
        ResultSetHandler<List<TestLog>> beanHandler = new BeanListHandler<TestLog>(TestLog.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, parentId);
    }

    public static TestLog getById(int id) throws SQLException {
        String sql = "select id, name, create_time createTime, type, parent_id parentId, success runSuccess, success_num successNum, failure_num failureNum from test_log where id = ?";
        ResultSetHandler<TestLog> beanHandler = new BeanHandler<TestLog>(TestLog.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, id);
    }

    public static void delete(TestLog testLog) throws SQLException {
        TestLogDataDao.deleteByTestLogId(testLog.getId());
        List<TestLog> testLogs = TestLogDao.getByParentId(testLog.getId());
        for(TestLog testLog1 : testLogs) {
            TestLogDataDao.deleteByTestLogId(testLog1.getId());
            delete(testLog1);
        }
        DbUtils.getQueryRunner().update("delete from test_log where id = ?", testLog.getId());
    }

    public static void update(TestLog testLog) throws SQLException {
        DbUtils.getQueryRunner().update("update test_log set success = ?, success_num = ?, failure_num = ? where id = ?", testLog.getRunSuccess(),
                testLog.getSuccessNum(), testLog.getFailureNum(), testLog.getId());
    }
}
