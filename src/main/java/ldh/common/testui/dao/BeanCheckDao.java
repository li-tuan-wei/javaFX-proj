package ldh.common.testui.dao;

import ldh.common.testui.model.BeanCheck;
import ldh.common.testui.util.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;

public class BeanCheckDao {

    public static void save(BeanCheck beanCheck) throws SQLException {
        if (beanCheck.getId() != null && beanCheck.getId() != 0) {
            update(beanCheck);
        } else {
            insert(beanCheck);
        }
    }

    public static void insert(BeanCheck beanCheck) throws SQLException {
        ResultSetHandler<Long> h = new ScalarHandler();
        String insertSql = "insert into bean_check(tree_node_id, check_name, bean_type, other_info, columns, content) values(?, ?, ?, ?, ?, ?)";
        Long id = DbUtils.getQueryRunner().insert(insertSql, h, beanCheck.getTreeNodeId(), beanCheck.getCheckName(), beanCheck.getBeanType().name(),
                beanCheck.getOtherInfo(), beanCheck.getColumns(), beanCheck.getContent());
        beanCheck.setId(id.intValue());
    }

    public static BeanCheck getByTreeNodeId(int treeNodeId) throws SQLException {
        String sql = "select id, tree_node_id as treeNodeId, check_name checkName, bean_type beanType, other_info otherInfo, columns, content from bean_check where tree_node_id = ?";
        ResultSetHandler<BeanCheck> beanHandler = new BeanHandler<BeanCheck>(BeanCheck.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, treeNodeId);
    }

    public static void delete(BeanCheck beanCheck) throws SQLException {
        DbUtils.getQueryRunner().update("delete from bean_check where id = ?", beanCheck.getId());
    }

    public static void deleteByTreeNodeId(int treeNodeId) throws SQLException {
        DbUtils.getQueryRunner().update("delete from bean_check where tree_node_id = ?", treeNodeId);
    }

    public static void update(BeanCheck beanCheck) throws SQLException {
        DbUtils.getQueryRunner().update("update bean_check set check_name= ?, bean_type = ?, other_info=?, columns = ?, content = ? where id = ?",
                beanCheck.getCheckName(), beanCheck.getBeanType().name(), beanCheck.getOtherInfo(), beanCheck.getColumns(), beanCheck.getContent(), beanCheck.getId());
    }
}
