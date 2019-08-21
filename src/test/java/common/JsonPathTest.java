package common;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import ldh.common.testui.util.JsonUtil;
import org.junit.Test;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;

/**
 * Created by ldh on 2018/4/25.
 */
public class JsonPathTest {

    @Test
    public void jsonTest() {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("test", 123);
        dataMap.put("test2", "hello");
        String json = JsonUtil.toJson(dataMap);
        Map<String, Object> dataMap3 = new HashMap<>();
        dataMap3.put("hht", "dasfasd");
        dataMap3.put("hh2", "1111");
        dataMap3.put("hh", json);
        String json2 = JsonUtil.toJson(dataMap3);
        System.out.println(json2);

        Object document = Configuration.defaultConfiguration().jsonProvider().parse(json2);
        String hh3 = JsonPath.read(document, "$.hh");
        System.out.println("hh3:" + hh3);
        String test2 = JsonPath.parse(hh3).read("$.test2");
        Integer test = JsonPath.parse(hh3).read("$.test");
        System.out.println("test2:" + test2 + ",test:" + test);

    }

    public void threadTest() {

    }
}
