package ldh.common.testui.dao;

import ldh.common.testui.model.IncrementVar;
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
public class IncrementVarDao {

    public static void save(IncrementVar incrementVar) {
        if (incrementVar.getId() != null && incrementVar.getId() != 0) {
            update(incrementVar);
        } else {
            insert(incrementVar);
        }
    }

    public static void insert(IncrementVar incrementVar) {
        ResultSetHandler<Integer> h = new ScalarHandler();
        String insertSql = "insert into increment_var(name, value, step) values(?, ?, ?)";
        int id = 0;
        try {
            id = DbUtils.getQueryRunner().insert(insertSql, h, incrementVar.getName(), incrementVar.getValue(), incrementVar.getStep());
            incrementVar.setId(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static IncrementVar getByName(String name) throws SQLException {
        String sql = "select * from increment_var where name = ?";
        ResultSetHandler<IncrementVar> beanHandler = new BeanHandler<>(IncrementVar.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, name);
    }

    public static void delete(String name) throws SQLException {
        DbUtils.getQueryRunner().update("delete from increment_var where name = ?", name);
    }

    public static void deleteByTreeNodeId(int treeNodeId) {
        try {
            DbUtils.getQueryRunner().update("delete from increment_var where name in (select name from param where category ='Sql' and tree_node_id = ?)", treeNodeId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void update(IncrementVar incrementVar) {
        try {
            DbUtils.getQueryRunner().update("update increment_var set name = ?, value = ?, step = ? where id = ?",
                    incrementVar.getName(), incrementVar.getValue(), incrementVar.getName(), incrementVar.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Long getAndIncrease(String name) throws SQLException {
        DbUtils.getQueryRunner().update("update increment_var set value = value + step where name = ?", name);
        ResultSetHandler<Long> beanHandler = new ScalarHandler("value");
        return DbUtils.getQueryRunner().query("select value from increment_var where name = ?", beanHandler, name);
    }
}
