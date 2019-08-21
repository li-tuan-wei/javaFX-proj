package ldh.common.testui.util;

import javafx.scene.control.TreeItem;
import ldh.common.testui.handle.RunTreeItem;
import ldh.common.testui.model.BeanVar;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by ldh on 2018/4/3.
 */
public class BeanVarUtil {

//    public static Object createBean(TreeItem<TreeNode> treeItem, BeanVar beanVar) throws Exception {
//        Set<ParamModel> allParamModel = DataUtil.getAllData(treeItem);
//        List<String> argsList = beanVar.getArgList();
//        List<Object> argsObject = new ArrayList<>(argsList.size());
//        int size = argsList.size();
//        int idx = 0;
//        for (ParamModel pm : allParamModel) {
//            for (String arg : argsList) {
//                if (VarUtil.isVar(arg)) {
//                    String var = VarUtil.getVarName(arg);
//                    if (pm.getName().equals(var)) {
//                        argsObject.add(RunTreeItem.buildValue(pm));
//                        idx++;
//                        if (idx == size) break;
//                    }
//                }
//            }
//        }
//        if (idx != size) {
//            List<BeanVar> allBeanVar = DataUtil.getAllDataForBeanVars(treeItem);
//            for (BeanVar beanVar1 : allBeanVar) {
//                for (String arg : argsList) {
//                    if (VarUtil.isVar(arg)) {
//                        String var = VarUtil.getVarName(arg);
//                        if (beanVar1.getName().equals(var)) {
//                            argsObject.add(RunTreeItem.buildValue(beanVar1));
//                            idx++;
//                            if (idx == size) break;
//                        }
//                    }
//                }
//            }
//        }
//        return createBean(beanVar, argsObject.toArray());
//    }

    private static Object createBean(BeanVar beanVar, Object[] args) {
        return null;
    }

    public static Object createBean(String className) throws Exception {
        Class clazz = Class.forName(className);
        return clazz.newInstance();
    }
}
