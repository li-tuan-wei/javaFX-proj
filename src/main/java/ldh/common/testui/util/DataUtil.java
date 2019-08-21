package ldh.common.testui.util;

import javafx.scene.control.TreeItem;
import ldh.common.testui.assist.template.beetl.BeetlFactory;
import ldh.common.testui.constant.ParamCategory;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.controller.BeanVarController;
import ldh.common.testui.dao.BeanVarDao;
import ldh.common.testui.dao.ParamDao;
import ldh.common.testui.model.BeanVar;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by ldh on 2018/3/19.
 */
public class DataUtil {

//    private static Map<Integer, List<ParamModel>> paramMap = new HashMap<>();
//    private static Map<Integer, List<BeanVar>> beanVarMap = new HashMap<>();

    public static synchronized Set<ParamModel> getAllData(TreeItem<TreeNode> treeItem) {
        Set<ParamModel> allParamModel = new HashSet<>();
        try {
            loadParent(treeItem, allParamModel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return allParamModel;
    }

    public static synchronized List<ParamModel> getData(int treeNodeId) {
//        if (paramMap.containsKey(treeNodeId)) {
//            return paramMap.get(treeNodeId);
//        }
        List<ParamModel> result = reLoad(treeNodeId);
        return result;
    }

    public static synchronized List<ParamModel> reLoad(int treeNodeId) {
        List<ParamModel> result = new ArrayList<>();
        try {
            result = ParamDao.getByTreeNodeId(treeNodeId);
            int i = 0;
            for (ParamModel p : result) {
                if (p.getIndex() == null || p.getIndex() == 0) {
                    p.setIndex(++i);
                }
            }
//            paramMap.put(treeNodeId, result);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static ParamModel getById(Integer id) {
        return getById(id, "");
    }

    public static ParamModel getById(Integer id, String name) {
        if (id == null || id == -1) return BeanVarController.buildLangParamModel(name);
        ParamModel pm = null;
//        for(Map.Entry<Integer, List<ParamModel>> entry : paramMap.entrySet()) {
//            for (ParamModel paramModel : entry.getValue()) {
//                if (paramModel.getId().equals(id)) {
//                    pm = paramModel;
//                    break;
//                }
//            }
//        }
        if (pm == null) {
            try {
                pm = ParamDao.getById(id);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return pm;
    }

    private static void loadParent(TreeItem<TreeNode> parent, Set<ParamModel> allParamModel) {
        if (parent.getValue().getTreeNodeType() == TreeNodeType.Root) return;
        List<ParamModel> result = null; //paramMap.get(parent.getValue().getId());
        result = result == null ? new ArrayList<>() : result;
        if (parent.getValue().getTreeNodeType() == TreeNodeType.Param) {
            result = reLoad(parent.getValue().getId());
        } else {
            List<TreeItem<TreeNode>> children = parent.getChildren();
            List<TreeItem<TreeNode>> paramList = children.stream().filter(treeItem -> treeItem.getValue().getTreeNodeType() == TreeNodeType.Param).collect(Collectors.toList());
            for (TreeItem<TreeNode> paramTreeItem : paramList) {
                List<ParamModel> temp = reLoad(paramTreeItem.getValue().getId());
                result.addAll(temp);
            }
        }
        if (result != null && result.size() > 0) {
            for(ParamModel pm : result) {
                if (!allParamModel.contains(pm)) {
                    allParamModel.add(pm);
                }
            }
        }

        loadParent(parent.getParent(), allParamModel);
    }

    public static List<ParamModel> getAllParamModels(TreeItem<TreeNode> treeItem, ParamCategory paramCategory) {
        List<ParamModel> result = new ArrayList();
        TreeItem<TreeNode> temp = treeItem;
        while(temp.getValue().getTreeNodeType() != TreeNodeType.Root) {
            TreeItem<TreeNode> paramTreeItem = getParamTreeItem(temp, TreeNodeType.Param);
            if (paramTreeItem != null) {
                List<ParamModel> paramModels = DataUtil.getData(paramTreeItem.getValue().getId());
                List<ParamModel> dd = paramModels.stream().filter(p->p.getParamCategory() == paramCategory).collect(Collectors.toList());
                result.addAll(dd);
            }
            temp = temp.getParent();
        }
        return result;
    }

    public static ParamModel getParamModel(TreeItem<TreeNode> treeItem, String name) {
        TreeItem<TreeNode> temp = treeItem;
        while (temp.getValue().getTreeNodeType() != TreeNodeType.Root) {
            TreeItem<TreeNode> paramTreeItem = getParamTreeItem(temp, TreeNodeType.Param);
            if (paramTreeItem != null) {
                List<ParamModel> paramModels = DataUtil.getData(paramTreeItem.getValue().getId());
                List<ParamModel> dd = paramModels.stream().filter(p -> p.getName().equals(name)).collect(Collectors.toList());
                if (dd.size() > 0) return dd.get(0);
            }
            temp = temp.getParent();
        }
        return null;
    }

        private static TreeItem<TreeNode> getParamTreeItem(TreeItem<TreeNode> treeItem, TreeNodeType type) {
        for (TreeItem<TreeNode> treeNodeTreeItem : treeItem.getChildren()) {
            if(treeNodeTreeItem.getValue().getTreeNodeType() == type) {
                return treeNodeTreeItem;
            }
        }
        return null;
    }

    public static List<BeanVar> getAllDataForBeanVars(TreeItem<TreeNode> treeNode) {
        List<BeanVar> result = new ArrayList<>();
        TreeItem<TreeNode> temp = treeNode;
        while(temp.getValue().getTreeNodeType() != TreeNodeType.BeanVar && temp.getValue().getTreeNodeType() != TreeNodeType.Root) {
            boolean isEnd = false;
            for (TreeItem<TreeNode> tt : temp.getChildren()) {
              if (tt.getValue().getTreeNodeType() == TreeNodeType.BeanVar) {
                  temp = tt;
                  isEnd = true;
                  break;
              }
            }
            if (isEnd) break;
            temp = temp.getParent();
        }
        if (temp.getValue().getTreeNodeType() == TreeNodeType.BeanVar) {
            try {
                List<BeanVar> resultData = BeanVarDao.getByTreeNodeId(temp.getValue().getId());
//                beanVarMap.putIfAbsent(temp.getValue().getId(), resultData);
                return resultData;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static void removeBeanVar(TreeItem<TreeNode> treeNode, Integer id) {
        List<BeanVar> result = getAllDataForBeanVars(treeNode);
        List<BeanVar> remove = result.stream().filter(bv -> bv.getId().equals(id)).collect(Collectors.toList());
        result.removeAll(remove);
    }

    public static Object getVar(TreeItem<TreeNode> treeItem, String arg) throws Exception {
        String var = arg.substring(2, arg.length() -1).trim();
        TreeItem<TreeNode> tmp = treeItem;

        return null;
    }
}
