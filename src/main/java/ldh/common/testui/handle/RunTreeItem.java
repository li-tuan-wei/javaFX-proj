package ldh.common.testui.handle;

import com.google.gson.reflect.TypeToken;
import com.mysql.cj.x.protobuf.MysqlxDatatypes;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import ldh.common.testui.assist.convert.JsonConvert;
import ldh.common.testui.assist.template.beetl.BeetlFactory;
import ldh.common.testui.assist.template.el.ElFactory;
import ldh.common.testui.constant.*;
import ldh.common.testui.assist.convert.ConvertFactory;
import ldh.common.testui.dao.*;
import ldh.common.testui.model.*;
import ldh.common.testui.util.*;
import ldh.common.testui.vo.*;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by ldh on 2018/3/26.
 */
public class RunTreeItem {

    private static final Logger LOGGER = Logger.getLogger(RunTreeItem.class.getName());

    public static void runTreeItem(TreeItem<TreeNode> treeItem, Consumer<TreeItem<TreeNode>> handler, Consumer<String> consumer, boolean isDebug) {
        TestLog testLog = null;
        try {
            Map<String, Object> paramMap = initRun(treeItem);  // 加载公用方法

            cleanTreeNode(treeItem);
            if (!treeItem.getValue().getEnable()) return;
            if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Case || treeItem.getValue().getTreeNodeType() == TreeNodeType.Test) {
                testLog = TestLog.buildTestLog(treeItem.getParent().getValue().getName() + "-" + treeItem.getValue().getName(), treeItem.getValue().getTreeNodeType().name());
            } else {
                testLog = TestLog.buildTestLog(treeItem.getValue().getName(), treeItem.getValue().getTreeNodeType().name());
            }
            TestLogDao.insert(testLog);
            UiUtil.addTestLog(testLog);
            cache(treeItem.getValue(), paramMap, isDebug, testLog);
            boolean isSuccess = true;

            showTreeItem(handler, treeItem);

            if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Case || treeItem.getValue().getTreeNodeType() == TreeNodeType.Test) {
                if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Test) {
                    LogUtil.log("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                    LogUtil.log("测试:" + treeItem.getValue().getName());
                }

                for(TreeItem<TreeNode> treeItemt : treeItem.getChildren()) {
                    if (!treeItem.getValue().getEnable()) return;
                    if (treeItemt.getValue().getTreeNodeType() == TreeNodeType.Test) {
                        LogUtil.log("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                        LogUtil.log("测试:" + treeItemt.getValue().getName());
                    }
                    Map<String, Object> tempMap = new HashMap();
                    tempMap.putAll(paramMap);
                    boolean success = runTreeItem(treeItemt, tempMap, handler, consumer, testLog, isDebug);
                    isSuccess = success ? isSuccess : false;
                    tempMap = null;
                }

            } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Method || treeItem.getValue().getTreeNodeType() == TreeNodeType.Http) {
                boolean success = runTreeItem(treeItem, paramMap, handler, consumer, testLog, isDebug);
                isSuccess = success ? isSuccess : false;
            }

            testLog.setRunSuccess(isSuccess ? 1 : 3);
            TestLogDao.update(testLog);
            setTreeNodeMark(treeItem, isSuccess ? 1 : 3);

            TestLog tmpLog = testLog;
            Platform.runLater(()->{
                int successNum = tmpLog.getSuccessNum();
                int failureNum = tmpLog.getFailureNum();
                if (successNum >= 0 && failureNum == 0) { // 无失败为成功
                    tmpLog.getSuccess().set(1);
                } else if (failureNum > 0 && successNum != 0 && failureNum >= successNum) {  // 失败个数大于成功个数，为失败
                    tmpLog.getSuccess().set(2);
                } else {
                    tmpLog.getSuccess().set(3);
                }
            });
            paramMap = null;
        } catch (Exception e) {
            if (testLog != null) {
                testLog.setRunSuccess(2);
                try {
                    TestLogDao.update(testLog);
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
            throw new RuntimeException(e);
        }
    }

    public static boolean runTreeItem(TreeItem<TreeNode> treeItem, Map<String, Object> paramMap, Consumer<TreeItem<TreeNode>> handler, Consumer<String> consumer, TestLog testLog, boolean isDebug) {
        try {
            if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Case) {  // 测试场景
                if (!treeItem.getValue().getEnable()) return true;
               return runCase(treeItem, paramMap, handler, consumer, testLog, isDebug);
            } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Test) {  // 测试用例
                if (!treeItem.getValue().getEnable()) return true;
                boolean isSuccess = runTest(treeItem, paramMap, handler, consumer, testLog, isDebug);
                setTreeNodeMark(treeItem, isSuccess ? 1 : 3);
                return isSuccess;
            } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Http) {  // 测试http接口
                if (!treeItem.getValue().getEnable()) return true;
                return runHttp(treeItem, paramMap, handler, consumer, testLog, isDebug);
            } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Method) { // 测试方法
                if (!treeItem.getValue().getEnable()) return true;
                return runMethod(treeItem, paramMap, handler, consumer, testLog, isDebug);
            } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.SqlCheckData) { // 数据库参数验证
                if (!treeItem.getValue().getEnable()) return true;
                return runSqlCheckData(treeItem, testLog, paramMap, handler, isDebug);
            } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Bean) {
                if (!treeItem.getValue().getEnable()) return true;
                return runBeanCheck(treeItem, testLog, paramMap, handler, isDebug);
            } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Data) { // 数据导入
                if (!treeItem.getValue().getEnable()) return true;
                return runSqlData(treeItem, testLog, paramMap, handler, isDebug);
            } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Data2) { // 数据导入
                if (!treeItem.getValue().getEnable()) return true;
                return runSqlData(treeItem, testLog, paramMap, handler, isDebug);
            } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.ExportData) { // 数据导出
                if (!treeItem.getValue().getEnable()) return true;
                return runExportData(treeItem, testLog, paramMap, handler, isDebug);
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean runCase(TreeItem<TreeNode> treeItem, Map<String, Object> paramMap, Consumer<TreeItem<TreeNode>> handler, Consumer<String> consumer, TestLog testLog, boolean isDebug) throws Exception {
        boolean isSuccess = true;
        int i=0;
        double total = treeItem.getChildren().size() * 1.0d;
        for(TreeItem<TreeNode> treeItemt : treeItem.getChildren()) {
            if (treeItemt.getValue().getTreeNodeType() == TreeNodeType.Param) continue;
            Map<String, Object> tempMap = new HashMap();
            tempMap.putAll(paramMap);
            boolean success = runTreeItem(treeItemt, tempMap, handler, consumer, testLog, isDebug);
            isSuccess = success ? isSuccess : false;
            tempMap = null;
            UiUtil.showProgress(++i/total);
            UiUtil.showMessage(String.format("正运行完%s/%s个", i, treeItem.getChildren().size()));
        }
        UiUtil.showMessage(String.format("运行结束"));
        return isSuccess;
    }

    private static boolean runTest(TreeItem<TreeNode> treeItem, Map<String, Object> paramMap, Consumer<TreeItem<TreeNode>> handler, Consumer<String> consumer, TestLog testLog, boolean isDebug) throws Exception {
        boolean isSuccess = true;
        Map<String, Object> childParamMap = TreeUtil.getParamMap(treeItem, paramMap);
        TestLog testLog2 = TestLog.buildTestLog(treeItem.getValue().getName(), treeItem.getValue().getTreeNodeType().name());
        testLog2.setParentId(testLog.getId());
        TestLogDao.insert(testLog2);

        paramMap.putAll(childParamMap);
        cache(treeItem.getValue(), paramMap, isDebug, testLog2);

        showTreeItem(handler, treeItem);

        for(TreeItem<TreeNode> treeItemt : treeItem.getChildren()) {
            if (treeItemt.getValue().getTreeNodeType() == TreeNodeType.Param) continue;
            Map<String, Object> tempMap = new HashMap();
            tempMap.putAll(paramMap);
            boolean success = runTreeItem(treeItemt, tempMap, handler, consumer, testLog2, isDebug);
            isSuccess = success ? isSuccess : false;
            tempMap = null;
        }
        return isSuccess;
    }

    private static boolean runHttp(TreeItem<TreeNode> treeItem, Map<String, Object> paramMap, Consumer<TreeItem<TreeNode>> handler, Consumer<String> consumer, TestLog testLog, boolean isDebug) throws SQLException {
        boolean isSuccess = true;
        showTreeItem(handler, treeItem);
        List<TestHttp> testHttps = TestHttpDao.getByTreeNodeId(treeItem.getValue().getId());
        if (testHttps.size() < 1) return true;
        TestHttp testHttp = testHttps.get(0);

        cache(treeItem.getValue(), paramMap, isDebug, testLog);

        try {
            HttpMethod httpMethod = testHttp.getMethod();
            String result = null;
            List<TestHttpParam> testHttpParams = TestHttpParamDao.getByTestHttpId(testHttp.getId());
            Map<String, Object> headerParamMap = new HashMap();
            Map<String, Object> paramParamMap = new HashMap();
            Map<String, Object> cookieParamMap = new HashMap();
            for (TestHttpParam testHttpParam : testHttpParams) {
                if (testHttpParam.getParamType() == ParamType.Header) {
                    headerParamMap.put(testHttpParam.getName(), testHttpParam.getContent());
                } else if (testHttpParam.getParamType() == ParamType.Param) {
                    if (testHttpParam.getContent() != null && !testHttpParam.getContent().equals("")) {
                        paramParamMap.put(testHttpParam.getName(), testHttpParam.getContent());
                    }
                } else if (testHttpParam.getParamType() == ParamType.Cookie) {
                    cookieParamMap.put(testHttpParam.getName(), testHttpParam.getContent());
                } else {
                    new RuntimeException("not support");
                }
            }
            List<TestHttpBody> testHttpBodys = TestHttpBodyDao.getByTestHttpId(testHttp.getId());
            TestHttpBody httpBody = testHttpBodys.size() > 0 ? testHttpBodys.get(0) : null;
            Map<String, Object> headerParams = VarUtil.var(paramMap, headerParamMap);
            Map<String, Object> httpParams = VarUtil.var(paramMap, paramParamMap);
            Map<String, Object> paramData = new HashMap();
            paramData.put("header", headerParams);
            paramData.put("param", httpParams);
//            paramData.put("var", FreeMarkerFactory.filterParamMap(paramMap));
            paramData.put("var", paramMap);

            Map<String, Object> urlParamMap = new HashMap();
            urlParamMap.putAll(paramMap);
            List<String> urlVars = VarUtil.getElExpressions(testHttp.getUrl());
            for(String varName : urlVars) {
                if (paramParamMap.containsKey(varName)) {
                    urlParamMap.put(varName, paramParamMap.get(varName));
                }
            }
            String url = VarUtil.var(urlParamMap, testHttp.getUrl());
            if (httpMethod == HttpMethod.Post) {
                if (httpBody != null) {
                    String body = VarUtil.var(paramMap, httpBody.getBody());
                    paramData.put("body", body);
                    TestLogData testLogData = TestLogData.buildTestLogData(testLog.getId(), treeItem.getValue().getName(), TestLogType.http, JsonUtil.toJson(paramData));
                    TestLogDataDao.insert(testLogData);
                    result = HttpClientUtil.getInstance().sendHttpPost(url, headerParams, httpParams, body, ContentType.valueOf(httpBody.getContentType()).getContent());
                } else {
                    TestLogData testLogData = TestLogData.buildTestLogData(testLog.getId(), treeItem.getValue().getName(), TestLogType.http, JsonUtil.toJson(paramData));
                    TestLogDataDao.insert(testLogData);
                    result = HttpClientUtil.getInstance().sendHttpPost(testHttp.getUrl(), headerParams, httpParams);
                }
            } else if (httpMethod == HttpMethod.Get) {
                TestLogData testLogData = TestLogData.buildTestLogData(testLog.getId(), treeItem.getValue().getName(), TestLogType.http, JsonUtil.toJson(paramData));
                TestLogDataDao.insert(testLogData);
                result = HttpClientUtil.getInstance().sendHttpGet(url, headerParams, httpParams);
            }
            testLog.increaseSuccessNum(1);
            if (result != null) {
                if (consumer != null)consumer.accept(result);
                LogUtil.log("httpApi:" + treeItem.getValue().getName() + ":" + url);
                LogUtil.log("返回结果:" + result, 25, color(true));

                TestLogData testLogData = TestLogData.buildTestLogData(testLog.getId(), treeItem.getValue().getName() + "-http结果", TestLogType.response, result);
                TestLogDataDao.insert(testLogData);

                for(TreeItem<TreeNode> treeItemt : treeItem.getChildren()) {
                    System.out.println("treeItemt:" + treeItemt.getValue().getName());
                    Map<String, Object> tempMap = new HashMap();
                    tempMap.putAll(paramMap);
                    tempMap.put("result", result);
                    boolean success = runTreeItem(treeItemt, tempMap, handler, consumer, testLog, isDebug);
                    isSuccess = success ? isSuccess : false;
                    tempMap = null;
                }
            }
            setTreeNodeMark(treeItem, 1);
        } catch (Exception e) {
            if (consumer != null) consumer.accept(e.getMessage());
//            LogUtil.log(e.getMessage());
            testLog.increaseFailureNum(1);
            setTreeNodeMark(treeItem, 2);

            throw new RuntimeException(e);
        }
        return isSuccess;
    }

    private static boolean runMethod(TreeItem<TreeNode> treeItem, Map<String, Object> paramMap, Consumer<TreeItem<TreeNode>> handler, Consumer<String> consumer, TestLog testLog, boolean isDebug) throws Exception {
        boolean isSuccess = true;
        showTreeItem(handler, treeItem);

        cache(treeItem.getValue(), paramMap, isDebug, testLog);

        List<TestMethod> testMethodList = TestMethodDao.getByTreeNodeId(treeItem.getValue().getId());
        if (testMethodList.size() < 1) return true;
        TestMethod testMethod = testMethodList.get(0);
        List<TestMethodData> testMethodDataList = TestMethodDataDao.getByTestMethodId(testMethod.getId());
        Object bean = createBean(testMethod);
//        treeItem.getValue().put("bean", bean);
        List<ParamModel> allParamModel = RunTreeItem.getAllParamModel(treeItem);
        int i = 0;
        LogUtil.log("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        LogUtil.log(String.format("测试类：%s, 测试方法：%s", testMethod.getClassName(), testMethod.getMethodName()));
        int successNum = 0, failureNum = 0;
        for (TestMethodData  methodData : testMethodDataList) {
            Map<String, MethodData> data = JsonUtil.toObject(methodData.getData(), new TypeToken<Map<String, MethodData>>(){}.getType());
            data.values().stream().forEach(d->d.setId(methodData.getId()));
            LogUtil.log(String.format("\t检查第%s个测试数据", ++i));
            Object[] returnValues = runTestMethodData(treeItem, bean, testMethod, methodData, paramMap);

            boolean invokeIsSuccess = (boolean) returnValues[0];
            if (!invokeIsSuccess) {
                failureNum++;
                continue;
            }
            successNum++;

            // 设置需要检查的变量
            Method method = MethodUtil.findMethodByMethodName(testMethod);
            for (Map.Entry<String, MethodData> entry : data.entrySet()) {
                String key = entry.getKey();
                MethodData methodData1 = entry.getValue();
                if (VarUtil.isPutVar(methodData1.getData())) {
                    String[] methodInfoes = key.split(" ");
                    if (methodData1.getConvert() == null) { // get 方法
                        try {
                            Method methodt = bean.getClass().getDeclaredMethod(methodInfoes[1]);
                            Object value = methodt.invoke(bean);
                            paramMap.put(VarUtil.getPutVarName(methodData1.getData()), value);
                        } catch (NoSuchMethodException e) {  // 可能方法被修改
                            LOGGER.warning(String.format("%s没有这个方法%s", bean.getClass(), methodInfoes[1]));
                        }

                    } else if (methodInfoes[1].equals(method.getName())) {
                        paramMap.put(VarUtil.getPutVarName(methodData1.getData()), returnValues[1]);
                    }
                }
            }
        }
        if (successNum > 0 && failureNum == 0) setTreeNodeMark(treeItem, 1);
        if (successNum == 0 && failureNum > 0) setTreeNodeMark(treeItem, 2);
        if (successNum > 0 && failureNum > 0) setTreeNodeMark(treeItem, 3);

        Map<String, Object> dataMap = new HashMap();
        dataMap.put("param", paramMap);
        dataMap.put("method", testMethod);
        dataMap.put("data", testMethodDataList);
        TestLogData testLogData = TestLogData.buildTestLogData(testLog.getId(), treeItem.getValue().getName(), TestLogType.method, JsonUtil.toJson(dataMap));
        TestLogDataDao.insert(testLogData);

        for(TreeItem<TreeNode> childTreeItem : treeItem.getChildren()) {
            boolean success = runTreeItem(childTreeItem, paramMap, handler, consumer, testLog, isDebug);
            if (success) {
                successNum++;
            } else {
                isSuccess = false;
                failureNum++;
            }
        }

        if (successNum > 0 && failureNum == 0) setTreeNodeMark(treeItem, 1);
        if (successNum == 0 &&  failureNum > 0) setTreeNodeMark(treeItem, 2);
        if (successNum > 0 &&  failureNum > 0) setTreeNodeMark(treeItem, 3);

        return isSuccess;
    }

    private static boolean runSqlCheckData(TreeItem<TreeNode> treeItem, TestLog testLog, Map<String, Object> paramMap, Consumer<TreeItem<TreeNode>> handler, boolean isDebug) throws SQLException {
        boolean isSuccess = true;
        showTreeItem(handler, treeItem);
        List<SqlCheck> sqlCheckList = SqlCheckDao.getByTreeNodeId(treeItem.getValue().getId());
        if (sqlCheckList.size() < 1) return true;

        cache(treeItem.getValue(), paramMap, isDebug, testLog);

        for (SqlCheck sqlCheck : sqlCheckList) {
            String sql = VarUtil.replaceLine(sqlCheck.getSql());
            LogUtil.log(String.format("\t\t验证Sql数据：%s， sql: %s, args: %s", sqlCheck.getName(), sql, sqlCheck.getArgs()));
            List<SqlCheckData> sqlCheckDataList = new ArrayList();
            try {
                List<Map<String, SqlColumnData>> databaseDataList = SqlCheckDao.getSqlDataForSql(sqlCheck, sqlCheck.getDatabaseParam(), paramMap); // 数据库中实际值
                sqlCheckDataList = SqlCheckDataDao.getSqlCheckData(sqlCheck);  // 期望值
                cleanSqlCheckOldData(sqlCheckDataList);
                initVarValue(sqlCheckDataList, paramMap); // 期望值具体化
                boolean success = mergeSqlCheckData(databaseDataList, sqlCheckDataList, treeItem, testLog);
                saveSqlCheckData(sqlCheckDataList, sqlCheck);
                TestLogDao.update(testLog);
                logSqlCheck(testLog.getId(), sqlCheck, sqlCheckDataList);

                if (!success) {
                    isSuccess = false;
                }
            } catch (Exception e) {
                logSqlCheck(testLog.getId(), sqlCheck, sqlCheckDataList);
                LogUtil.log(String.format("\t\t验证失败，出现异常: %s", e), 50, color(false));
                e.printStackTrace();
                treeItem.getValue().getRunSuccess().set(2);

                if (isDebug) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (!isSuccess) {
            setTreeNodeMark(treeItem, 2);
        }
        if (isDebug && !isSuccess) {
            throw new RuntimeException("检查结果失败");
        }
        return isSuccess;
    }

    private static void cleanSqlCheckOldData(List<SqlCheckData> sqlCheckDataList) {
        sqlCheckDataList.forEach(sqlCheckData -> {
            sqlCheckData.clean();
            SqlCheckDataDao.save(sqlCheckData);
        });
    }

    private static void initVarValue(List<SqlCheckData> sqlCheckDataList, Map<String, Object> paramMap) throws Exception {
        for (SqlCheckData sqlCheckData : sqlCheckDataList) {
            Map<String, SqlColumnData> sqlCheckDataMap = sqlCheckData.getDataMap();
            for (Map.Entry<String, SqlColumnData> entry : sqlCheckDataMap.entrySet()) {
                String value = entry.getValue().getExpectValue();
                if (VarUtil.isPutVar(value)) continue;
                String value2 = BeetlFactory.getInstance().process(value, paramMap);
                entry.getValue().setChangedValue(value2);
            }
        }
    }

    private static void saveSqlCheckData(List<SqlCheckData> sqlCheckDataList, SqlCheck sqlCheck) {
        for (SqlCheckData sqlCheckData : sqlCheckDataList) {
            Map<String, SqlColumnData> sqlCheckDataMap = sqlCheckData.getDataMap();
            if (sqlCheckDataMap.size() < 1) return;
            SqlCheckDataDao.save(sqlCheckData);
        }
    }

    private static void logSqlCheck(Integer testLogId, SqlCheck sqlCheck, List<SqlCheckData> sqlCheckDataList) throws SQLException {
        Map<String, Object> map = new HashMap();
        map.put("sqlCheck", sqlCheck);
        map.put("sqlCheckData", sqlCheckDataList);
        TestLogData testLogData = TestLogData.buildTestLogData(testLogId, sqlCheck.getName(), TestLogType.sqlCheck, JsonUtil.toJsonExpose(map));
        TestLogDataDao.insert(testLogData);
    }

    private static boolean mergeSqlCheckData(List<Map<String, SqlColumnData>> databaseDataList, List<SqlCheckData> sqlCheckDataList, TreeItem<TreeNode> treeItem, TestLog testLog) {
        int successNum = 0;
        int failureNum = 0;
        if (databaseDataList.size() > sqlCheckDataList.size()) {
            int idx = 0;
            for (SqlCheckData sqlCheckData : sqlCheckDataList) {
                Map<String, SqlColumnData> sqlCheckDataMap = sqlCheckData.getDataMap();
                Map<String, SqlColumnData> databaseDataMap = databaseDataList.get(idx++);
                for (Map.Entry<String, SqlColumnData> entry : sqlCheckDataMap.entrySet()) {
                    SqlColumnData sqlColumnData = entry.getValue();
                    SqlColumnData databaseData = databaseDataMap.get(entry.getKey());
                    if (databaseData == null) databaseData = databaseDataMap.get(entry.getKey().toUpperCase());
                    if (databaseData == null) databaseData = databaseDataMap.get(entry.getKey().toLowerCase());

                    if (VarUtil.isPutVar(sqlColumnData.getExpectValue())) {
                        VarUtil.putValue(sqlColumnData.getExpectValue(), treeItem, databaseData.getValue());
                        continue;
                    }
                    sqlColumnData.setValue(databaseData.getValue().toString());
                    databaseData.setExpectValue(sqlColumnData.getExpectValue());
                    Boolean isSuccess = ObjectUtil.isEqual(databaseData.getValue(),sqlColumnData.getChangedValue());
                    sqlColumnData.setIsEqual(isSuccess);
                    if (isSuccess) successNum++;
                    if (!isSuccess) failureNum++;
                    LogUtil.log(String.format("验证SQL数据: %s, 结果：%s, 期望值：%s, 实际值：%s", sqlColumnData.getDesc(), isSuccess, sqlColumnData.getChangedValue(), databaseData.getValue()), 75, color(isSuccess));
                }
                sqlCheckData.setContent(JsonUtil.toJson(sqlCheckDataMap));
            }
            LogUtil.log(String.format("【警告】数据中只有%s条数据，验证数据有%s条, 不对称的数据被忽略", databaseDataList.size(), sqlCheckDataList.size()), 75, "gray");
            if (successNum > 0) setTreeNodeMark(treeItem, 1);
            if (failureNum > 0) setTreeNodeMark(treeItem, 2);
            if (successNum != 0 && failureNum != 0) setTreeNodeMark(treeItem, 3);
            testLog.increaseSuccessNum(successNum);
            testLog.increaseFailureNum(failureNum);

            return successNum > 0 && failureNum == 0;
        } else {
            int idx = 0;
            for (Map<String, SqlColumnData> databaseDataMap : databaseDataList) {
                SqlCheckData sqlCheckData = sqlCheckDataList.get(idx++);
                Map<String, SqlColumnData> sqlCheckDataMap = sqlCheckData.getDataMap();
                for (Map.Entry<String, SqlColumnData> entry : sqlCheckDataMap.entrySet()) {
                    SqlColumnData sqlColumnData = entry.getValue();
                    SqlColumnData databaseData = databaseDataMap.get(entry.getKey());
                    if (databaseData == null) databaseData = databaseDataMap.get(entry.getKey().toUpperCase());
                    sqlColumnData.setValue(databaseData.getValue().toString());

                    if (VarUtil.isPutVar(sqlColumnData.getExpectValue())) {
                        VarUtil.putValue(sqlColumnData.getExpectValue(), treeItem, databaseData.getValue());
                        continue;
                    }

                    Boolean isSuccess = ObjectUtil.isEqual(databaseData.getValue(),sqlColumnData.getChangedValue());
                    sqlColumnData.setIsEqual(isSuccess);
                    if (isSuccess) successNum++;
                    if (!isSuccess) failureNum++;
                    LogUtil.log(String.format("验证SQL数据: %s, 结果：%s, 期望值：%s, 实际值：%s", sqlColumnData.getDesc(), isSuccess, sqlColumnData.getChangedValue(), databaseData.getValue()), 75, color(isSuccess));
                    databaseData.setExpectValue(sqlColumnData.getExpectValue());
                }
                sqlCheckData.setContent(JsonUtil.toJson(sqlCheckDataMap));
            }
            if (databaseDataList.size() != sqlCheckDataList.size()) {
                LogUtil.log(String.format("【警告】数据中只有%s条数据，验证数据有%s条, 不对称的数据被忽略", databaseDataList.size(), sqlCheckDataList.size()), 75, "gray");
            }
            testLog.increaseSuccessNum(successNum);
            testLog.increaseFailureNum(failureNum);
            if (successNum > 0) setTreeNodeMark(treeItem, 1);
            if (failureNum > 0) setTreeNodeMark(treeItem, 2);
            if (successNum != 0 && failureNum != 0) setTreeNodeMark(treeItem, 3);

            return successNum > 0 && failureNum == 0;
        }
    }

    public static Object[] runTestMethodData(TreeItem<TreeNode> treeItem, Object bean, TestMethod testMethod, TestMethodData testMethodData, Map<String, Object> paramMap) {
        Object value = null;
        boolean isSuccess = false;
        try {
            Method method = MethodUtil.findMethodByMethodName(testMethod);
            Map<String, MethodData> data = JsonUtil.toObject(testMethodData.getData(), new TypeToken<Map<String, MethodData>>(){}.getType());

            Object[] args = MethodUtil.buildArgs(data, method, paramMap);

            try {
                for (Map.Entry<String, MethodData> entry : data.entrySet()) {
                    MethodData methodData = entry.getValue();
                    if (methodData.getMethodType() == MethodType.Set && !StringUtils.isEmpty(entry.getValue().getData())) {
                        Object setValue = null;
                        if (entry.getValue().getConvert().equals("Bean")) {
                            setValue = paramMap.get(VarUtil.getVarName(entry.getValue().getData()));
                        } else if (entry.getValue().getConvert().equals("Json")) {
                            String setValueStr = BeetlFactory.getInstance().process(entry.getValue().getData(), paramMap);
                            JsonConvert convert = (JsonConvert) ConvertFactory.getInstance().get(entry.getValue().getConvert());
                            convert.setType(MethodUtil.forClass(methodData.getClassName()));
                            setValue = convert.parse(setValueStr);
                        } else {
                            String setValueStr = BeetlFactory.getInstance().process(entry.getValue().getData(), paramMap);
                            setValue = ConvertFactory.getInstance().get(entry.getValue().getConvert()).parse(setValueStr);
                        }

                        String methodName = methodData.getKey().substring(0, methodData.getKey().indexOf("("));
                        Method setMethod = bean.getClass().getDeclaredMethod(methodName, new Class[]{MethodUtil.forClass(methodData.getClassName())});
                        MethodUtil.invoke(bean, setMethod, new Object[]{setValue});
                        LogUtil.log(String.format("\t\t设置属性： 方法：%s， 值：%s", setMethod.getName(), setValue));
                    }
                }
                LogUtil.log(String.format("\t\t参数：%s", JsonUtil.toSimpleJson(args)));
                value = MethodUtil.invoke(bean, method, args);
                LogUtil.log(String.format("\t\t运行结果：%s", value == null ? "无" : JsonUtil.toSimpleJson(value)), 50, "green");
                isSuccess = true;
            } catch (InvocationTargetException ee) {
                ee.printStackTrace();
                LogUtil.log(String.format("\t\t运行结果：%s", ee.getTargetException()));
                checkException(data, ee.getTargetException());
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.log(String.format("\t\t运行结果：%s", e));
                checkException(data, e);
            }
            if (isSuccess) {
                LogUtil.log(String.format("\t\t运行成功"), 50, "green");
//                treeNode.put("result", value);
                checkReturnData(data, value, method, paramMap);
            }
            testMethodData.setData(JsonUtil.toJson(data));
            if (testMethodData.getId() != null && !testMethodData.getId().equals(0)) {
                saveTestMethodData(testMethodData);
            }
        } catch (Exception e) {
            isSuccess = false;
            e.printStackTrace();
        }
        return new Object[]{isSuccess, value};
    }

    public static boolean runBeanCheck(TreeItem<TreeNode> treeItem, TestLog testLog, Map<String, Object> paramMap, Consumer<TreeItem<TreeNode>> handler, boolean isDebug) throws Exception {
        BeanCheck beanCheck = BeanCheckDao.getByTreeNodeId(treeItem.getValue().getId());

        cache(treeItem.getValue(), paramMap, isDebug, testLog);

        boolean runSuccess = true;

        Map<String, Object> contentMap = new HashMap();
        contentMap.put("param", paramMap);
        int successNum = 0, failureNum = 0;

        LogUtil.log(String.format("验证对象数据: %s, 对象数据： %s", treeItem.getValue().getName(), beanCheck.getCheckName()), 50);
        if (beanCheck.getBeanType() == BeanType.Json) {
            String json = VarUtil.var(paramMap, beanCheck.getCheckName());
//            ReadContext ctx = JsonPath.parse(json);
            Set<BeanData> beanDataSet = beanCheck.getBeanDatas();

            for (BeanData beanData : beanDataSet) {
                Class clazz = Class.forName(beanData.getClassType());
                Object value = null;
//                if (beanData.getSecondCheckName() == null || beanData.getSecondCheckName().equals("")) {
//                    value = ctx.read(beanData.getCheckName(), clazz);
//                } else {
//                    String secondJson = ctx.read(beanData.getCheckName(), String.class);
//                    value = JsonPath.parse(secondJson).read(beanData.getSecondCheckName(), clazz);
//                }
                JsonPathHelp jsonPathHelp = new JsonPathHelp(json, beanData.getCheckName());
                value = jsonPathHelp.jsonValue(clazz);
                JsonRootHelp jsonRootHelp = new JsonRootHelp(json, beanData.getExceptedValue());
                String str = jsonRootHelp.parse();
                Map<String, Object> paramMapt = jsonRootHelp.getNewParamMap();
                paramMapt.putAll(paramMap);
                String expectedValueString = BeetlFactory.getInstance().process(str, paramMapt);
                Object expectedValue = ConvertFactory.getInstance().get(clazz.getSimpleName()).parse(expectedValueString);
                beanData.setValue(expectedValue);

                CompareType compareType = beanData.getCompareType();
                compareType = compareType == null ? CompareType.Equal : compareType;
                boolean isSuccess = false;
                if (compareType == CompareType.Equal) {
                    isSuccess = value.equals(expectedValue);
                } else if (compareType == CompareType.Contain) {
                    isSuccess = value.toString().contains(expectedValue.toString());
                } else if (compareType == CompareType.Regix) {
                    Pattern p= Pattern.compile(expectedValueString);
                    isSuccess = p.matcher(value.toString()).matches();
                }

                if (isSuccess) successNum ++;
                if (!isSuccess) failureNum++;
                beanData.setSuccess(isSuccess);
                LogUtil.log(String.format("验证对象数据: %s, 期望值：%s, 实际值：%s, 结果：%s", beanData.getDesc(), expectedValueString, value, isSuccess), 75, color(isSuccess));

                if (!isSuccess) {
                    runSuccess = false;
                }
            }
            beanCheck.setContent(JsonUtil.toJson(beanDataSet));
            BeanCheckDao.save(beanCheck);
        } else if (beanCheck.getBeanType() == BeanType.String) {
            String text = VarUtil.var(paramMap, beanCheck.getCheckName());
            BeanData beanData = JsonUtil.toObject(beanCheck.getContent(), BeanData.class);
            String expectedValueString = BeetlFactory.getInstance().process(beanData.getExceptedValue(), paramMap);
            Map<String, Object> otherInfoMap = JsonUtil.toObject(beanCheck.getOtherInfo(), new TypeToken<Map<String, Object>>(){}.getType());
            CompareType compareType = CompareType.valueOf(otherInfoMap.get("compareType").toString());

            boolean isSuccess = false;
            if (compareType == CompareType.Equal) {
                isSuccess = text.equals(expectedValueString);
            } else if (compareType == CompareType.Contain) {
                isSuccess = text.contains(expectedValueString);
            } else if (compareType == CompareType.Regix) {
                Pattern p= Pattern.compile(expectedValueString);
                isSuccess = p.matcher(text).matches();
            }
            beanData.setValue(text);
            beanData.setExceptedObjectValue(expectedValueString);
            beanData.setSuccess(isSuccess);
            LogUtil.log(String.format("验证对象数据: %s, 期望值：%s, 实际值：%s, 结果：%s", beanCheck.getCheckName(), expectedValueString, text, isSuccess), 75, color(isSuccess));

            beanCheck.setContent(JsonUtil.toJson(beanData));
            BeanCheckDao.save(beanCheck);

            testLog.increaseSuccessNum(isSuccess ? 1: 0);
            testLog.increaseFailureNum(!isSuccess ? 1: 0);
            if (isSuccess) setTreeNodeMark(treeItem, 1); else setTreeNodeMark(treeItem, 2);
            TestLogDao.update(testLog);

            if (!isSuccess) {
                runSuccess = false;
            }
        } else if (beanCheck.getBeanType() == BeanType.Object) {
            Object bean = paramMap.get(VarUtil.getVarName(beanCheck.getCheckName()));
            List<Map<String, BeanData>> dataList = beanCheck.getBeanDatasForObject();
            String otherInfo = beanCheck.getOtherInfo();
            Map<String, Object> otherInfoMap = JsonUtil.toObject(otherInfo, new TypeToken<Map<String, Object>>(){}.getType());
            String beanValueTypeStr = (String) otherInfoMap.get("beanValueType");
            BeanValueType beanValueType = BeanValueType.valueOf(beanValueTypeStr);
            VarModel varModel = VarFactory.getInstance().getCache(treeItem, VarUtil.getVarName(beanCheck.getCheckName()));

            if (beanValueType == BeanValueType.Null) {
                boolean isSuccess = bean == null;
                if (isSuccess) successNum++; else failureNum++;
                LogUtil.log(String.format("验证对象数据: 数组为空, 结果：%s", isSuccess), 75, color(isSuccess));
            } else if (bean == null) {
                failureNum++;
                LogUtil.log(String.format("验证对象数据: 期望值非空，实际值为空, 结果：%s", false), 75, color(false));
            } else {
                if (ObjectUtil.isArray(bean)) {   // 数组
                    int[] compareResult = compareArray(bean, beanValueType, varModel, paramMap, dataList);
                    successNum += compareResult[0];
                    failureNum += compareResult[1];
                } else if (ObjectUtil.isList(bean)) {
                    int[] compareResult = compareCollection(bean, beanValueType, varModel, paramMap, dataList);
                    successNum += compareResult[0];
                    failureNum += compareResult[1];
                } else if (ObjectUtil.isSet(bean)) {
                    int[] compareResult = compareSet(bean, beanValueType, varModel, paramMap, dataList);
                    successNum += compareResult[0];
                    failureNum += compareResult[1];
                } else if (ObjectUtil.isMap(bean)) {
                    int[] compareResult =  compareMap(bean, beanValueType, varModel, paramMap, dataList);
                    successNum += compareResult[0];
                    failureNum += compareResult[1];
                } else if (ObjectUtil.commonClass().contains(varModel.getClazz())) {  // 常用类型
                    if (dataList.size() > 0 && dataList.get(0).size() > 0) {
                        Map<String, BeanData> beanDataMap = dataList.get(0);
                        BeanData beanData = beanDataMap.values().iterator().next();
                        String exceptedValue = BeetlFactory.getInstance().process(beanData.getExceptedValue(), paramMap);
                        Object value = ConvertFactory.getInstance().get(MethodUtil.forClass(beanData.getClassType()).getSimpleName()).parse(exceptedValue);
                        boolean issuccess = ObjectUtil.isEqual(bean, value);
                        if (issuccess) successNum ++; else  failureNum++;
                        LogUtil.log(String.format("验证对象数据: %s, 期望值: %s，实际值: %s, 结果：%s", beanData.getDesc(), exceptedValue, bean, issuccess), 75, color(issuccess));
                    } else {
                        LogUtil.log(String.format("验证对象数据: 设置失败"), 75, color(false));
                    }

                } else {  // bean类型
                    int[] compareResult =  compareBean(bean, beanValueType, varModel, paramMap, dataList);
                    successNum += compareResult[0];
                    failureNum += compareResult[1];
                }
            }

            beanCheck.setContent(JsonUtil.toJson(dataList));
            BeanCheckDao.save(beanCheck);

        } else if (beanCheck.getBeanType() == BeanType.EL) {
            Set<BeanData> beanDataSet = beanCheck.getBeanDatas();
            for (BeanData beanData : beanDataSet) {
                boolean isSuccess = false;
//                Object result = ElFactory.getInstance().process(beanData.getCheckName(), paramMap);
                String resultStr =BeetlFactory.getInstance().process(beanData.getCheckName(), paramMap);
                String valueStr = BeetlFactory.getInstance().process(beanData.getExceptedValue(), paramMap);
                Object value = ConvertFactory.getInstance().get(MethodUtil.forClass(beanData.getClassType()).getSimpleName()).parse(valueStr);
                Object result = ConvertFactory.getInstance().get(MethodUtil.forClass(beanData.getClassType()).getSimpleName()).parse(resultStr);
                beanData.setExceptedObjectValue(value);
                beanData.setValue(result);
                isSuccess = ObjectUtil.isEqual(result, value);

                LogUtil.log(String.format("验证对象数据: %s, 期望值：%s， 实际值： %s, 结果：%s", beanData.getDesc(), valueStr, result, isSuccess), 75, color(isSuccess));
                if(isSuccess) successNum++;
                if(!isSuccess) failureNum++;
                beanData.setSuccess(isSuccess);

                if (!isSuccess) {
                    runSuccess = false;
                }
            }

            beanCheck.setContent(JsonUtil.toJson(beanDataSet));
            BeanCheckDao.save(beanCheck);
        }

        testLog.increaseSuccessNum(successNum);
        testLog.increaseFailureNum(failureNum);
        if (successNum != 0 && failureNum != 0) setTreeNodeMark(treeItem, 3);
        if (successNum > 0 && failureNum == 0) setTreeNodeMark(treeItem, 1);
        if (successNum == 0 && failureNum >0) setTreeNodeMark(treeItem, 2);

        TestLogDao.update(testLog);
        for(TreeItem<TreeNode> childTreeItem : treeItem.getChildren()) {
            boolean success = runTreeItem(childTreeItem, paramMap, handler, null, testLog, isDebug);
            if (success) {
                successNum++;
            } else {
                runSuccess = false;
                failureNum++;
            }
        }

        contentMap.put("_content", beanCheck);

        TestLogData testLogData = new TestLogData();
        testLogData.setTestLogId(testLog.getId());
        testLogData.setName(treeItem.getValue().getName());
        testLogData.setType(TestLogType.bean);
        testLogData.setContent(JsonUtil.toJson(contentMap));
        TestLogDataDao.insert(testLogData);

        if (isDebug && !runSuccess) {
            throw new RuntimeException("检查对象失败");
        }
        return runSuccess;
    }

    private static int[] checkObjectValue(Map<String, BeanData> beanDataMap, Object objectValue, Map<String, Object> paramMap, VarModel varModel) {
        int successNum = 0, failureNum = 0;
        for (Map.Entry<String, BeanData> entry : beanDataMap.entrySet()) {
            boolean isSuccess = false;
            String methodName = entry.getKey();
            BeanData beanData = entry.getValue();
            try {
                if (beanData.getExceptedValue() == null || beanData.getExceptedValue().equals("")) continue;
                if (varModel.isListMap()) {
                    Map<Object, Object> map = (Map<Object, Object>) objectValue;
                    Object value = map.get(methodName);
                    beanData.setValue(value);
                    String evalue = BeetlFactory.getInstance().process(entry.getValue().getExceptedValue(), paramMap);
                    Object checkValue = ConvertFactory.getInstance().get(MethodUtil.forClass(beanData.getClassType()).getSimpleName()).parse(evalue);

                    beanData.setExceptedObjectValue(checkValue);
                    isSuccess = ObjectUtil.isEqual(value, checkValue);
                    beanData.setSuccess(isSuccess);
                    LogUtil.log(String.format("验证对象数据: %s, 期望值：%s, 实际值：%s, 结果：%s", beanData.getDesc(), evalue, value, isSuccess), 75, color(isSuccess));
                } else {
                    if (ObjectUtil.commonClass().contains(varModel.getBeanClazz())) { // 简单类型
                        beanData.setValue(objectValue);
                        String evalue = BeetlFactory.getInstance().process(entry.getValue().getExceptedValue(), paramMap);
                        Object checkValue = ConvertFactory.getInstance().get(MethodUtil.forClass(entry.getValue().getClassType()).getSimpleName()).parse(evalue);

                        beanData.setExceptedObjectValue(checkValue);
                        isSuccess = ObjectUtil.isEqual(objectValue, checkValue);
                        beanData.setSuccess(isSuccess);
                        LogUtil.log(String.format("验证对象数据: %s, 期望值：%s, 实际值：%s, 结果：%s", beanData.getDesc(), evalue, objectValue, isSuccess), 75, color(isSuccess));
                    } else {
                        Method method = objectValue.getClass().getDeclaredMethod(methodName);
                        Object value = method.invoke(objectValue, new Object[]{});
                        beanData.setValue(value);
                        if (VarUtil.isPutVar(entry.getValue().getExceptedValue())) {
                            paramMap.put(VarUtil.getPutVarName(entry.getValue().getExceptedValue()), value);
                            continue;
                        }
                        String evalue = BeetlFactory.getInstance().process(entry.getValue().getExceptedValue(), paramMap);
                        Object checkValue = null;
                        if (!evalue.equalsIgnoreCase("null")) {
                            checkValue = ConvertFactory.getInstance().get(method.getReturnType().getSimpleName()).parse(evalue);
                        }

                        beanData.setExceptedObjectValue(checkValue);
                        isSuccess = ObjectUtil.isEqual(value, checkValue);
                        beanData.setSuccess(isSuccess);
                        LogUtil.log(String.format("验证对象数据: %s, 期望值：%s, 实际值：%s, 结果：%s", beanData.getDesc(), evalue, value, isSuccess), 75, color(isSuccess));
                    }
                }
            } catch (Exception e) {
                LogUtil.log(String.format("验证对象数据: %s, msg: %s, 结果：%s", beanData.getDesc(), e.getMessage(), isSuccess), 75, color(isSuccess));
                e.printStackTrace();
            }
            if (isSuccess) successNum++; else failureNum++;
        }
        return new int[]{successNum, failureNum};
    }

    public static Object createBean(TestMethod testMethod) throws Exception {
        String className = testMethod.getClassName();
        InstanceClassType instanceClassType = InstanceClassType.valueOf(testMethod.getInstanceClassName());
        Class clazz = Class.forName(className);
        if (instanceClassType == InstanceClassType.Reflect) {
            return clazz.newInstance();
        } else if (instanceClassType == InstanceClassType.Spring) {
            String resourceName = getResourceName(clazz);
            if (resourceName != null) {
                return SpringInitFactory.getInstance().getBean(resourceName);
            }
            return SpringInitFactory.getInstance().getBean(clazz);
        } else {
            throw new RuntimeException("不支持这种实例化类型：" + instanceClassType);
        }
    }

    private static String getResourceName(Class clazz) {
        Component component = (Component) clazz.getAnnotation(Component.class);
        if (component != null) {
            String name = component.value();
            if (name.equals("")) return null;
            return name;
        }
        Service service = (Service) clazz.getAnnotation(Service.class);
        if (service != null) {
            String name = service.value();
            if (name.equals("")) return null;
            return name;
        }
        return null;
    }

    public static void checkException(Map<String, MethodData> data, Throwable e) {
        LogUtil.log(String.format("\t\t开始检查异常"));
        MethodData exceptionValue = data.get("exception");
        Object value = ConvertFactory.getInstance().get(exceptionValue.getConvert()).parse(exceptionValue.getData());
        boolean isSuccess = value.equals(Boolean.TRUE);
        int idx = isSuccess ? 1 : 0;
//        MethodData methodData = new MethodData(0, "check", isSuccess + "", "boolean", isSuccess, "1/1");
//        data.put("check", methodData);
        LogUtil.log(String.format("\t\t\t检查结果：%s, 期望值：%s，实际值：%s", isSuccess, isSuccess ? "异常":"无异常", "异常"));

        MethodData exceptionValue2 = data.get("exceptionName");
        if (exceptionValue2.getData() == null || exceptionValue2.getData().trim().equalsIgnoreCase("")) {
            LogUtil.log(String.format("\t\t\t检查结果：%s, 期望值：%s，实际值：%s", isSuccess, "null", e.getClass().getName()));
            return;
        }
        Object value2 = ConvertFactory.getInstance().get(exceptionValue2.getConvert()).parse(exceptionValue2.getData());
        System.out.println(value2);
        boolean isSuccess2 = exceptionValue2.getData().equals(e.getClass().getName());
        idx = isSuccess2 ? ++idx : idx;
        LogUtil.log(String.format("\t\t\t检查结果：%s, 期望值：%s，实际值：%s", isSuccess2, exceptionValue2.getData(), e.getClass().getName()));
    }

    public static void checkReturnData(Map<String, MethodData> data, Object value, Method method, Map<String, Object> paramMap) throws Exception {
        if (method.getReturnType() != void.class) {
//            LogUtil.log(String.format("\t\t开始检查返回值"));
            String methodName = MethodUtil.buildMethodName(method);
            String returnName = MethodUtil.methodReturnName(method);
            MethodData methodData = data.get(returnName);
            if (VarUtil.isPutVar(methodData.getData())) return;
            if (MethodUtil.isPrimitive(method.getReturnType())) {
                if (data.containsKey(methodName)) {
                    Object valueV = MethodUtil.buildReturnValue(data, method, paramMap);
                    boolean isSuccess = value.equals(valueV);
                    LogUtil.log(String.format("\t\t\t检查结果：%s, 期望值：%s, 实际值：%s", isSuccess, JsonUtil.toSimpleJson(valueV), JsonUtil.toSimpleJson(value)), 75, color(isSuccess));
                }
            } else if (MethodUtil.isCollection(method.getReturnType()) || MethodUtil.isMap(method.getReturnType()) || method.getReturnType().isArray()) {
                Object valueV = MethodUtil.buildReturnValue(data, method, paramMap);
                boolean isSuccess = JsonUtil.toJson(value).equals(JsonUtil.toJson(valueV));
                LogUtil.log(String.format("\t\t\t检查结果：%s, 期望值：%s, 实际值：%s", isSuccess, JsonUtil.toSimpleJson(valueV), JsonUtil.toSimpleJson(value)), 75, color(isSuccess));
            } else if (method.getReturnType().isEnum()) {
                Object valueV = MethodUtil.buildReturnValue(data, method, paramMap);
                boolean isSuccess = value.equals(valueV);
                LogUtil.log(String.format("\t\t\t检查结果：%s, 期望值：%s, 实际值：%s", isSuccess, JsonUtil.toSimpleJson(valueV), JsonUtil.toSimpleJson(value)), 75, color(isSuccess));
            }
        } else {
            LogUtil.log(String.format("\t\t无检查项"));
        }
    }

    public static void saveTestMethodData(TestMethodData testMethodData) throws SQLException {
        TestMethodDataDao.save(testMethodData);
    }

    public static List<ParamModel> getAllParamModel(TreeItem<TreeNode> treeItem) throws SQLException {
        List<ParamModel> allParamModel = new ArrayList<>();
        TreeItem<TreeNode> temp = treeItem;
        while(temp.getValue().getTreeNodeType() != TreeNodeType.Root) {
            for (TreeItem<TreeNode> child : temp.getChildren()) {
                if (child.getValue().getTreeNodeType() == TreeNodeType.Param) {
                    allParamModel.addAll(ParamDao.getByTreeNodeId(child.getValue().getId()));
                }
            }
            temp = temp.getParent();
        }
        return allParamModel;
    }

    public static Object buildValue(ParamModel paramModel) throws SQLException {
        if (paramModel.getParamCategory() == ParamCategory.Increment) {
            return IncrementVarDao.getAndIncrease(paramModel.getName());
        }
        return null;
    }

    public static void runSpring(TreeItem<TreeNode> treeItem) {
        try {
            SpringInitFactory.getInstance().initSpring(treeItem);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private static void showTreeItem(Consumer<TreeItem<TreeNode>> handler, TreeItem<TreeNode> treeItem) {
        if (handler != null) {
            handler.accept(treeItem);
        }
    }

    // 删除节点上运行标识
    public static void cleanTreeNode(TreeItem<TreeNode> treeItem) {
        for(TreeItem<TreeNode> child : treeItem.getChildren()) {
            child.getValue().getRunSuccess().set(0);
            cleanTreeNode(child);
        }
    }

    private static void setTreeNodeMark(TreeItem<TreeNode> treeItem, Integer mark) {
        Platform.runLater(()->{
            treeItem.getValue().getRunSuccess().set(mark);
        });
    }

    private static String color(boolean isSuccess) {
        return isSuccess ? "green" : "red";
    }

    private static void cache(TreeNode treeNode, Map<String, Object> paramMap, boolean isDebug, TestLog testLog) {
        if (isDebug) {
            DebugCacheFactory.getInstance().cache(treeNode, paramMap);
            DebugCacheFactory.getInstance().cache(treeNode, testLog);
        }
    }

    private static int[] compareArray(Object bean, BeanValueType beanValueType, VarModel varModel, Map<String, Object> paramMap, List<Map<String, BeanData>> dataList) {
        Object[] beanArray = (Object[]) bean;
        int successNum = 0, failureNum = 0;
        if (beanValueType == BeanValueType.Empty) {
            int length = ((Object[]) bean).length;
            boolean isSuccess = length == 0;
            if (isSuccess) successNum++; else failureNum++;
            LogUtil.log(String.format("验证对象数据: 数组长度, 期望值：0， 实际值： %s, 结果：%s", length, isSuccess), 75, color(isSuccess));
        } else if (beanValueType == BeanValueType.Not_null) {
            int index = 0;
            if (beanArray.length < dataList.size()) { // 期望列表比实际值列表多
                for (Object objectValue : beanArray) {
                    Map<String, BeanData> beanDataMap = dataList.get(index++);
                    int[] result = checkObjectValue(beanDataMap, objectValue, paramMap, varModel);
                    successNum+= result[0];  failureNum+=result[1];
                }
            } else {
                for (Map<String, BeanData> beanDataMap : dataList) {
                    Object objectValue = beanArray[index++];
                    int[] result = checkObjectValue(beanDataMap, objectValue, paramMap, varModel);
                    successNum+= result[0];  failureNum+=result[1];
                }
            }
            if (beanArray.length != dataList.size()) {
                LogUtil.log(String.format("验证对象数据: 队列两边长度不一致，超出的数据被舍弃, 期望长度：%s， 实际长度： %s, " +
                        "结果：%s", dataList.size(), beanArray.length, false), 75, color(false));
            }
        }
        return new int[]{successNum, failureNum};
    }

    private static int[] compareCollection(Object bean, BeanValueType beanValueType, VarModel varModel, Map<String, Object> paramMap, List<Map<String, BeanData>> dataList) {
        Collection beanList = (Collection) bean;
        int successNum = 0, failureNum = 0;
        if (beanValueType == BeanValueType.Empty) {
            boolean isSuccess = beanList.size() == 0;
            if (isSuccess) successNum++; else failureNum++;
            LogUtil.log(String.format("验证对象数据: 队列长度, 期望长度：0， 实际长度： %s, 结果：%s", beanList.size(), isSuccess), 75, color(isSuccess));
        } else if (beanValueType == BeanValueType.Not_null) {
            int index = 0;
            if (beanList.size() < dataList.size()) { // 期望列表比实际值列表多
                for (Object objectValue : beanList) {
                    Map<String, BeanData> beanDataMap = dataList.get(index++);
                    int[] result = checkObjectValue(beanDataMap, objectValue, paramMap, varModel);
                    successNum+= result[0];  failureNum+=result[1];
                }
            } else {
                for (Map<String, BeanData> beanDataMap : dataList) {
                    Object objectValue = beanList.toArray()[index++];
                    int[] result = checkObjectValue(beanDataMap, objectValue, paramMap, varModel);
                    successNum+= result[0];  failureNum+=result[1];
                }
            }
            if (beanList.size() != dataList.size()) {
                LogUtil.log(String.format("验证对象数据: 队列两边长度不一致，超出的数据被舍弃, 期望长度：%s， 实际长度： %s, " +
                        "结果：%s", dataList.size(), beanList.size(), false), 75, color(false));
                failureNum++;
            }
        }
        return new int[]{successNum, failureNum};
    }

    private static int[] compareSet(Object bean, BeanValueType beanValueType, VarModel varModel, Map<String, Object> paramMap, List<Map<String, BeanData>> dataList) {
        Set set = (Set) bean;
        int successNum = 0, failureNum = 0;
        if (beanValueType == BeanValueType.Empty) {
            boolean isSuccess = set.size() == 0;
            if (isSuccess) successNum++; else failureNum++;
            LogUtil.log(String.format("验证对象数据: Set长度, 期望长度：0， 实际长度： %s, 结果：%s", set.size(), isSuccess), 75, color(isSuccess));
        } else if (beanValueType == BeanValueType.Not_null) {
            if (set.size() == 0) {
                LogUtil.log(String.format("验证对象数据: Set长度为空，" + "结果：%s", false), 75, color(false));
                failureNum++;
            } else {
                Object obj = set.iterator().next();
                if (ObjectUtil.commonClass().contains(obj.getClass())) {
                    for (Map<String, BeanData> beanDataMap : dataList) {
                        try {
                            BeanData beanData = beanDataMap.values().iterator().next();
                            String evalue = BeetlFactory.getInstance().process(beanData.getExceptedValue(), paramMap);
                            Object checkValue = ConvertFactory.getInstance().get(MethodUtil.forClass(beanData.getClassType()).getSimpleName()).parse(evalue);

                            beanData.setExceptedObjectValue(checkValue);
                            boolean isSuccess = set.contains(checkValue);
                            beanData.setSuccess(isSuccess);
                            beanData.setValue("set不检查值");
                            if (isSuccess) successNum++;  failureNum++;

                        } catch (Exception e) {
                            e.printStackTrace();
                            failureNum++;
                        }
                    }
                }

                if (set.size() != dataList.size()) {
                    LogUtil.log(String.format("验证对象数据: 队列两边长度不一致，超出的数据被舍弃, 期望长度：%s， 实际长度： %s, " +
                            "结果：%s", dataList.size(), set.size(), false), 75, color(false));
                    failureNum++;
                }
            }
        }
        return new int[]{successNum, failureNum};
    }


    private static int[] compareMap(Object bean, BeanValueType beanValueType, VarModel varModel, Map<String, Object> paramMap, List<Map<String, BeanData>> dataList) throws Exception {
        Map<Object, Object> map = (Map) bean;
        int successNum = 0, failureNum = 0;
        if (beanValueType == BeanValueType.Empty) {
            boolean isSuccess = map.size() == 0;
            if (isSuccess) successNum++; else failureNum++;
            BeanData beanData = dataList.get(0).values().iterator().next();
            beanData.setValue(map.size());
            beanData.setExceptedObjectValue(0);
            beanData.setSuccess(isSuccess);
            LogUtil.log(String.format("验证对象数据: Map长度, 期望长度：0， 实际长度： %s, 结果：%s", map.size(), isSuccess), 75, color(isSuccess));
        } else if (beanValueType == BeanValueType.Not_null) {
            if  (dataList.size() > 0) {
                for (Map<String, BeanData> beanDataMap : dataList) {
                    try {
                        BeanData keyBeanData = beanDataMap.get("key");
                        String keyStr = BeetlFactory.getInstance().process(keyBeanData.getExceptedValue(), paramMap);
                        Object exceptedKeyValue = ConvertFactory.getInstance().get(MethodUtil.forClass(keyBeanData.getClassType()).getSimpleName()).parse(keyStr);
                        keyBeanData.setExceptedObjectValue(exceptedKeyValue);
                        Object mapValue = map.get(exceptedKeyValue);

                        if (mapValue == null) {
                            keyBeanData.setValue("");
                            keyBeanData.setSuccess(false);
                            LogUtil.log(String.format("验证对象Map中key数据: %s, 期望值：%s， 实际值： %s, 结果：%s", keyBeanData.getDesc(), keyStr, "Null", false), 75, color(false));
                            failureNum++;
                            continue;
                        }
                        keyBeanData.setValue(exceptedKeyValue);
                        keyBeanData.setSuccess(true);
                        if (ObjectUtil.commonClass().contains(varModel.getBeanClazz())) { // 普通类型
                            BeanData valueBeanData = beanDataMap.get(varModel.getBeanClazz().getSimpleName());
                            String valueStr = BeetlFactory.getInstance().process(valueBeanData.getExceptedValue(), paramMap);
                            Object exceptionValue = ConvertFactory.getInstance().get(MethodUtil.forClass(valueBeanData.getClassType()).getSimpleName()).parse(valueStr);
                            valueBeanData.setExceptedObjectValue(exceptionValue);
                            valueBeanData.setValue(mapValue);
                            if (mapValue != null) {
                                boolean isSuccess = ObjectUtil.isEqual(exceptionValue, mapValue);
                                valueBeanData.setSuccess(isSuccess);
                                LogUtil.log(String.format("验证对象Map数据: %s, 期望值：%s， 实际值： %s, 结果：%s", valueBeanData.getDesc(), exceptionValue, mapValue, isSuccess), 75, color(isSuccess));
                                if (isSuccess) successNum++; else failureNum++;
                            }
                        } else { // 复杂类型
                            for (Map.Entry<String, BeanData> entry : beanDataMap.entrySet()) {
                                if (entry.getKey().equals("key")) continue;
                                BeanData beanData = entry.getValue();
                                if (StringUtils.isEmpty(beanData.getExceptedValue())) continue;
                                if (VarUtil.isPutVar(beanData.getExceptedValue())) {
                                    paramMap.put(VarUtil.getPutVarName(beanData.getExceptedValue()), mapValue);
                                    continue;
                                }
                                String value = BeetlFactory.getInstance().process(beanData.getExceptedValue(), paramMap);
                                try {
                                    Object exceptedValue = ConvertFactory.getInstance().get(MethodUtil.forClass(beanData.getClassType()).getSimpleName()).parse(value);
                                    beanData.setExceptedObjectValue(exceptedValue);
                                    String[] methodInfoes = beanData.getCheckName().split(" ");
                                    String methodName = beanData.getCheckName();
                                    if (methodInfoes.length > 1) {
                                        methodName = methodInfoes[1];
                                    }
                                    Method method = mapValue.getClass().getDeclaredMethod(methodName);
                                    Object value2 = method.invoke(mapValue);
                                    boolean isSuccess = ObjectUtil.isEqual(exceptedValue, value2);
                                    beanData.setSuccess(isSuccess);
                                    LogUtil.log(String.format("验证对象Map数据: %s, 期望值：%s， 实际值： %s, 结果：%s", beanData.getDesc(), exceptedValue, value2, isSuccess), 75, color(isSuccess));
                                    if (isSuccess) successNum++; else failureNum++;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            if (dataList.size() != map.size()) {
                LogUtil.log(String.format("验证对象数据: Map长度, 期望长度：%s， 实际长度： %s, 结果：%s", dataList.size(), map.size(), false), 75, color(false));
            }
        }
        return new int[] {successNum, failureNum};
    }

    public static int[] compareBean(Object bean, BeanValueType beanValueType, VarModel varModel, Map<String, Object> paramMap, List<Map<String, BeanData>> dataList) {
        int successNum = 0, failureNum = 0;
        if (beanValueType == BeanValueType.Not_null) {
            int[] result = checkObjectValue(dataList.get(0), bean, paramMap, varModel);
            successNum+= result[0];  failureNum+=result[1];
        }
        return new int[]{successNum, failureNum};
    }

    private static void loadFunction(TreeItem<TreeNode> treeItem) throws Exception {
        if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Case) {
            LoadFunction2(treeItem);
        } else {
            TreeItem<TreeNode> tmp = treeItem;
            while(tmp.getValue().getTreeNodeType() != TreeNodeType.Case) {
                tmp = tmp.getParent();
            }
            LoadFunction2(tmp);
        }
    }

    private static void LoadFunction2(TreeItem<TreeNode> treeItem) throws Exception {
        List<TreeItem<TreeNode>>  list = treeItem.getChildren().stream().filter(treeItem1 -> treeItem1.getValue().getTreeNodeType() == TreeNodeType.Function).collect(Collectors.toList());
        for (TreeItem<TreeNode> ti : list) {
            List<CommonFun> commonFuns = CommonFunDao.getByTreeNodeId(ti.getValue().getId());
            for(CommonFun commonFun : commonFuns) {
                Class clazz = MethodUtil.forClass(commonFun.getClassName());
                BeetlFactory.getInstance().addVarClass(commonFun.getName(), clazz.newInstance());
            }
        }
    }

    public static Map<String, Object> initRun(TreeItem<TreeNode> treeItem) throws Exception {
        Map<String, Object> paramMap = TreeUtil.buildParamMap(treeItem);
        Map<String, Object> childParamMap = TreeUtil.getParamMap(treeItem, paramMap);
        paramMap.putAll(childParamMap);

        VarUtil.cacheVar(treeItem); // 缓存变量
        loadFunction(treeItem);     // 加载公用方法
        return paramMap;
    }

    private static boolean runSqlData(TreeItem<TreeNode> treeItem, TestLog testLog, Map<String,Object> paramMap, Consumer<TreeItem<TreeNode>> handler, boolean isDebug) throws SQLException {
        List<SqlData> sqlDataList = SqlDataDao.getByTreeNodeId(treeItem.getValue().getId());

        List<Map<String, Object>> list = new ArrayList<>();
        for (SqlData sqlData : sqlDataList) {
            try{
                Map<String, Object> dataMap = new HashMap<>();
                ParamModel paramModel = sqlData.getParamModel();
                Connection conn = DbUtils.getConnection(paramModel);
                if (sqlData.getDataType() == SqlDataType.sql) {
                    Resource rc = new InputStreamResource(new ByteArrayInputStream(sqlData.getData().getBytes("utf-8")));
                    ScriptUtils.executeSqlScript(conn, rc);

                    dataMap.put("sql", sqlData.getData());
                } else if (sqlData.getDataType() == SqlDataType.file_sql) {
                    List<SqlData.SqlFileData> sqlFileDataList = (List<SqlData.SqlFileData>) sqlData.getObjectData();
                    for (SqlData.SqlFileData sqlFileData : sqlFileDataList) {
                        Resource rc = new InputStreamResource(new FileInputStream(new File(sqlFileData.getFile())));
                        ScriptUtils.executeSqlScript(conn, new EncodedResource(rc, Charset.forName("utf8")));
                    }
                    dataMap.put("file", JsonUtil.toJson(sqlFileDataList.stream().map(sqlFileData -> sqlFileData.getFile()).collect(Collectors.toList())));
                } else if (sqlData.getDataType() == SqlDataType.csv) {

                } else {
                    throw new RuntimeException("不支持这种类型:" + sqlData.getDataType());
                }

                DatabaseParam databaseParam = JsonUtil.toObject(paramModel.getValue(), DatabaseParam.class);
                dataMap.put("dbName", databaseParam.getDbName());
            } catch (Exception e) {
                setTreeNodeMark(treeItem, 2);
                LogUtil.log("初始化数据: 失败", 25, color(false));
                e.printStackTrace();
                return false;
            }
        }
        setTreeNodeMark(treeItem, 1);
        LogUtil.log("初始化数据:成功", 25, color(true));

        TestLogData testLogData = new TestLogData();
        testLogData.setTestLogId(testLog.getId());
        testLogData.setName(treeItem.getValue().getName());
        testLogData.setType(TestLogType.DataExport);
        testLogData.setContent(JsonUtil.toJson(list));
        TestLogDataDao.insert(testLogData);

        return true;
    }

    private static boolean runExportData(TreeItem<TreeNode> treeItem, TestLog testLog, Map<String,Object> paramMap, Consumer<TreeItem<TreeNode>> handler, boolean isDebug) throws SQLException {
        List<DataExport> dataExportList = DataExportDao.getByTreeNodeId(treeItem.getValue().getId());

        List<Map<String, Object>> list = new ArrayList();
        for (DataExport dataExport : dataExportList) {
            Map<String, Object> contentMap = new HashMap();
            Connection conn = null;
            try{
                ParamModel paramModel = dataExport.getDatabaseParamModel();
                conn = DbUtils.getConnection(paramModel);
                DatabaseParam databaseParam = JsonUtil.toObject(paramModel.getValue(), DatabaseParam.class);
                String dbName = databaseParam.getDbName();
                List<DataExportItem> dataExportItemList = JsonUtil.toObjectExpose(dataExport.getData(), new TypeToken<List<DataExportItem>>(){}.getType());
                int idx = dataExport.getName().lastIndexOf(".");
                String fileName = dataExport.getName();
                if (idx > 0) {
                    fileName = fileName.substring(0, idx);
                }

                File file = new File(dataExport.getDir() + File.separator + fileName + DateUtil.format(new Date(), "yyyyMMddHHmmss") + ".sql");
                while(!file.exists()) file.createNewFile();

                contentMap.put("dbName", dbName);
                contentMap.put("file", file.getPath());
                list.add(contentMap);

                Set<String> exportedTableSet = new HashSet<>();
//                Map<String, DataExportItem> notExportTableMap = new HashMap<>();
                Map<String, Map<String, String>> databaseMap = DatabaseUtil.getDatabaseInfo(dbName, conn);
                Map<String, List<String>> foreignMap = DatabaseUtil.getFeigonMaps(dbName, conn);
                for (DataExportItem item : dataExportItemList) {
                    if (item.getSelected() != null && item.getSelected()) {
                        String tableName = item.getTableName();
                        if (foreignMap.containsKey(tableName) && foreignMap.get(tableName).size() > 0) {
                            continue;
                        }
                        String sql = "select * from " + tableName;
                        if (!StringUtils.isEmpty(item.getWhere())) {
                            String where = item.getWhere().trim().toLowerCase().startsWith("where") ? item.getWhere() : "where " + item.getWhere();
                            sql = sql + " " + where;
                        }
                        exportTableData(conn, file, dbName, tableName, sql);
                        exportedTableSet.add(tableName);
                    }
                }

                boolean isEnd = false;
                for (int i=0; i<10; i++) {
                    if (i == 10) isEnd = true;
                    for (DataExportItem item : dataExportItemList) {
                        if (item.getSelected() != null && item.getSelected()) {
                            String tableName = item.getTableName();
                            if (exportedTableSet.contains(tableName)) continue;
                            if (!isEnd) {
                                int t = 0;
                                for (String ft : foreignMap.get(tableName)) {
                                    if (!exportedTableSet.contains(ft)) t++;
                                }
                                if (t > 0) continue;
                            }
                            String sql = "select * from " + tableName;
                            if (!StringUtils.isEmpty(item.getWhere())) {
                                String where = item.getWhere().trim().toLowerCase().startsWith("where") ? item.getWhere() : "where " + item.getWhere();
                                sql = sql + " " + where;
                            }
                            exportTableData(conn, file, dbName, tableName, sql);
                            exportedTableSet.add(tableName);
                        }
                    }
                }
            } catch (Exception e) {
                setTreeNodeMark(treeItem, 2);
                LogUtil.log("导出数据: 失败", 25, color(false));
                e.printStackTrace();
                return false;
            } finally {
                DbUtils.close(conn);
            }
        }
        setTreeNodeMark(treeItem, 1);
        LogUtil.log("导出数据:成功", 25, color(true));

        TestLogData testLogData = new TestLogData();
        testLogData.setTestLogId(testLog.getId());
        testLogData.setName(treeItem.getValue().getName());
        testLogData.setType(TestLogType.DataImport1);
        testLogData.setContent(JsonUtil.toJson(list));
        TestLogDataDao.insert(testLogData);

        return true;
    }

    private static void exportTableData(Connection conn, File file, String databaseName, String tableName, String sql) throws Exception {
        String textSql = DatabaseUtil.exportToSql(conn, databaseName, tableName, sql);
        Files.write(Paths.get(file.toURI()), textSql.getBytes("utf-8"), StandardOpenOption.APPEND);
    }

}
