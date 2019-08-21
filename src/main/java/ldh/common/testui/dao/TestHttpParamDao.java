package ldh.common.testui.dao;

import ldh.common.testui.model.TestHttp;
import ldh.common.testui.model.TestHttpParam;
import ldh.common.testui.util.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;

public class TestHttpParamDao {

    public static void save(TestHttpParam testHttpParam) throws SQLException {
        if (testHttpParam.getId() != null && testHttpParam.getId() != 0) {
            update(testHttpParam);
        } else {
            insert(testHttpParam);
        }
    }

    public static void insert(TestHttpParam testHttpParam) throws SQLException {
        ResultSetHandler<Integer> h = new ScalarHandler();
        String insertSql = "insert into test_http_param(test_http_id, name, content, type) values(?, ?, ?, ?)";
        int id = DbUtils.getQueryRunner().insert(insertSql, h, testHttpParam.getTestHttpId(), testHttpParam.getName(), testHttpParam.getContent(), testHttpParam.getParamType().name());
        testHttpParam.setId(id);
    }

    public static List<TestHttpParam> getByTestHttpId(int testHttpId) throws SQLException {
        String sql = "select id, test_http_id as testHttpId, name, content, type as paramType from test_http_param where test_http_id = ?";
        ResultSetHandler<List<TestHttpParam>> beanHandler = new BeanListHandler<TestHttpParam>(TestHttpParam.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, testHttpId);
    }

    public static void delete(TestHttpParam testHttpParam) throws SQLException {
        DbUtils.getQueryRunner().update("delete from test_http_param where id = ?", testHttpParam.getId());
    }

    public static void update(TestHttpParam testHttpParam) throws SQLException {
        DbUtils.getQueryRunner().update("update test_http_param set name = ?, content = ?  where id = ?", testHttpParam.getName(),
                testHttpParam.getContent(), testHttpParam.getId());
    }

    public static void deleteByTestHttpId(int testHttpId) throws SQLException {
        DbUtils.getQueryRunner().update("delete from test_http_param where test_http_id = ?", testHttpId);
    }
}
