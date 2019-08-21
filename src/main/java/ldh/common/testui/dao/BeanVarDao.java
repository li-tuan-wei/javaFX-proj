package ldh.common.testui.dao;

import javafx.scene.control.TreeItem;
import ldh.common.testui.model.*;
import ldh.common.testui.util.DataUtil;
import ldh.common.testui.util.DbUtils;
import ldh.common.testui.util.MethodUtil;
import ldh.common.testui.util.VarUtil;
import ldh.common.testui.vo.SqlCheck;
import ldh.common.testui.vo.SqlColumn;
import ldh.common.testui.vo.SqlColumnData;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ldh on 2018/3/17.
 */
public class BeanVarDao {

    public static void save(BeanVar beanVar) {
        if (beanVar.getId() != null && beanVar.getId() != 0) {
            update(beanVar);
        } else {
            insert(beanVar);
        }
    }

    public static void insert(BeanVar beanVar) {
        ResultSetHandler<Integer> h = new ScalarHandler();
        String insertSql = "insert into bean_var(index, tree_node_id, name, type, database_param_id, sql" +
                ", args, package_param_id,class_name,instance_class_type, method_name) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        int id = 0;
        String classTypeName = beanVar.getInstanceClassType() == null ? null : beanVar.getInstanceClassType().name();
        String methodName = beanVar.getMethod() == null ? null : MethodUtil.buildMethodName(beanVar.getMethod());
        try {
            id = DbUtils.getQueryRunner().insert(insertSql, h, beanVar.getIndex(), beanVar.getTreeNodeId(), beanVar.getName(), beanVar.getType().name(), beanVar.getDatabaseParamId(),
                    beanVar.getSql(), beanVar.getArgs(), beanVar.getPackageParamId(), beanVar.getClassName(), classTypeName, methodName);
            beanVar.setId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static BeanVar getByName(String name) throws SQLException {
        String sql = "select id, index, tree_node_id as treeNodeId, name, type, database_param_id as databaseParamId, sql," +
                "args, package_param_id as packageParamId, class_name, instance_class_type as instanceClassType, method_name methodName from bean_var where tree_node_id = ?";
        ResultSetHandler<BeanVar> beanHandler = new BeanHandler<>(BeanVar.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, name);
    }

    public static List<BeanVar> getByTreeNodeId(Integer treeNodeId) throws SQLException {
        String sql = "select id, index, tree_node_id as treeNodeId, name, type, database_param_id as databaseParamId, sql," +
                "args, package_param_id as packageParamId, class_name as className, instance_class_type as instanceClassType, method_name methodName from bean_var where tree_node_id = ? order by index";
        ResultSetHandler<List<BeanVar>> beanHandler = new BeanListHandler<BeanVar>(BeanVar.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, treeNodeId);
    }

    public static void delete(Integer id) throws SQLException {
        DbUtils.getQueryRunner().update("delete from bean_var where id = ?",  id);
    }

    public static void deleteByTreeNodeId(int treeNodeId) {
        try {
            DbUtils.getQueryRunner().update("delete from bean_var where tree_node_id = ?", treeNodeId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update(BeanVar beanVar) {
        try {
            String instanceClassType = beanVar.getInstanceClassType() == null ? null : beanVar.getInstanceClassType().name();
            String methodName = beanVar.getMethod() == null ? null : MethodUtil.buildMethodName(beanVar.getMethod());
            DbUtils.getQueryRunner().update("update bean_var set index = ?, name = ?, type = ?, database_param_id = ?, sql=?, args=?," +
                            " package_param_id = ?,class_name=?, instance_class_type=?, method_name = ? where id = ?",
                    beanVar.getIndex(), beanVar.getName(), beanVar.getType().name(), beanVar.getDatabaseParamId(), beanVar.getSql(), beanVar.getArgs(),
                    beanVar.getPackageParamId(), beanVar.getClassName(), instanceClassType, methodName, beanVar.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, SqlColumnData> getSqlDataForSql(BeanVar beanVar, Map<String, Object> paramMap) throws Exception {
        Map<String, SqlColumnData> sqlColumnDatas = new HashMap();
        Connection connection = null;
        try {
            ParamModel paramModel = DataUtil.getById(beanVar.getDatabaseParamId());
            connection = DbUtils.getConnection(paramModel);
            String sql = VarUtil.replaceLine(beanVar.getSql());
            PreparedStatement ps = connection.prepareStatement(sql);
            if (!beanVar.getArgs().equals("")) {
                Object[] params = VarUtil.vars(beanVar.getArgs(), paramMap);
                int i = 1;
                for (Object param : params) {
                    ps.setObject(i++, param);
                }
            }
            ps.execute();

            List<SqlColumn> sqlColumns = new ArrayList<>();
            ResultSetMetaData rsm = ps.getMetaData();
            int cols = rsm.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                SqlColumn sqlColumn = new SqlColumn();
                sqlColumn.setColumnName(rsm.getColumnName(i));
                sqlColumn.setColumnType(rsm.getColumnType(i));
                sqlColumns.add(sqlColumn);
            }

            ResultSet rs = ps.getResultSet();
            while(rs.next()) {
                for (SqlColumn sqlColumn : sqlColumns) {
                    SqlColumnData sqlColumnData = new SqlColumnData();
                    sqlColumnData.setValue(rs.getObject(sqlColumn.getColumnName()));
                    sqlColumnData.setSqlColumn(sqlColumn);
                    sqlColumnDatas.put(sqlColumn.getColumnName(), sqlColumnData);
                }
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            DbUtils.close(connection);
        }
        return sqlColumnDatas;
    }
}
