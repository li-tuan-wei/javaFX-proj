package el;

import ldh.common.testui.assist.template.freemarker.FreeMarkerFactory;
import ldh.common.testui.util.VarUtil;
import org.junit.Test;

import java.io.StringWriter;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by ldh on 2018/4/10.
 */
public class BeetlTest {

    @Test
    public void replace() throws Exception {
        String json = "{   \n" +
                "         \"borrowUserId\":1283972,\n" +
                "         \"borrowerRealName\":\"阿斯顿发\",\n" +
                "         \"borrowerFullPhone\":\"13439054927\",\n" +
                "         \"borrowerFullIdCard\":\"131024198910160057\",\n" +
                "         \"productId\":\"BL-ZPWXF\",\n" +
                "         \"assetId\":1,\n" +
                "         \"transactionId\":\"${transactionId}\",\n" +
                "         \"name\":\"项目名称1121001\",\n" +
                "         \"amount\":3000,\n" +
                "         \"maturity\":3,\n" +
                "         \"userRate\":0.9,\n" +
                "         \"projectExtra\": {\n" +
                "    \"firstPayDate\":\"2017-12-20\",\n" +
                "    \"payGoodsFeeDate\":\"2017-05-15\",\n" +
                "    \"transfer\":\"Not_Transfer\",\n" +
                "            \"merchantRate\":0.001,\n" +
                "    \"merchantType\":\"adsfa\" \n" +
                "      }\n" +
                "}";

//        Configuration cfg = new Configuration(Configuration.VERSION_2_3_0);
//        StringTemplateLoader stringLoader = new StringTemplateLoader();
//        cfg.setTemplateLoader(stringLoader);
//
//        stringLoader.putTemplate("myTemplate",json);
//        Template template = cfg.getTemplate("myTemplate","utf-8");
//        Map root = new HashMap();
//        root.put("transactionId", "javaboy2012");
//
//        FreeMarkerFactory.getInstance().process(json, root);
//        StringWriter writer = new StringWriter();
//        template.process(root, writer);
//        System.out.println(writer.toString());
//
//        stringLoader = new StringTemplateLoader();
//        stringLoader.putTemplate("myTemplate", "hello: ${transactionId}");
//        cfg.setTemplateLoader(stringLoader);
//
//        writer = new StringWriter();
//        template = cfg.getTemplate("myTemplate","utf-8");
//        template.process(root, writer);
//        System.out.println(writer.toString());
    }

    @Test
    public void test() {
        String t = "\t\tadfasdfasfa";
        String str = t;
        int c = 0;
        while(str.startsWith("\t")) {
            str = str.substring(1);
            c+=50;
        }
        System.out.println("cc:" + c);
    }

    @Test
    public void el() {
        String tt = "asdfaf('adfa', 'adfaf')";
        String t = VarUtil.removeParam(tt);
        System.out.println("tt:" + t);
    }
}
