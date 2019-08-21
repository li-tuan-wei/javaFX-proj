package db;

import ldh.common.testui.TestUIMainApp;
import ldh.common.testui.dao.BeanVarDao;
import ldh.common.testui.dao.ParamDao;
import ldh.common.testui.dao.TreeDao;
import ldh.common.testui.model.BeanVar;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.JsonUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by ldh123 on 2017/6/7.
 */
@Ignore
public class TreeDaoTest {

    @Before
    public void before() throws Exception {
        TestUIMainApp.startDb(null);
    }

    @Test
    public void test() throws Exception {
//        TreeNode treeNode = new TreeNode();
//        treeNode.setName("test001");
//        treeNode.setParentId(0);
//        TreeDao.save(treeNode);
//        Assert.assertNotNull(treeNode.getId());

        List<TreeNode> treeNodeList = TreeDao.getAll();
        for(TreeNode treeNode1 : treeNodeList) {
            listTreeNode(treeNode1);
        }
        Assert.assertTrue(treeNodeList.size() > 0);
    }

    private void listTreeNode(TreeNode treeNode) throws SQLException {
        List<ParamModel> params = ParamDao.getByTreeNodeId(treeNode.getId());
        int n = 0;
        for(ParamModel pm : params) {
            if (pm.getIndex() == 0) {
                pm.setIndex(++n);
//                ParamDao.update(pm);
            }
        }
    }

    @Test
    public void query() throws SQLException {
        List<TreeNode> treeNodeList = TreeDao.getAll();
        Assert.assertTrue(treeNodeList.size() > 0);
    }

    @Test
    public void bigDecimail() throws SQLException {
        System.out.println(Number.class.isAssignableFrom(BigDecimal.class));

        List<ParamModel> params = ParamDao.getByTreeNodeId(7);
        params.forEach(pm -> System.out.println(pm.getIndex()));
    }

    @Test
    public void beanVar() throws SQLException {
        List<BeanVar> beanVars= BeanVarDao.getByTreeNodeId(288);
        System.out.println(JsonUtil.toJson(beanVars));
    }

}
