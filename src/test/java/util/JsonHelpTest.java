package util;

import com.google.gson.reflect.TypeToken;
import ldh.common.testui.assist.template.freemarker.function.JsonHelp;
import ldh.common.testui.constant.CompareType;
import ldh.common.testui.model.BeanCheck;
import ldh.common.testui.swagger.Swagger;
import ldh.common.testui.util.DateUtil;
import ldh.common.testui.util.JsonUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by ldh on 2018/11/30.
 */
public class JsonHelpTest {

    @Test
    public void intTest() {
        Integer t = (Integer) JsonHelp.toBean("3", Integer.class);
        Assert.assertEquals(t.longValue(), 3L);

        List<Integer> list = (List<Integer>) JsonHelp.toBean("[12,23,45]", new TypeToken<List<Integer>>() {}.getType());
        list.stream().forEach(e->System.out.println(e));
    }

    @Test
    public void map() {
        Map<String, Object> map = new HashMap();
        map.put("test", 123);
        map.put("test", 234243);
        System.out.println(map.get("test"));

        String name = "{{name }}";
        String key = name.substring(2, name.length() - 2);
        System.out.println("key: " + key + "adas");
    }

    @Test
    @Ignore
    public void var() {
        String ext = "[a-zA-Z_$][a-zA-Z0-9_$]*";
        String str = "${Lang.scale((amount - (refundAmount -originPri-originInt-feeAmount)) * rate/100 * (60-changeDay)/30, 2)}";
        Pattern pattern = Pattern.compile(ext);
        Matcher matcher = pattern.matcher(str);
        while(matcher.find()) {
            String code = matcher.group(0);
            System.err.println("bbbb:" + code);
        }
    }

    @Test
    public void date() {
        CompareType compareType = null;
        System.out.println(compareType == CompareType.Equal);
    }

    @Test
    public void json() {
        Map<String, String> dd = new HashMap<>();
        dd.put("dd", "dsasa");
        dd.put("test", "${tt}");
        String json = JsonUtil.toJson(dd);
        json = "{\n" +
                "    \"id\": 37,\n" +
                "    \"checkName\": \"${tt}\",\n" +
                "    \"beanType\": \"Object\",\n" +
                "    \"otherInfo\": \"{}\",\n" +
                "    \"treeNodeId\": 289,\n" +
                "    \"columns\": \"getId,getStudent,getName,getAge\",\n" +
                "    \"content\": \"[\\n  {\\n    \\\"getAge\\\": {\\n      \\\"index\\\": 4,\\n      \\\"checkName\\\": \\\"getAge\\\",\\n      \\\"secondCheckName\\\": null,\\n      \\\"exceptedValue\\\": \\\"23\\\",\\n      \\\"value\\\": 23,\\n      \\\"desc\\\": null,\\n      \\\"classType\\\": \\\"java.lang.Short\\\",\\n      \\\"success\\\": true,\\n      \\\"compareType\\\": \\\"Equal\\\"\\n    },\\n    \\\"getName\\\": {\\n      \\\"index\\\": 4,\\n      \\\"checkName\\\": \\\"getName\\\",\\n      \\\"secondCheckName\\\": null,\\n      \\\"exceptedValue\\\": \\\"asd2\\\",\\n      \\\"value\\\": \\\"asd2\\\",\\n      \\\"desc\\\": null,\\n      \\\"classType\\\": \\\"java.lang.String\\\",\\n      \\\"success\\\": true,\\n      \\\"compareType\\\": \\\"Equal\\\"\\n    },\\n    \\\"getId\\\": {\\n      \\\"index\\\": 4,\\n      \\\"checkName\\\": \\\"getId\\\",\\n      \\\"secondCheckName\\\": null,\\n      \\\"exceptedValue\\\": \\\"2\\\",\\n      \\\"value\\\": 2,\\n      \\\"desc\\\": null,\\n      \\\"classType\\\": \\\"java.lang.Integer\\\",\\n      \\\"success\\\": true,\\n      \\\"compareType\\\": \\\"Equal\\\"\\n    },\\n    \\\"getStudent\\\": {\\n      \\\"index\\\": 4,\\n      \\\"checkName\\\": \\\"getStudent\\\",\\n      \\\"secondCheckName\\\": null,\\n      \\\"exceptedValue\\\": \\\"\\\",\\n      \\\"value\\\": null,\\n      \\\"desc\\\": null,\\n      \\\"classType\\\": \\\"ldh.common.testui.demo.Student\\\",\\n      \\\"success\\\": null,\\n      \\\"compareType\\\": \\\"Equal\\\"\\n    }\\n  }\\n]\"\n" +
                "  }";
        BeanCheck ttt = JsonUtil.toObject(json, BeanCheck.class);
        System.out.println(ttt.getCheckName());
    }

    @Test
    public void swagger() throws Exception {
        List<String> strings = Files.readAllLines(Paths.get(JsonHelpTest.class.getResource("/swagger.json").toURI()));
        String content = strings.stream().collect(Collectors.joining());
        Swagger swagger = JsonUtil.toObject(content, Swagger.class);
        System.out.println(swagger.getPaths().size());
    }
}
