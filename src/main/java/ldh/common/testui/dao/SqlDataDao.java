package ldh.common.testui.dao;

import ldh.common.testui.model.SqlData;
import ldh.common.testui.util.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.*;
import java.util.List;

/**
 * Created by ldh on 2018/3/17.
 */
public class SqlDataDao {

    public static void save(SqlData sqlData) {
        if (sqlData.getId() != null && sqlData.getId() != 0) {
            update(sqlData);
        } else {
            insert(sqlData);
        }
    }

    public static void insert(SqlData sqlData) {
        ResultSetHandler<Integer> h = new ScalarHandler();
        String insertSql = "insert into sql_data(tree_node_id, database_param_id,  handle_type,  data_type, data" +
                ") values(?, ?, ?, ?, ?)";
        long id = 0;
        try {
            id = DbUtils.getQueryRunner().insert(insertSql, h, sqlData.getTreeNodeId(), sqlData.getDatabaseParamId(), sqlData.getHandleType().name(),
                    sqlData.getDataType().name(), sqlData.getData());
            sqlData.setId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static SqlData getById(Integer id) throws SQLException {
        String sql = "select id, tree_node_id as treeNodeId, database_param_id as databaseParamId, handle_type as handleType, data_type as dataType, data" +
                " from sql_data where id = ?";
        ResultSetHandler<SqlData> beanHandler = new BeanHandler<>(SqlData.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, id);
    }

    public static List<SqlData> getByTreeNodeId(Integer treeNodeId) throws SQLException {
        String sql = "select id, tree_node_id as treeNodeId, database_param_id as databaseParamId, handle_type as handleType, data_type as dataType, data" +
                " from sql_data where tree_node_id = ?";
        ResultSetHandler<List<SqlData>> beanHandler = new BeanListHandler<SqlData>(SqlData.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, treeNodeId);
    }

//    public static void delete(Integer id) throws SQLException {
//        SqlDataDao.deleteBySqlDataId(id);
//        DbUtils.getQueryRunner().update("delete from sql_check where id = ?",  id);
//    }

    public static void deleteByTreeNodeId(int treeNodeId) {
        try {
            DbUtils.getQueryRunner().update("delete from sql_data where tree_node_id = ?", treeNodeId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update(SqlData sqlData) {
        try {
            DbUtils.getQueryRunner().update("update sql_data set database_param_id = ?, handle_type=?, data_type=?, data=? " +
                            " where id = ?",
                    sqlData.getDatabaseParamId(), sqlData.getHandleType().name(), sqlData.getDataType().name(), sqlData.getData(), sqlData.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
