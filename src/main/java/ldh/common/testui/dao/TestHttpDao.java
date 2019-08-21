package ldh.common.testui.dao;

import ldh.common.testui.model.TestHttp;
import ldh.common.testui.model.TestMethod;
import ldh.common.testui.util.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;

public class TestHttpDao {

    public static void save(TestHttp testHttp) throws SQLException {
        if (testHttp.getId() != null && testHttp.getId() != 0) {
            update(testHttp);
        } else {
            insert(testHttp);
        }
    }

    public static void insert(TestHttp testHttp) throws SQLException {
        ResultSetHandler<Integer> h = new ScalarHandler();
        String insertSql = "insert into test_http(tree_node_id, url, method) values(?, ?, ?)";
        int id = DbUtils.getQueryRunner().insert(insertSql, h, testHttp.getTreeNodeId(), testHttp.getUrl(), testHttp.getMethod().name());
        testHttp.setId(id);
    }

    public static List<TestHttp> getByTreeNodeId(int treeNodeId) throws SQLException {
        String sql = "select id, tree_node_id as treeNodeId, url, method from test_http where tree_node_id = ?";
        ResultSetHandler<List<TestHttp>> beanHandler = new BeanListHandler<TestHttp>(TestHttp.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, treeNodeId);
    }

    public static void delete(TestHttp testHttp) throws SQLException {
        DbUtils.getQueryRunner().update("delete from test_http where id = ?", testHttp.getId());
    }

    public static void update(TestHttp testHttp) throws SQLException {
        DbUtils.getQueryRunner().update("update test_http set url = ?, method = ?  where id = ?", testHttp.getUrl(),
                testHttp.getMethod().name(), testHttp.getId());
    }

    public static void deleteByTreeNodeId(int treeNodeId) throws SQLException {
        List<TestHttp> testHttps = getByTreeNodeId(treeNodeId);
        for (TestHttp testHttp : testHttps) {
            TestHttpParamDao.deleteByTestHttpId(testHttp.getId());
            TestHttpResultDao.deleteByTestHttpId(testHttp.getId());
            TestHttpBodyDao.deleteByTestHttpId(testHttp.getId());
        }
        DbUtils.getQueryRunner().update("delete from test_http where tree_node_id = ?", treeNodeId);
    }
}
