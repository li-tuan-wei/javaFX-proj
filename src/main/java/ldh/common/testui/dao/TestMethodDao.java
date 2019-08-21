package ldh.common.testui.dao;

import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.model.TestMethod;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;

public class TestMethodDao {

    public static void save(TestMethod testMethod) throws SQLException {
        if (testMethod.getId() != null && testMethod.getId() != 0) {
            update(testMethod);
        } else {
            insert(testMethod);
        }
    }

    public static void insert(TestMethod testMethod) throws SQLException {
        ResultSetHandler<Integer> h = new ScalarHandler();
        String insertSql = "insert into test_method(tree_node_id, param_id, class_name, method_name, instance_class_name) values(?, ?, ?, ?, ?)";
        int id = DbUtils.getQueryRunner().insert(insertSql, h, testMethod.getTreeNodeId(), testMethod.getParamId(), testMethod.getClassName(), testMethod.getMethodName(), testMethod.getInstanceClassName());
        testMethod.setId(id);
    }

    public static List<TestMethod> getByTreeNodeId(int treeNodeId) throws SQLException {
        String sql = "select id, tree_node_id as treeNodeId, param_id as paramId, class_name as className, method_name as methodName, instance_class_name as instanceClassName from test_method where tree_node_id = ?";
        ResultSetHandler<List<TestMethod>> beanHandler = new BeanListHandler<TestMethod>(TestMethod.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, treeNodeId);
    }

    public static void delete(TestMethod testMethod) throws SQLException {
        DbUtils.getQueryRunner().update("delete from test_method where id = ?", testMethod.getId());
    }

    public static void update(TestMethod testMethod) throws SQLException {
        DbUtils.getQueryRunner().update("update test_method set param_id = ?, class_name = ?, method_name = ?, instance_class_name = ? where id = ?", testMethod.getParamId(),
                testMethod.getClassName(), testMethod.getMethodName(), testMethod.getInstanceClassName(), testMethod.getId());
    }

    public static void deleteByTreeNodeId(int treeNodeId) throws SQLException {
        List<TestMethod> testMethods = getByTreeNodeId(treeNodeId);
        for (TestMethod testMethod : testMethods) {
            TestMethodDataDao.deleteByTestMethodId(testMethod.getId());
            DbUtils.getQueryRunner().update("delete from test_method where tree_node_id = ?", treeNodeId);
        }
    }
}
