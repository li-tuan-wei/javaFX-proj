package ldh.common.testui.dao;

import ldh.common.testui.model.TestHttpResult;
import ldh.common.testui.model.TestHttpResult;
import ldh.common.testui.util.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;

public class TestHttpResultDao {

    public static void save(TestHttpResult testHttpResult) throws SQLException {
        if (testHttpResult.getId() != null && testHttpResult.getId() != 0) {
            update(testHttpResult);
        } else {
            insert(testHttpResult);
        }
    }

    public static void insert(TestHttpResult testHttpResult) throws SQLException {
        ResultSetHandler<Integer> h = new ScalarHandler();
        String insertSql = "insert into test_http_result(test_http_id, result, var_name) values(?, ?, ?)";
        int id = DbUtils.getQueryRunner().insert(insertSql, h, testHttpResult.getTestHttpId(), testHttpResult.getResult(), testHttpResult.getVarName());
        testHttpResult.setId(id);
    }

    public static List<TestHttpResult> getByTestHttpId(int testHttpId) throws SQLException {
        String sql = "select id, test_http_id as testHttpId, varName, result from test_http_result where test_http_id = ?";
        ResultSetHandler<List<TestHttpResult>> beanHandler = new BeanListHandler<TestHttpResult>(TestHttpResult.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, testHttpId);
    }

    public static void delete(TestHttpResult testHttpResult) throws SQLException {
        DbUtils.getQueryRunner().update("delete from test_http_result where id = ?", testHttpResult.getId());
    }

    public static void update(TestHttpResult testHttpResult) throws SQLException {
        DbUtils.getQueryRunner().update("update test_http_result set var_name = ?, result = ?  where id = ?", testHttpResult.getVarName(),
                testHttpResult.getResult(), testHttpResult.getId());
    }

    public static void deleteByTestHttpId(int testHttpId) throws SQLException {
        DbUtils.getQueryRunner().update("delete from test_http_result where test_http_id = ?", testHttpId);
    }
}
