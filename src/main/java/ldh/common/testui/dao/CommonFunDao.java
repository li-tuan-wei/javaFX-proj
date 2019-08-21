package ldh.common.testui.dao;

import ldh.common.testui.model.CommonFun;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.util.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by ldh on 2018/3/17.
 */
public class CommonFunDao {

    public static void save(CommonFun commonFun) {
        if (commonFun.getId() != null && commonFun.getId() != 0) {
            update(commonFun);
        } else {
            insert(commonFun);
        }
    }

    public static void insert(CommonFun commonFun) {
        ResultSetHandler<Integer> h = new ScalarHandler();
        String insertSql = "insert into common_fun(tree_node_id, name, package_param_id, class_name, desc) values(?, ?, ?, ?, ?)";
        int id = 0;
        try {
            id = DbUtils.getQueryRunner().insert(insertSql, h, commonFun.getTreeNodeId(), commonFun.getName(), commonFun.getPackageParamId(), commonFun.getClassName(),
                    commonFun.getDesc());
            commonFun.setId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<CommonFun> getAll() throws SQLException {
        String sql = "select id, tree_node_id as treeNodeId, name, package_param_id as packageParamId, class_name className, desc from common_fun";
        ResultSetHandler<List<CommonFun>> beanHandler = new BeanListHandler<>(CommonFun.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler);
    }

    public static CommonFun getById(Integer id) throws SQLException {
        String sql = "select id, tree_node_id as treeNodeId, name, package_param_id as packageParamId, class_name className, desc from common_fun where id = ?";
        ResultSetHandler<CommonFun> beanHandler = new BeanHandler<CommonFun>(CommonFun.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, id);
    }

    public static List<CommonFun> getByTreeNodeId(int treeNodeId) throws SQLException {
        String sql = "select id, tree_node_id as treeNodeId, name, package_param_id as packageParamId, class_name className, desc from common_fun where tree_node_id = ?";
        ResultSetHandler<List<CommonFun>> beanHandler = new BeanListHandler<>(CommonFun.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, treeNodeId);
    }

    public static void delete(CommonFun commonFun) throws SQLException {
        DbUtils.getQueryRunner().update("delete from common_fun where id = ?", commonFun.getId());
    }

    public static void deleteByTreeNodeId(int treeNodeId) {
        try {
            DbUtils.getQueryRunner().update("delete from common_fun where tree_node_id = ?", treeNodeId);
            IncrementVarDao.deleteByTreeNodeId(treeNodeId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update(CommonFun commonFun) {
        try {
            DbUtils.getQueryRunner().update("update common_fun set name = ?, package_param_id = ?, class_name = ?, desc = ? where id = ?",
                    commonFun.getName(), commonFun.getPackageParamId(), commonFun.getClassName(),
                    commonFun.getDesc(), commonFun.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
