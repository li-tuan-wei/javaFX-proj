package ldh.common.testui.dao;

import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;

public class TreeDao {

    public static void save(TreeNode treeNode) throws SQLException {
        if (treeNode.getId() != null && treeNode.getId() != 0) {
            update(treeNode);
        } else {
            insert(treeNode);
        }
    }

    public static void insert(TreeNode treeNode) throws SQLException {
        ResultSetHandler<Integer> h = new ScalarHandler();
        String insertSql = "insert into tree(name, desc, type, parent_id, index, path, enable) values(?, ?, ?, ?, ?, ?, ?)";
        int id = DbUtils.getQueryRunner().insert(insertSql, h, treeNode.getName(), treeNode.getDesc(), treeNode.getTreeNodeType().name(), treeNode.getParentId(), treeNode.getIndex(), treeNode.getPath(), treeNode.getEnable());
        treeNode.setId(id);
    }

    public static List<TreeNode> getAll() throws SQLException {
        String sql = "select id, name, desc, type as treeNodeType, parent_id as parentId, index, path, enable from tree";
        ResultSetHandler<List<TreeNode>> beanHandler = new BeanListHandler<TreeNode>(TreeNode.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler);
    }

    public static TreeNode getById(Integer id) throws SQLException {
        String sql = "select id, name, desc, type as treeNodeType, parent_id as parentId, index, path, enable from tree where id = ?";
        ResultSetHandler<TreeNode> beanHandler = new BeanHandler<TreeNode>(TreeNode.class);
        return DbUtils.getQueryRunner().query(sql, beanHandler, id);
    }

    public static void delete(TreeNode treeNode) throws SQLException {
        deleteTreeNode(treeNode);
        deleteChildren(treeNode.getChildren());
        DbUtils.getQueryRunner().update("delete from tree where id = ?", treeNode.getId());
    }

    public static void update(TreeNode treeNode) throws SQLException {
        int n = DbUtils.getQueryRunner().update("update tree set name = ?, desc = ?, type = ?, index = ?, path = ?, enable = ? where id = ?", treeNode.getName(),
                treeNode.getDesc(), treeNode.getTreeNodeType().name(), treeNode.getIndex(), treeNode.getPath(), treeNode.getEnable(), treeNode.getId());
        if (n != 1) {
            throw new RuntimeException("更新失败");
        }
    }

    public static void deleteChildren(List<TreeNode> children) throws SQLException {
        if (children == null) return;
        for (TreeNode treeNode1 : children) {
            deleteTreeNode(treeNode1);
            deleteChildren(treeNode1.getChildren());
            DbUtils.getQueryRunner().update("delete from tree where id = ?", treeNode1.getId());
        }
    }

    private static void deleteTreeNode(TreeNode treeNode) throws SQLException {
        if (treeNode.getTreeNodeType() == TreeNodeType.Param) {
            ParamDao.delete(treeNode.getId());
        } else if (treeNode.getTreeNodeType() == TreeNodeType.Method) {
            TestMethodDao.deleteByTreeNodeId(treeNode.getId());
        } else if (treeNode.getTreeNodeType() == TreeNodeType.SqlCheckData) {
            SqlCheckDao.deleteByTreeNodeId(treeNode.getId());
        } else if (treeNode.getTreeNodeType() == TreeNodeType.BeanVar) {
            BeanVarDao.deleteByTreeNodeId(treeNode.getId());
        } else if (treeNode.getTreeNodeType() == TreeNodeType.Http) {
            TestHttpDao.deleteByTreeNodeId(treeNode.getId());
        } else if (treeNode.getTreeNodeType() == TreeNodeType.Bean) {
            BeanCheckDao.deleteByTreeNodeId(treeNode.getId());
        } else if (treeNode.getTreeNodeType() == TreeNodeType.Data) {
            SqlDataDao.deleteByTreeNodeId(treeNode.getId());
        } else if (treeNode.getTreeNodeType() == TreeNodeType.Data2) {
            SqlDataDao.deleteByTreeNodeId(treeNode.getId());
        } else if (treeNode.getTreeNodeType() == TreeNodeType.ExportData) {
            DataExportDao.deleteByTreeNodeId(treeNode.getId());
        } else if (treeNode.getTreeNodeType() == TreeNodeType.Function) {
            CommonFunDao.deleteByTreeNodeId(treeNode.getId());
        }
    }
}
