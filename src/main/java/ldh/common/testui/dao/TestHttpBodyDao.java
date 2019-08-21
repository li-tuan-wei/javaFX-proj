package ldh.common.testui.dao;

import ldh.common.testui.model.TestHttpBody;
import ldh.common.testui.util.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;

public class TestHttpBodyDao {

    public static void save(TestHttpBody testHttpBody) throws SQLException {
        if (testHttpBody.getId() != null && testHttpBody.getId() != 0) {
            update(testHttpBody);
        } else {
            insert(testHttpBody);
        }
    }

    public static void insert(TestHttpBody testHttpBody) throws SQLException {
        ResultSetHandler<Integer> h = new ScalarHandler();
        String insertSql = "insert into test_http_body(test_http_id, content_type, body) values(?, ?, ?)";
        int id = DbUtils.getQueryRunner().insert(insertSql, h, testHttpBody.getTestHttpId(), testHttpBody.getContentType(), testHttpBody.getBody());
        testHttpBody.setId(id);
    }

    public static List<TestHttpBody> getByTestHttpId(int testHttpId) throws SQLException {
        String sql = "select id, test_http_id as testHttpId, content_type as contentType, body from test_http_body where test_http_id = ?";
        ResultSetHandler<List<TestHttpBody>> beanHandler = new BeanListHandler<TestHttpBody>(TestHttpBody.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, testHttpId);
    }

    public static void delete(TestHttpBody testHttpBody) throws SQLException {
        DbUtils.getQueryRunner().update("delete from test_http_body where id = ?", testHttpBody.getId());
    }

    public static void update(TestHttpBody testHttpBody) throws SQLException {
        DbUtils.getQueryRunner().update("update test_http_body set content_type = ?, body = ?  where id = ?", testHttpBody.getContentType(),
                testHttpBody.getBody(), testHttpBody.getId());
    }

    public static void deleteByTestHttpId(int testHttpId) throws SQLException {
        DbUtils.getQueryRunner().update("delete from test_http_body where test_http_id = ?", testHttpId);
    }
}
