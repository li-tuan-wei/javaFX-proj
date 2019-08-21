package ldh.common.testui.util;

import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.TreeItem;
import ldh.common.testui.assist.convert.ConvertFactory;
import ldh.common.testui.assist.template.beetl.BeetlFactory;
import ldh.common.testui.constant.BeanType;
import ldh.common.testui.constant.BeanVarType;
import ldh.common.testui.constant.InstanceClassType;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.controller.MainAppController;
import ldh.common.testui.dao.*;
import ldh.common.testui.model.*;
import ldh.common.testui.vo.MethodData;
import ldh.common.testui.vo.SqlCheck;
import ldh.common.testui.vo.SqlCheckData;
import ldh.common.testui.vo.SqlColumnData;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by ldh on 2018/3/27.
 */
public class VarUtil {

    private final static Logger LOGGER = Logger.getLogger(VarUtil.class.getSimpleName());

    public static boolean isVar(String data) {
        return data.startsWith("${") && data.endsWith("}");
    }

    public static String getVarName(String data) {
        data = data.trim();
        if (data.startsWith("${")) {
            return data.substring(2, data.length() - 1).trim();
        }
        return data;
    }

    public static Object[] vars(String args, Map<String, Object> paramMap) throws Exception {
        String argas[] = args.split(",");
        Object[] argss = new Object[argas.length];
        int i=0;
        for (String arg : argas) {
            String name = getVarName(arg);
            Object pm = paramMap.get(name);

            if (pm == null) {
                String prefixName = name;
                int idx = name.indexOf(".");
                if (idx > 0) {
                    prefixName = name.substring(0, name.indexOf("."));
                }
                Map<String, BeanVar> beanVarMap = (Map<String, BeanVar>) paramMap.get("-beanVar-");
                if (beanVarMap.containsKey(prefixName)) {
                    BeanVar beanVar = beanVarMap.get(prefixName);
                    Object value = valueBeanVar(beanVar, paramMap);
                    paramMap.put(beanVar.getName(), value);
                } else {
                    throw new RuntimeException("变量名称不存在");
                }
                argss[i++] = BeetlFactory.getInstance().process(arg, paramMap);
            } else {
                argss[i++] = pm;
            }
        }
        return argss;
    }

    public static String var(Map<String, Object> paramMap, String arg) throws Exception {
        if (arg == null) return null;
        return BeetlFactory.getInstance().process(arg, paramMap);
    }

    public static Map<String, Object> var(Map<String, Object> paramMap, Map<String, Object> httpParamMap) throws Exception {
        Map<String, Object> httpMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : httpParamMap.entrySet()) {
            String key = entry.getKey();
            String value = BeetlFactory.getInstance().process(entry.getValue().toString(), paramMap);
            httpMap.put(key, value);
        }
        return httpMap;
    }

    public static String replaceLine(String str) {
        return str.replaceAll("(\\r\\n|\\r|\\n|\\n\\r)", " ");
    }

    public static void main(String[] args) {
        String var = "${abs}";
        if (isVar(var)) {
            String name = getVarName(var);
            System.out.println("name:" + name);
        }
    }

    public static Object valueBeanVar(BeanVar beanVar, Map<String, Object> paramMap) throws Exception {
        if (beanVar.getType() == BeanVarType.Sql) {
            Map<String, SqlColumnData> valueMap = BeanVarDao.getSqlDataForSql(beanVar, paramMap);
            if (valueMap.size() > 1) {
                Map<String, Object> valuesMap = new HashMap();
                for (Map.Entry<String, SqlColumnData> entry : valueMap.entrySet()) {
                    Object v = ConvertFactory.getInstance().get(Class.forName(beanVar.getClassName()).getSimpleName()).parse(entry.getValue().getValue().toString());
                    paramMap.put(beanVar.getName(), v);
                    LOGGER.info("VAR PARAM:" + beanVar.getName() + "." + entry.getKey() + ":" + v);
                }
//                paramMap.put(beanVar.getName(), valuesMap);
                return valueMap;
            } else {
                Object value = valueMap.get(valueMap.keySet().iterator().next()).getValue();
                Object v = ConvertFactory.getInstance().get(Class.forName(beanVar.getClassName()).getSimpleName()).parse(value.toString());
//                paramMap.put(beanVar.getName(), v);
                LOGGER.info("VAR PARAM:" + beanVar.getName() + ":" + paramMap.get(beanVar.getName()));
                return v;
            }
        } else if (beanVar.getType() == BeanVarType.Method) {
            if (beanVar.getInstanceClassType() == InstanceClassType.Reflect) {
                Object obj = BeanVarUtil.createBean(beanVar.getClassName());
                Method method = beanVar.getMethod();
                Object[] args = vars(beanVar.getArgs(), paramMap);
                return MethodUtil.invoke(obj, method, args);
            } else if (beanVar.getInstanceClassType() == InstanceClassType.Spring) {
                Object obj = SpringInitFactory.getInstance().getBean(Class.forName(beanVar.getClassName()));
                Method method = beanVar.getMethod();
                Object[] args = vars(beanVar.getArgs(), paramMap);
                return MethodUtil.invoke(obj, method, args);
            } else {
                throw new RuntimeException("不支持这种类型:" + beanVar.getInstanceClassType());
            }
        } else {
            throw new RuntimeException("不支持这种类型:" + beanVar.getType());
        }
    }

    public static Set<String> getElVarNames(String elText) {
        String ext = "[a-zA-Z_$][a-zA-Z0-9_$]*";
        Pattern pattern = Pattern.compile(ext);
        elText = removeParam(elText);
        Matcher matcher = pattern.matcher(elText);
        Set<String> varNameSet = new HashSet();
        while(matcher.find()) {
            String code = matcher.group(0);
            varNameSet.add(code);
        }
        Set<String> methodName = BeetlFactory.getInstance().getCommonMethodNames();
        varNameSet.removeAll(methodName);
        varNameSet.remove("$");
        return varNameSet;
    }

    public static String removeParam(String text) {
        int idx1 = text.indexOf("'");
        if (idx1 >= 0) {
            int idx2 = text.indexOf("'", idx1+1);
            if (idx2 >= 0) {
                String t = text.substring(0, idx1) + text.substring(idx2+1);
                return removeParam(t);
            }
        }
        return text;
    }

    public static List<String> getElExpressions(String elText) { // el表达式
        List<String> values = new ArrayList<>();
        int startIdx = elText.indexOf("${");
        if (startIdx < 0){
            return values;
        }

        while (startIdx >= 0) {
            int end = elText.indexOf("}", startIdx);
            String str = elText.substring(startIdx+2, end);
            values.add(str);
            startIdx = elText.indexOf("${", end);
        }
        return values;
    }

    public static List<String> notHaveElName(TreeItem<TreeNode> treeItem, List<String> elExpressions, Integer paramModelIndex) {
        List<String> notElNames = new ArrayList<>();
        Set<ParamModel> paramModels = DataUtil.getAllData(treeItem);
        List<BeanVar> beanVars = DataUtil.getAllDataForBeanVars(treeItem);

        Set<String> paramSet = new HashSet();
        Set<String> p1 = paramModels.stream().map(paramModel -> paramModel.getName()).collect(Collectors.toSet());
        paramSet.addAll(p1);

        Set<String> p2 = beanVars.stream().map(beanVar -> beanVar.getName()).collect(Collectors.toSet());
        paramSet.addAll(p2);

        for (String value : elExpressions) {
            Set<String> elVarNameSet = VarUtil.getElVarNames(value);
            for(String elVarName : elVarNameSet) {
                boolean ishave = paramSet.contains(elVarName);
                if (!ishave) {
                    notElNames.add(elVarName);
                }
            }
        }
        return notElNames;
    }

    public static Task buildCheckVarTask(TreeItem<TreeNode> treeItem, List<String> values, int index) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<String> notElNames = VarUtil.notHaveElName(treeItem, values, index);
                if (notElNames.isEmpty()) {
                    Platform.runLater(()->{
                        DialogUtil.alert(String.format("变量名称都正确"), Alert.AlertType.INFORMATION);
                    });
                } else {
                    String str = notElNames.stream().collect(Collectors.joining(","));
                    Platform.runLater(()->{
                        DialogUtil.alert(String.format("%s 变量不存在", str), Alert.AlertType.ERROR);
                    });
                }

                return null;
            }
        };
        return task;
    }

    public static boolean isPutVar(String name) {
        return name != null && name.startsWith("{{") && name.endsWith("}}");
    }

    public static String getPutVarName(String name) {
        return name.substring(2, name.length() - 2).trim();
    }

    public static void putValue(String name, TreeItem<TreeNode> treeItem, Object value) {
        String key = name.substring(2, name.length()-2).trim();
        TreeItem<TreeNode> temp = treeItem;
        while (temp.getValue().getTreeNodeType() != TreeNodeType.Test || temp.getValue().getTreeNodeType() != TreeNodeType.Root) {
            temp = temp.getParent();
        }
        if (temp.getValue().getTreeNodeType() == TreeNodeType.Test) {
//            temp.getValue().getContextMap().put(key, value);
        } else {
            throw new RuntimeException("存储变量失败");
        }
    }


    public static void cacheVar(TreeItem<TreeNode> treeItem) {
        TreeItem<TreeNode> tmp = treeItem;
        while (tmp.getValue().getTreeNodeType() != TreeNodeType.Case) {
            tmp = tmp.getParent();
        }
        MainAppController.loadVarData(tmp);

    }

    public static List<String> checkVarNames(TreeItem<TreeNode> treeItem) throws Exception {
        List<String> result = new ArrayList();
        Set<String> paramSet = new HashSet();
        checkNodeVarNames(treeItem, result, paramSet);
        return result;
    }

    private static void checkNodeVarNames(TreeItem<TreeNode> treeItem, List<String> result, Set<String> paramSet) throws Exception {
        if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Case) {
            List<TreeItem<TreeNode>> paramTreeItems = TreeUtil.getChildren(treeItem.getParent(), TreeNodeType.Param);
            for(TreeItem<TreeNode> treeItem1 : paramTreeItems) {
                List<ParamModel> paramModels = ParamDao.getByTreeNodeId(treeItem1.getValue().getId()); // 验证Root下面的param
                paramModels.forEach(paramModel -> {
                    String tt = checkParamModelValue(treeItem, paramModel, paramSet);
                    paramSet.add(paramModel.getName());
                    if (tt != null && !tt.equals("")) result.add(tt);
                });
            }

            List<TreeItem<TreeNode>> beanVarTreeItems = TreeUtil.getChildren(treeItem, TreeNodeType.BeanVar);
            for(TreeItem<TreeNode> treeItem1 : beanVarTreeItems) {
                List<BeanVar> beanVars = BeanVarDao.getByTreeNodeId(treeItem.getValue().getId());
                beanVars.forEach(beanVar -> paramSet.add(beanVar.getName()));
            }

            List<TreeItem<TreeNode>> paramTreeItems2 = TreeUtil.getChildren(treeItem, TreeNodeType.Param);
            for(TreeItem<TreeNode> treeItem1 : paramTreeItems2) {
                List<ParamModel> paramModels = ParamDao.getByTreeNodeId(treeItem1.getValue().getId()); // 验证Case下面的param
                paramModels.forEach(paramModel -> {
                    String tt = checkParamModelValue(treeItem, paramModel, paramSet);
                    paramSet.add(paramModel.getName());
                    if (tt != null && !tt.equals("")) result.add(tt);
                });
            }
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Test) { // 验证Test下面的param
            List<TreeItem<TreeNode>> paramTreeItems = TreeUtil.getChildren(treeItem, TreeNodeType.Param);
            for(TreeItem<TreeNode> treeItem1 : paramTreeItems) {
                List<ParamModel> paramModels = ParamDao.getByTreeNodeId(treeItem1.getValue().getId()); // 验证Test下面的param
                paramModels.forEach(paramModel -> {
                    String tt = checkParamModelValue(treeItem, paramModel, paramSet);
                    paramSet.add(paramModel.getName());
                    if (tt != null && !tt.equals("")) result.add(tt);
                });
            }

            List<TreeItem<TreeNode>> beanVarTreeItems = TreeUtil.getChildren(treeItem.getParent(), TreeNodeType.BeanVar);
            for(TreeItem<TreeNode> treeItem1 : beanVarTreeItems) {
                List<BeanVar> beanVars = BeanVarDao.getByTreeNodeId(treeItem1.getValue().getId());
                beanVars.forEach(beanVar -> {
                    String tt = checkBeanVarParam(treeItem1, beanVar, paramSet);
                    paramSet.add(beanVar.getName());
                    if (tt != null && !tt.equals("")) result.add(tt);
                });
            }
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Http) {
            List<TestHttp> testHttps = TestHttpDao.getByTreeNodeId(treeItem.getValue().getId());
            for(TestHttp testHttp : testHttps) {
                List<TestHttpParam> testHttpParams = TestHttpParamDao.getByTestHttpId(testHttp.getId());
                List<TestHttpBody> testHttpBodys = TestHttpBodyDao.getByTestHttpId(testHttp.getId());
                String tt = checkHttpData(treeItem, testHttp, testHttpParams, testHttpBodys, paramSet);
                if (tt != null && !tt.equals("")) result.add(tt);
            }
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.SqlCheckData) {
            List<SqlCheck> sqlChecks = SqlCheckDao.getByTreeNodeId(treeItem.getValue().getId());
            for(SqlCheck sqlCheck : sqlChecks) {
                List<SqlCheckData> sqlCheckDatas = SqlCheckDataDao.getSqlCheckData(sqlCheck);
                String tt = checkSqlCheck(treeItem, sqlCheck, sqlCheckDatas, paramSet);
                if (tt != null && !tt.equals("")) result.add(tt);
            }
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Bean) {
            BeanCheck beanCheck = BeanCheckDao.getByTreeNodeId(treeItem.getValue().getId());
            String tt = checkBeanCheck(treeItem, beanCheck, paramSet);
            if (tt != null && !tt.equals("")) result.add(tt);
        } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Method) {
            List<TestMethod> testMethods = TestMethodDao.getByTreeNodeId(treeItem.getValue().getId());
            for (TestMethod testMethod : testMethods) {
                List<TestMethodData> testMethodDatas = TestMethodDataDao.getByTestMethodId(testMethod.getId());
                String tt = checkTestMethod(treeItem, testMethodDatas, paramSet);
                if (tt != null && !tt.equals("")) result.add(tt);
            }

        }
        for(TreeItem<TreeNode> child : treeItem.getChildren()) {
            if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Case && child.getValue().getTreeNodeType() != TreeNodeType.Test) continue;
            Set<String> childParamSet = new HashSet();
            childParamSet.addAll(paramSet);
            checkNodeVarNames(child, result, childParamSet);
        }
    }

    private static String checkTestMethod(TreeItem<TreeNode> treeItem, List<TestMethodData> testMethodDatas, Set<String> paramSet) {
        List<String> result = new ArrayList<>();
        for (TestMethodData testMethodData : testMethodDatas) {
            Map<String, MethodData> dataMap = JsonUtil.toObject(testMethodData.getData(), new TypeToken<Map<String, MethodData>>(){}.getType());
            dataMap.forEach((key, value)->{
                List<String> params = checkVarNameForContext(value.getData(), paramSet);
                result.addAll(params);
            });
        }
        String line = result.stream().collect(Collectors.joining(","));
        return StringUtils.isEmpty(line) ? null : treeItem.getValue().getName() + "-testMethod:" + line + ";";
    }

    private static String checkBeanCheck(TreeItem<TreeNode> treeItem, BeanCheck beanCheck, Set<String> paramSet) {
        List<String> result = new ArrayList<>();
        if (beanCheck.getBeanType() == BeanType.Json) {
            Set<BeanData> beanDatas = beanCheck.getBeanDatas();
            for (BeanData beanData : beanDatas) {
                List<String> params = checkVarNameForContext(beanData.getExceptedValue(), paramSet);
                result.addAll(params);
            }
        } else if (beanCheck.getBeanType() == BeanType.String) {
            String str = beanCheck.getContent();
            List<String> params = checkVarNameForContext(str, paramSet);
            result.addAll(params);
        } else if (beanCheck.getBeanType() == BeanType.Object) {
            List<Map<String, BeanData>> beanDatas = beanCheck.getBeanDatasForObject();
            for (Map<String, BeanData> map : beanDatas) {
                map.forEach((key, value)->{
                    List<String> params = checkVarNameForContext(value.getExceptedValue(), paramSet);
                    result.addAll(params);
                });
            }
        } else if (beanCheck.getBeanType() == BeanType.EL) {
            Set<BeanData> beanDatas = beanCheck.getBeanDatas();
            for (BeanData beanData : beanDatas) {
                List<String> params = checkVarNameForContext(beanData.getCheckName(), paramSet);
                result.addAll(params);

                params = checkVarNameForContext(beanData.getExceptedValue(), paramSet);
                result.addAll(params);
            }
        }

        String line = result.stream().collect(Collectors.joining(","));
        return StringUtils.isEmpty(line) ? null : treeItem.getValue().getName() + "-beanCheck:" + line + ";";
    }

    private static String checkSqlCheck(TreeItem<TreeNode> treeItem, SqlCheck sqlCheck, List<SqlCheckData> sqlCheckDatas, Set<String> paramSet) {
        List<String> result = new ArrayList<>();
        List<String> urlVarNames = checkVarNameForContext(sqlCheck.getArgs(), paramSet);
        result.addAll(urlVarNames);
        for (SqlCheckData sqlCheckData : sqlCheckDatas) {
            Map<String, SqlColumnData> map = sqlCheckData.toSqlColumnDataMap();
            for(Map.Entry<String, SqlColumnData> entry : map.entrySet()) {
                List<String> params = checkVarNameForContext(entry.getValue().getExpectValue(), paramSet);
                result.addAll(params);
            }
        }

        String line = result.stream().collect(Collectors.joining(","));
        return StringUtils.isEmpty(line) ? null : treeItem.getValue().getName() + "-sqlCheck:" + line + ";";
    }

    private static String checkHttpData(TreeItem<TreeNode> treeItem, TestHttp testHttp, List<TestHttpParam> testHttpParams, List<TestHttpBody> testHttpBodys, Set<String> paramSet) {
        List<String> result = new ArrayList<>();
        List<String> urlVarNames = checkVarNameForContext(testHttp.getUrl(), paramSet);
        result.addAll(urlVarNames);
        for (TestHttpParam testHttpParam : testHttpParams) {
            List<String> params = checkVarNameForContext(testHttpParam.getContent(), paramSet);
            result.addAll(params);
        }
        for (TestHttpBody body : testHttpBodys) {
            List<String> params = checkVarNameForContext(body.getBody(), paramSet);
            result.addAll(params);
        }
        String line = result.stream().collect(Collectors.joining(","));
        return StringUtils.isEmpty(line) ? null : treeItem.getValue().getName() + "-http:" + line + ";";
    }

    private static List<String> checkVarNameForContext(String context, Set<String> paramSet) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isEmpty(context)) return result;
        if (VarUtil.isPutVar(context)) {
            paramSet.add(VarUtil.getPutVarName(context));
            return result;
        }
        List<String> urlVarNames = getElExpressions(context);
        for(String urlVarName : urlVarNames) {
            Set<String> uvns = getElVarNames(urlVarName);
            Set<String> tts = uvns.stream().filter(uvn->!paramSet.contains(uvn)).collect(Collectors.toSet());
            result.addAll(tts);
        }
        return result;
    }

    private static String checkParamModelValue(TreeItem<TreeNode> treeItem, ParamModel paramModel, Set<String> paramSet) {
        String value = paramModel.getValue();
        List<String> list = checkVarNameForContext(value, paramSet);
        String line = list.stream().collect(Collectors.joining(","));
        return StringUtils.isEmpty(line) ? null : treeItem.getValue().getName() + "-param:" + line + ";";
    }

    private static String checkBeanVarParam(TreeItem<TreeNode> treeItem, BeanVar beanVar, Set<String> paramSet) {
        List<String> result = new ArrayList<>();
        String args = beanVar.getArgs();
        if (StringUtils.isEmpty(args)) return null;
        String argas[] = args.split(",");
        int i=0;
        for (String arg : argas) {
            String name = getVarName(arg);
            if (!paramSet.contains(name)) result.add(name);
        }
        String line = result.stream().collect(Collectors.joining(","));
        return StringUtils.isEmpty(line) ? null : treeItem.getValue().getName() + "-beanVar:" + line + ";";
    }
}
