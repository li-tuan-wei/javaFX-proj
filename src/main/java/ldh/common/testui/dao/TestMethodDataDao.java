package ldh.common.testui.dao;

import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.model.TestMethod;
import ldh.common.testui.model.TestMethodData;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;

public class TestMethodDataDao {

    public static void save(TestMethodData testMethodData) throws SQLException {
        if (testMethodData.getId() != null && testMethodData.getId() != 0) {
            update(testMethodData);
        } else {
            insert(testMethodData);
        }
    }

    public static void insert(TestMethodData testMethodData) throws SQLException {
        ResultSetHandler<Integer> h = new ScalarHandler();
        String insertSql = "insert into test_method_data(test_method_id, name, data, var_name) values(?, ?, ?, ?)";
        int id = DbUtils.getQueryRunner().insert(insertSql, h, testMethodData.getTestMethodId(), testMethodData.getTestName(), testMethodData.getData(), testMethodData.getVarName());
        testMethodData.setId(id);
    }

    public static List<TestMethodData> getByTestMethodId(int testMethodId) throws SQLException {
        String sql = "select id, test_method_id as testMethodId, name as testName, data, var_name as varName from test_method_data where test_method_id = ?";
        ResultSetHandler<List<TestMethodData>> beanHandler = new BeanListHandler<TestMethodData>(TestMethodData.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, testMethodId);
    }

    public static void delete(int testMethodDataId) throws SQLException {
        DbUtils.getQueryRunner().update("delete from test_method_data where id = ?", testMethodDataId);
    }

    public static void deleteByTestMethodId(int paramId) throws SQLException {
        DbUtils.getQueryRunner().update("delete from test_method_data where test_method_id = ?", paramId);
    }

    public static void update(TestMethodData testMethodData) throws SQLException {
        DbUtils.getQueryRunner().update("update test_method_data set data = ? where id = ?", testMethodData.getData(), testMethodData.getId());
    }

}
