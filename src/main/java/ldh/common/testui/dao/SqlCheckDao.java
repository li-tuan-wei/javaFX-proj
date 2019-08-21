package ldh.common.testui.dao;

import ldh.common.testui.model.ParamModel;
import ldh.common.testui.util.DbUtils;
import ldh.common.testui.util.StringUtil;
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
public class SqlCheckDao {

    public static void save(SqlCheck sqlCheck) {
        if (sqlCheck.getId() != null && sqlCheck.getId() != 0) {
            update(sqlCheck);
        } else {
            insert(sqlCheck);
        }
    }

    public static void insert(SqlCheck sqlCheck) {
        ResultSetHandler<Integer> h = new ScalarHandler();
        String insertSql = "insert into sql_check(tree_node_id, name, database_param_id, sql, sql_struct, args" +
                ") values(?, ?, ?, ?, ?, ?)";
        int id = 0;
        try {
            id = DbUtils.getQueryRunner().insert(insertSql, h, sqlCheck.getTreeNodeId(), sqlCheck.getName(), sqlCheck.getDatabaseParamId(),
                    sqlCheck.getSql(), sqlCheck.getSqlStruct(), sqlCheck.getArgs());
            sqlCheck.setId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static SqlCheck getById(Integer id) throws SQLException {
        String sql = "select id, tree_node_id as treeNodeId, name, database_param_id as databaseParamId, sql, sql_struct as sqlStruct, args" +
                " from sql_check where id = ?";
        ResultSetHandler<SqlCheck> beanHandler = new BeanHandler<>(SqlCheck.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, id);
    }

    public static List<SqlCheck> getByTreeNodeId(Integer treeNodeId) throws SQLException {
        String sql = "select id, tree_node_id as treeNodeId, name, database_param_id as databaseParamId, sql, sql_struct as sqlStruct, args" +
                " from sql_check where tree_node_id = ?";
        ResultSetHandler<List<SqlCheck>> beanHandler = new BeanListHandler<SqlCheck>(SqlCheck.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, treeNodeId);
    }

    public static void delete(Integer id) throws SQLException {
        SqlCheckDataDao.deleteBySqlCheckId(id);
        DbUtils.getQueryRunner().update("delete from sql_check where id = ?",  id);
    }

    public static void deleteByTreeNodeId(int treeNodeId) {
        try {
            DbUtils.getQueryRunner().update("delete from sql_check where tree_node_id = ?", treeNodeId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update(SqlCheck sqlCheck) {
        try {
            DbUtils.getQueryRunner().update("update sql_check set name = ?, database_param_id = ?, sql=?, sql_struct=?, args=? " +
                            " where id = ?",
                    sqlCheck.getName(), sqlCheck.getDatabaseParamId(), sqlCheck.getSql(), sqlCheck.getSqlStruct(), sqlCheck.getArgs(), sqlCheck.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static List<SqlColumn> getSqlStructForSql(String sqlStruct, ParamModel paramModel) throws Exception {
        List<SqlColumn> sqlColumns = new ArrayList<>();
        Connection connection = null;
        try {
            connection = DbUtils.getConnection(paramModel);
            PreparedStatement rs = connection.prepareStatement(sqlStruct);
            rs.execute();
            ResultSetMetaData rsm = rs.getMetaData();
            int cols = rsm.getColumnCount();
            for (int i = 1; i <= cols; i++) {
                SqlColumn sqlColumn = new SqlColumn();
                String columnName = rsm.getColumnName(i);
                sqlColumn.setColumnName(columnName);
                if (StringUtil.isUpperString(columnName)) {
                    sqlColumn.setColumnName(columnName.toLowerCase());
                }
                sqlColumn.setColumnType(rsm.getColumnType(i));
                sqlColumns.add(sqlColumn);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.close(connection);
        }
        return sqlColumns;
    }

    public static List<Map<String, SqlColumnData>> getSqlDataForSql(SqlCheck sqlCheck, ParamModel paramModel, Map<String, Object> paramMap) throws Exception {
        List<Map<String, SqlColumnData>> sqlColumnDatas = new ArrayList<>();
        Connection connection = null;
        try {
            connection = DbUtils.getConnection(paramModel);
            String sql = VarUtil.replaceLine(sqlCheck.getSql());
            PreparedStatement ps = connection.prepareStatement(sql);
            if (!sqlCheck.getArgs().equals("")) {
                Object[] params = VarUtil.vars(sqlCheck.getArgs(), paramMap);
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
                Map<String, SqlColumnData> dataMap = new HashMap<>();
                for (SqlColumn sqlColumn : sqlColumns) {
                    SqlColumnData sqlColumnData = new SqlColumnData();
                    sqlColumnData.setValue(rs.getObject(sqlColumn.getColumnName()));
                    sqlColumnData.setSqlColumn(sqlColumn);
                    dataMap.put(sqlColumn.getColumnName(), sqlColumnData);
                }
                sqlColumnDatas.add(dataMap);
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
