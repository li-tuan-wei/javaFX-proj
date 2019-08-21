package util;

import ldh.common.testui.assist.template.beetl.BeetlFactory;
import ldh.common.testui.util.JsonUtil;
import ldh.common.testui.vo.JsonPathHelp;
import ldh.common.testui.vo.JsonRootHelp;
import org.beetl.ext.fn.Json;
import org.junit.Test;

import java.util.Map;
import java.util.HashMap;

/**
 * Created by ldh on 2019/1/24.
 */
public class JsonPathHelpTest {

    @Test
    public void jsonTest() {
        Map<String, Object> map = new HashMap();
        map.put("result", 1);
        map.put("reason", null);
        map.put("success", true);

        Map<String, Object> content = new HashMap();
        content.put("assetId", 1);
        content.put("transactionId", "autotest_20190123181118");
        content.put("vendor", "BBNet");

        Map<String, Object> demo = new HashMap();
        demo.put("id", 1);
        content.put("demo", JsonUtil.toJson(demo));
        map.put("content", JsonUtil.toJson(content));

        String json = JsonUtil.toJson(map);

        System.out.println("json:" + json);
        String jsonPath = "Json.toJson(Json.toJson($.content).demo).id";
        JsonPathHelp jsonPathHelp = new JsonPathHelp(json, jsonPath);
        Integer assertId = jsonPathHelp.jsonValue(Integer.class);
        System.out.println("value:" + assertId);
    }

    @Test
    public void jsonTest2() throws Exception {
        Map<String, Object> map = new HashMap();
        map.put("result", 1);
        map.put("reason", null);
        map.put("success", true);

        Map<String, Object> content = new HashMap();
        content.put("assetId", 1);
        content.put("transactionId", "autotest_20190123181118");
        content.put("vendor", "BBNet");

        Map<String, Object> demo = new HashMap();
        demo.put("id", 1);
        content.put("demo", JsonUtil.toJson(demo));
        map.put("content", JsonUtil.toJson(content));

        String json = JsonUtil.toJson(map);

        System.out.println("json:" + json);
        String jsonPath = "${2 + R.value(Json.toJson(Json.toJson(content).demo).id)}";
        JsonRootHelp jsonRootHelp = new JsonRootHelp(json, jsonPath);
        String expression = jsonRootHelp.parse();
        System.out.println("value:" + expression);

        String tt = "${Sql.sql(R.value(data))}";
        Map<String, Object> map2 = new HashMap();
        map2.put("data", "sdfsafas");
        JsonRootHelp help = new JsonRootHelp(JsonUtil.toJson(map2), tt);
        String t = help.parse();
        System.out.println("tt:" +t);
        Map<String, Object> paramMap = new HashMap();
        paramMap.put("_r", "sdfadfassd");
        String ttt = BeetlFactory.getInstance().process(t, paramMap);
        System.out.println("tt:" +ttt);

        String el = "${CommonHelp.aesDecode(Json.jsonPathValue(result, 'data')) == 'hello world'}";
//        el = "${Json.jsonPathValue(result, 'data') == 'hello world'}";
        Map<String, Object> paramMap2 = new HashMap();
        paramMap2.put("data", "xxTjvIA0KonkRjSxkEcqLrA9xi/OGnI00N2Tu9t9XqU=");
        String result = JsonUtil.toJson(paramMap2);
        paramMap.put("result", result);
//        BeetlFactory.getInstance().addVarClass("CommonHelp", new CommonHelp())
        String elValue = BeetlFactory.getInstance().process(el, paramMap);
        System.out.println("elValue:" +elValue);
    }
}
