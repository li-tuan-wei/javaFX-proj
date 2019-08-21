package ldh.common.testui.controller;

import javafx.scene.control.TreeItem;
import ldh.common.testui.constant.ParamCategory;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.DataUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by ldh on 2018/3/21.
 */
public class BaseController {

    protected TreeItem<TreeNode> treeItem;

    public void setTreeItem(TreeItem<TreeNode> treeItem) {
        this.treeItem = treeItem;
    }

    protected TreeItem<TreeNode> getParamTreeItem(TreeItem<TreeNode> treeItem) {
        for (TreeItem<TreeNode> treeNodeTreeItem : treeItem.getChildren()) {
            if(treeNodeTreeItem.getValue().getTreeNodeType() == TreeNodeType.Param) {
                return treeNodeTreeItem;
            }
        }
        return null;
    }

    protected Set<ParamModel> getParamModels(ParamCategory category) {
        Set<ParamModel> results = new HashSet<>();
        TreeItem<TreeNode> temp = treeItem;
        while(temp.getValue().getTreeNodeType() != TreeNodeType.Root) {
            TreeItem<TreeNode> paramTreeItem = getParamTreeItem(temp);
            if (paramTreeItem != null) {
                List<ParamModel> paramModels = DataUtil.getData(paramTreeItem.getValue().getId());
                List<ParamModel> dd = paramModels.stream().filter(p->p.getParamCategory() == category).collect(Collectors.toList());
                results.addAll(dd);
            }
            temp = temp.getParent();
        }
        return results;
    }

//    protected  Set<ParamModel> getClassParamModels() {
//        Set<ParamModel> results = new HashSet<>();
//        results.addAll(getParamModels(ParamCategory.Method_Package));
//        results.addAll(getParamModels(ParamCategory.Other_jar));
//        return results;
//    }
}
