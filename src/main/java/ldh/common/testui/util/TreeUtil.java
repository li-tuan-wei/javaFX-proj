package ldh.common.testui.util;

import javafx.scene.control.TreeItem;
import ldh.common.testui.assist.convert.Convert;
import ldh.common.testui.assist.convert.ConvertFactory;
import ldh.common.testui.assist.template.beetl.BeetlFactory;
import ldh.common.testui.assist.template.freemarker.FreeMarkerFactory;
import ldh.common.testui.constant.ParamCategory;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.dao.BeanVarDao;
import ldh.common.testui.model.BeanVar;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;

import java.util.*;
import java.util.stream.Collectors;

public class TreeUtil {

    public static List<TreeNode> tree(List<TreeNode> treeNodeList) {
        List<TreeNode> treeNodes = new ArrayList<>();
        Collections.sort(treeNodeList, new Comparator<TreeNode>() {
            @Override
            public int compare(TreeNode o1, TreeNode o2) {
                return o1.getParentId().compareTo(o2.getParentId());
            }
        });
        Map<Integer, TreeNode> projectMap = new HashMap<Integer, TreeNode>();
        for (TreeNode treeNode : treeNodeList) {
            if (!projectMap.containsKey(treeNode.getId())) {
                treeNode.setChildren(new ArrayList<TreeNode>());
                projectMap.put(treeNode.getId(), treeNode);
            }
            if (projectMap.containsKey(treeNode.getParentId())) {
                TreeNode treeNode1 = projectMap.get(treeNode.getParentId());
                treeNode1.getChildren().add(treeNode);
            }
        }
        for (TreeNode treeNode : projectMap.values()) {
            if (treeNode.getParentId() == 0) {
                treeNodes.add(treeNode);
            }
            Collections.sort(treeNode.getChildren(), (o1, o2)->o1.getIndex().compareTo(o2.getIndex()));
        }
        return treeNodes;
    }

    public static boolean hasTreeNodeType(TreeItem<TreeNode> treeItem, TreeNodeType treeNodeType) {
        return getChildren(treeItem, treeNodeType).size() > 0;
    }

    public static Map<String, Object> buildParamMap(List<TreeItem<TreeNode>> treeItems) throws Exception {
        Map<String, Object> paramMap = new HashMap<>();
        for (TreeItem<TreeNode> treeItem : treeItems) {
            List<ParamModel> paramModelList = DataUtil.reLoad(treeItem.getValue().getId());
            for (ParamModel pm : paramModelList) {
                String key = pm.getName();
                Object value = BeetlFactory.getInstance().process(pm.getValue(), paramMap);
                if (pm.getParamCategory() == ParamCategory.Constant) {
                    value = ConvertFactory.getInstance().get(Class.forName(pm.getClassName()).getSimpleName()).parse(value.toString());
                }
                paramMap.put(key, value);
            }
            buildVarParamMap(treeItem);
        }
        return paramMap;
    }

    public static Map<String, Object> buildParamMap(TreeItem<TreeNode> treeItem) throws Exception {
        Map<String, Object> paramMap = new HashMap<>();
        if (treeItem.getValue().getTreeNodeType() != TreeNodeType.Node) {
            buildParamMap(treeItem.getParent(), paramMap);
        }
        List<ParamModel> paramModelList = DataUtil.reLoad(treeItem.getValue().getId());
        for (ParamModel pm : paramModelList) {
            String key = pm.getName();
            Object value = BeetlFactory.getInstance().process(pm.getValue(), paramMap);
            if (pm.getParamCategory() == ParamCategory.Constant) {
                value = ConvertFactory.getInstance().get(Class.forName(pm.getClassName()).getSimpleName()).parse(value.toString());
            }
            paramMap.put(key, value);
        }
        Map<String, BeanVar> beanVarMap = buildVarParamMap(treeItem);
        paramMap.put("-beanVar-", beanVarMap);
        return paramMap;
    }

    public static Map<String, Object> getParamMap(TreeItem<TreeNode> treeItem, Map<String, Object> paramMap) throws Exception {
        Map<String, Object> result = new HashMap();
        for (TreeItem<TreeNode> child : treeItem.getChildren()) {
            if (child.getValue().getTreeNodeType() == TreeNodeType.Param) {
                List<ParamModel> paramModelList = DataUtil.reLoad(child.getValue().getId());
                for (ParamModel pm : paramModelList) {
                    try {
                        String key = pm.getName();
                        Map<String, Object> tempMap = new HashMap();
                        tempMap.putAll(paramMap);
                        tempMap.putAll(result);
                        Object value = BeetlFactory.getInstance().process(pm.getValue(), tempMap);
                        if (pm.getParamCategory() == ParamCategory.Constant) {
                            value = ConvertFactory.getInstance().get(Class.forName(pm.getClassName()).getSimpleName()).parse(value.toString());
                        }
                        result.put(key, value);
                    } catch (Exception e) {
                        System.out.println("error:" + pm.getValue());
                        throw new RuntimeException(e);
                    }

                }
            }
        }
        return result;
    }

    public static Map<String, BeanVar> buildVarParamMap(TreeItem<TreeNode> treeItem) throws Exception {
        Map<String, BeanVar> paramMap = new HashMap<>();
        if (treeItem.getValue().getTreeNodeType() != TreeNodeType.Case) {
            return buildVarParamMap(treeItem.getParent());
        }
        for (TreeItem<TreeNode> child : treeItem.getChildren()) {
            if (child.getValue().getTreeNodeType() == TreeNodeType.BeanVar) {
                List<BeanVar> beanVars = BeanVarDao.getByTreeNodeId(child.getValue().getId());
                beanVars.stream().forEach(beanVar -> {
                    paramMap.put(beanVar.getName(), beanVar);
                });
            }
        }
        return paramMap;
    }

    private static void buildParamMap(TreeItem<TreeNode> treeItem, Map<String, Object> paramMap) throws Exception {
        if (treeItem.getValue().getTreeNodeType() != TreeNodeType.Root) {
            buildParamMap(treeItem.getParent(), paramMap);
        }
        for (TreeItem<TreeNode> child : treeItem.getChildren()) {
            if (child.getValue().getTreeNodeType() == TreeNodeType.Param) {
                List<ParamModel> paramModelList = DataUtil.reLoad(child.getValue().getId());
                for (ParamModel pm : paramModelList) {
                    String key = pm.getName();
                    Object value = BeetlFactory.getInstance().process(pm.getValue(), paramMap);
                    if (pm.getParamCategory() == ParamCategory.Constant) {
                        Convert convert = ConvertFactory.getInstance().get(Class.forName(pm.getClassName()).getSimpleName());
                        Object v = convert.parse(value.toString());
                        paramMap.put(key, v);
                        continue;
                    }
                    paramMap.put(key, value);
                }
            }
        }
    }

    public static List<TreeItem<TreeNode>> getChildren(TreeItem<TreeNode> parent, TreeNodeType treeNodeType) {
        return parent.getChildren().stream().filter(treeItem -> treeItem.getValue().getTreeNodeType() == treeNodeType).collect(Collectors.toList());
    }


}
