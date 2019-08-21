package ldh.common.testui.dao;

import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;
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
public class ParamDao {

    public static void save(ParamModel paramModel) {
        if (paramModel.getId() != null && paramModel.getId() != 0) {
            update(paramModel);
        } else {
            insert(paramModel);
        }
    }

    public static void insert(ParamModel paramModel) {
        ResultSetHandler<Integer> h = new ScalarHandler();
        String insertSql = "insert into param(tree_node_id, name, category, index, class_name, value, desc) values(?, ?, ?, ?, ?, ?, ?)";
        int id = 0;
        try {
            id = DbUtils.getQueryRunner().insert(insertSql, h, paramModel.getTreeNodeId(), paramModel.getName(), paramModel.getParamCategory().name(), paramModel.getIndex(), paramModel.getClassName(),
                    paramModel.getValue(), paramModel.getDesc());
            paramModel.setId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<ParamModel> getAll() throws SQLException {
        String sql = "select id, tree_node_id as treeNodeId, name, category as paramCategory, index, class_name className, value, desc from param";
        ResultSetHandler<List<ParamModel>> beanHandler = new BeanListHandler<>(ParamModel.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler);
    }

    public static ParamModel getById(Integer id) throws SQLException {
        String sql = "select id, tree_node_id as treeNodeId, name, category as paramCategory, index, class_name className, value, desc from param where id = ?";
        ResultSetHandler<ParamModel> beanHandler = new BeanHandler<ParamModel>(ParamModel.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, id);
    }

    public static List<ParamModel> getByTreeNodeId(int treeNodeId) throws SQLException {
        String sql = "select id, tree_node_id as treeNodeId, name, category as paramCategory, index seq, class_name className, value, desc from param where tree_node_id = ? order by index";
        ResultSetHandler<List<ParamModel>> beanHandler = new BeanListHandler<>(ParamModel.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, treeNodeId);
    }

    public static void delete(ParamModel paramModel) throws SQLException {
        DbUtils.getQueryRunner().update("delete from param where id = ?", paramModel.getId());
    }

    public static void delete(int treeNodeId) {
        try {
            DbUtils.getQueryRunner().update("delete from param where tree_node_id = ?", treeNodeId);
            IncrementVarDao.deleteByTreeNodeId(treeNodeId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update(ParamModel paramModel) {
        try {
            DbUtils.getQueryRunner().update("update param set name = ?, category = ?, class_name = ?, value = ?, desc = ?, index = ? where id = ?",
                    paramModel.getName(), paramModel.getParamCategory().name(), paramModel.getClassName(), paramModel.getValue(),
                    paramModel.getDesc(), paramModel.getIndex(), paramModel.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
