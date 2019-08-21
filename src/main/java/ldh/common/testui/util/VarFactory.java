package ldh.common.testui.util;

import javafx.scene.control.TreeItem;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.vo.VarModel;

import java.util.*;
import java.util.stream.Collectors;

public class VarFactory {

    private Map<TreeNode, VarModel> cacheMap = new HashMap<>();
    private Map<TreeNode, Map<String, VarModel>> dataMap = new HashMap<>();

    private static VarFactory instance = null;

    public static VarFactory getInstance() {
        if (instance == null) {
            synchronized (VarFactory.class) {
                if(instance == null) {
                    instance = new VarFactory();
                }
            }
        }
        return instance;
    }

    public void cache(TreeItem<TreeNode> treeItem, VarModel varModel) {
        cacheMap.put(treeItem.getValue(), varModel);
//        TreeItem<TreeNode> tmp = treeItem;
//        while(tmp.getValue().getTreeNodeType() != TreeNodeType.Node) {
//            tmp = tmp.getParent();
//        }
        Map<String, VarModel> map = dataMap.get(treeItem.getValue());
        if (map == null) {
            map = new HashMap<String, VarModel>();
            dataMap.put(treeItem.getValue(), map);
        }
        map.put(varModel.getVarName(), varModel);
    }

    public VarModel getCache(TreeNode treeNode) {
        if (!cacheMap.containsKey(treeNode)) {
            throw new RuntimeException("缓存不存在，key:" + treeNode.getName());
        }
        return cacheMap.get(treeNode);
    }

    public void clean() {
        dataMap.clear();
        cacheMap.clear();
    }

    public boolean isHave(TreeItem<TreeNode> treeItem, String key) {
        if (!cacheMap.containsKey(treeItem.getValue())) {
            TreeItem<TreeNode> tmp = treeItem;
            while(tmp.getValue().getTreeNodeType() != TreeNodeType.Node) {
                tmp = tmp.getParent();
                if (cacheMap.containsKey(tmp.getValue())) {
                    return dataMap.get(tmp.getValue()).containsKey(key);
                }
            }
            return false;
        }
        return dataMap.get(treeItem.getValue()).containsKey(key);
    }

    public VarModel getCache(TreeItem<TreeNode> treeItem, String key) {
        TreeItem<TreeNode> tmp = treeItem;
        while(tmp.getValue().getTreeNodeType() != TreeNodeType.Node) {
            tmp = tmp.getParent();

            if (dataMap.containsKey(tmp.getValue())) {
                Map<String, VarModel> varModelMap = dataMap.get(tmp.getValue());
                return varModelMap.get(key);
            }
        }
        if (!cacheMap.containsKey(treeItem.getValue())) {
            VarUtil.cacheVar(treeItem);
        }
        return null;
    }

    public Set<String> asStringList(TreeItem<TreeNode> treeItem) {
        Set<String> result = new LinkedHashSet<>();
        TreeItem<TreeNode> tmp = treeItem;
        while(tmp.getValue().getTreeNodeType() != TreeNodeType.Node) {
            if(tmp.getValue().getTreeNodeType() == TreeNodeType.Root) break;
            tmp = tmp.getParent();
            Map<String, VarModel>  map = dataMap.get(tmp.getValue());
            if (map != null) {
                List<String> list = map.values().stream().map(varmodel-> "${" + varmodel.getVarName() + "}").collect(Collectors.toList());
                result.addAll(list);
            }
        }
        if (!dataMap.containsKey(tmp.getValue())) {
            VarUtil.cacheVar(treeItem);
        }

        tmp = treeItem;
        while(tmp.getValue().getTreeNodeType() != TreeNodeType.Method) {
            tmp = tmp.getParent();
            if (tmp == null) break;
            Map<String, VarModel>  map = dataMap.get(tmp.getValue());
            if (map != null) {
                List<String> list = map.values().stream().map(varmodel-> "${" + varmodel.getVarName() + "}").collect(Collectors.toList());
                result.addAll(list);
            }
        }
        return result;
    }
}
