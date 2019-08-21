package ldh.common.testui.util;

import ldh.common.testui.handle.RunTreeItem;
import ldh.common.testui.model.TestLog;
import ldh.common.testui.model.TreeNode;

import java.util.HashMap;
import java.util.Map;

public class DebugCacheFactory {

    private Map<TreeNode, Map<String, Object>> cacheMap = new HashMap<>();
    private Map<TreeNode, TestLog> testLogCacheMap = new HashMap<>();

    private volatile boolean isDebug = false;

    private static DebugCacheFactory instance = null;

    public static DebugCacheFactory getInstance() {
        if (instance == null) {
            synchronized (DebugCacheFactory.class) {
                if(instance == null) {
                    instance = new DebugCacheFactory();
                }
            }
        }
        return instance;
    }

    public void cache(TreeNode treeNode, Map<String, Object> paramMap) {
        cacheMap.put(treeNode, paramMap);
    }

    public void cache(TreeNode treeNode, TestLog testLog) {
        testLogCacheMap.put(treeNode, testLog);
    }

    public Map<String, Object> getCache(TreeNode treeNode) {
        if (!cacheMap.containsKey(treeNode)) {
            throw new RuntimeException("缓存不存在，key:" + treeNode.getName());
        }
        return cacheMap.get(treeNode);
    }

    public TestLog getTestLog(TreeNode treeNode) {
        if (!cacheMap.containsKey(treeNode)) {
            throw new RuntimeException("缓存不存在，key:" + treeNode.getName());
        }
        return testLogCacheMap.get(treeNode);
    }

    public boolean contain(TreeNode treeNode) {
        return cacheMap.containsKey(treeNode);
    }

    public void clean() {
        cacheMap.clear();
        testLogCacheMap.clear();
        isDebug = false;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void debug(boolean isDebug) {
        this.isDebug = isDebug;
        if (!isDebug) {
            clean();
        }
    }
}
