package util;

import javafx.scene.control.TreeItem;
import ldh.common.testui.TestUIMainApp;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.handle.RunTreeItem;
import ldh.common.testui.model.TestLog;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.DbUtils;
import ldh.common.testui.util.H2Util;
import org.apache.commons.dbutils.QueryRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by ldh on 2019/1/4.
 */
@Ignore
public class RunTreeItemTest {

    @Before
    public void before() throws Exception {
        TestUIMainApp.startDb(null);
    }

    @Test
    public void exportData() {
        QueryRunner queryRunner = DbUtils.getQueryRunner();
        H2Util.exportAllTable(queryRunner, "F:\\other\\data","data.sql");
    }

    @Test
    public void testBean() throws Exception {
        TreeNode treeNode = new TreeNode();
        treeNode.setTreeNodeType(TreeNodeType.Bean);
        treeNode.setIndex(1);
        treeNode.setId(316);
        TreeItem<TreeNode> treeItem = new TreeItem<TreeNode>(treeNode);

        TestLog testLog = TestLog.buildTestLog("demo", treeNode.getTreeNodeType().name());
        Map<String,Object> paramMap = new HashMap();
        paramMap.put("result", "{\n" +
                "  \"result\": 1,\n" +
                "  \"reason\": null,\n" +
                "  \"content\": \"{\\\"assetId\\\":1,\\\"transactionId\\\":\\\"autotest_20190104152424\\\",\\\"vendor\\\":\\\"BBNet\\\",\\\"channelId\\\":56,\\\"projectStatus\\\":\\\"Initialized\\\"}\",\n" +
                "  \"success\": true\n" +
                "}");
        RunTreeItem.runBeanCheck(treeItem, testLog, paramMap, null, true);
    }
}
