package ldh.common.testui.dao;

import com.google.gson.reflect.TypeToken;
import javafx.scene.control.TreeItem;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.DbUtils;
import ldh.common.testui.util.JsonUtil;
import ldh.common.testui.util.VarUtil;
import ldh.common.testui.vo.SqlCheck;
import ldh.common.testui.vo.SqlCheckData;
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
import java.util.stream.Collectors;

/**
 * Created by ldh on 2018/3/17.
 */
public class SqlCheckDataDao {

    public static void save(SqlCheckData sqlCheckData) {
        if (sqlCheckData.getId() != null && sqlCheckData.getId() != 0) {
            update(sqlCheckData);
        } else {
            insert(sqlCheckData);
        }
    }

    public static void insert(SqlCheckData sqlCheckData) {
        ResultSetHandler<Integer> h = new ScalarHandler();
        String insertSql = "insert into sql_check_data(sql_check_id, index, content) values(?, ?, ?)";
        int id = 0;
        try {
            id = DbUtils.getQueryRunner().insert(insertSql, h, sqlCheckData.getSqlCheckId(), sqlCheckData.getIndex(), sqlCheckData.getContent());
            sqlCheckData.setId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static SqlCheckData getById(Integer id) throws SQLException {
        String sql = "select id, sql_check_id as sqlCheckId, content" +
                " from sql_check_data where id = ?";
        ResultSetHandler<SqlCheckData> beanHandler = new BeanHandler<>(SqlCheckData.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, id);
    }

    public static List<SqlCheckData> getBySqlCheckId(Integer sqlCheckId) throws SQLException {
        String sql = "select id, sql_check_id as sqlCheckId, content, index" +
                " from sql_check_data where sql_check_id = ? order by index";
        ResultSetHandler<List<SqlCheckData>> beanHandler = new BeanListHandler<SqlCheckData>(SqlCheckData.class);
        List<SqlCheckData> result = DbUtils.getQueryRunner().query(sql, beanHandler, sqlCheckId);
        int i = 0;
        for (SqlCheckData sqlCheckData : result) {
            if (sqlCheckData.getIndex() == null || sqlCheckData.getIndex() == 0) {
                sqlCheckData.setIndex(++i);
            }
        }
        return result;
    }

    public static void delete(Integer id) throws SQLException {
        DbUtils.getQueryRunner().update("delete from sql_check_data where id = ?",  id);
    }

    public static void deleteBySqlCheckId(int treeNodeId) {
        try {
            DbUtils.getQueryRunner().update("delete from sql_check_data where sql_check_id = ?", treeNodeId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update(SqlCheckData sqlCheckData) {
        try {
            DbUtils.getQueryRunner().update("update sql_check_data set content= ?, index = ? where id = ?",
                    sqlCheckData.getContent(), sqlCheckData.getIndex(), sqlCheckData.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

//    public static List<Map<String, SqlColumnData>> getSqlCheckData(SqlCheck sqlCheck) throws SQLException {
//        List<Map<String, SqlColumnData>> result = new ArrayList<>();
//        List<SqlCheckData> sqlCheckDatas = getBySqlCheckId(sqlCheck.getId());
//        for (SqlCheckData data : sqlCheckDatas) {
//            Map<String, SqlColumnData> dataMap = data.toSqlColumnDataMap();
//            result.add(dataMap);
//        }
//        return result;
//    }

    public static List<SqlCheckData> getSqlCheckData(SqlCheck sqlCheck) throws SQLException {
        List<SqlCheckData> sqlCheckDatas = getBySqlCheckId(sqlCheck.getId());
        return sqlCheckDatas;
    }
}
