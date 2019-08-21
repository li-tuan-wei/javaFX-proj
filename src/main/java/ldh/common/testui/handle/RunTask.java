package ldh.common.testui.handle;

import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import ldh.common.testui.model.TreeNode;

/**
 * Created by ldh on 2018/3/20.
 */
public class RunTask extends Task<Void> {

    private TreeItem<TreeNode> treeItem;

    public RunTask(TreeItem<TreeNode> treeItem) {
        this.treeItem = treeItem;
    }

    @Override
    protected Void call() throws Exception {
        initData();
        invoke();
        checkResult();
        checkData();
        return null;
    }

    protected void checkData() {
    }

    protected void checkResult() {
    }

    protected void invoke() {
    }

    protected void initData() {
    }
}
