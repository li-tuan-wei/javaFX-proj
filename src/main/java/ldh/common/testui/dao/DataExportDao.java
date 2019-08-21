package ldh.common.testui.dao;

import ldh.common.testui.model.DataExport;
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
public class DataExportDao {

    public static void save(DataExport dataExport) {
        if (dataExport.getId() != null && dataExport.getId() != 0) {
            update(dataExport);
        } else {
            insert(dataExport);
        }
    }

    public static void insert(DataExport dataExport) {
        ResultSetHandler<Long> h = new ScalarHandler();
        String insertSql = "insert into data_export(tree_node_id, database_param_id, dir,  name, data" +
                ") values(?, ?, ?, ?, ?)";
        long id = 0;
        try {
            id = DbUtils.getQueryRunner().insert(insertSql, h, dataExport.getTreeNodeId(), dataExport.getDatabaseParamId(), dataExport.getDir(),
                    dataExport.getName(), dataExport.getData());
            dataExport.setId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static DataExport getById(Integer id) throws SQLException {
        String sql = "select id, tree_node_id as treeNodeId, database_param_id as databaseParamId, dir, name, data" +
                " from data_export where id = ?";
        ResultSetHandler<DataExport> beanHandler = new BeanHandler<>(DataExport.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, id);
    }

    public static List<DataExport> getByTreeNodeId(Integer treeNodeId) throws SQLException {
        String sql = "select id, tree_node_id as treeNodeId, database_param_id as databaseParamId, dir, name, data" +
                " from data_export where tree_node_id = ?";
        ResultSetHandler<List<DataExport>> beanHandler = new BeanListHandler<DataExport>(DataExport.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, treeNodeId);
    }

//    public static void delete(Integer id) throws SQLException {
//        DataExportDao.deleteByDataExportId(id);
//        DbUtils.getQueryRunner().update("delete from sql_check where id = ?",  id);
//    }

    public static void deleteByTreeNodeId(int treeNodeId) {
        try {
            DbUtils.getQueryRunner().update("delete from data_export where tree_node_id = ?", treeNodeId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update(DataExport dataExport) {
        try {
            DbUtils.getQueryRunner().update("update data_export set database_param_id = ?, dir=?, name=?, data=? " +
                            " where id = ?",
                    dataExport.getDatabaseParamId(), dataExport.getDir(), dataExport.getName(), dataExport.getData(), dataExport.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
