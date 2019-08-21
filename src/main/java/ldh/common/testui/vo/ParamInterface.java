package ldh.common.testui.vo;

import javafx.scene.control.TreeItem;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;

import java.util.Map;

/**
 * Created by ldh on 2018/3/28.
 */
public interface ParamInterface<T extends ParamInterface> {

    boolean check(TreeItem<TreeNode> treeItem, ParamModel paramModel);

    String demo();

    T parse(String data);
}
