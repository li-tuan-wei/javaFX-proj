package el;

import ldh.common.testui.assist.template.beetl.BeetlFactory;
import ldh.common.testui.assist.template.freemarker.FreeMarkerFactory;
import ldh.common.testui.assist.template.freemarker.function.LangUtil;
import ldh.common.testui.demo.Student;
import ldh.common.testui.util.DateUtil;
import ldh.common.testui.util.JsonUtil;
import org.junit.Test;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by ldh on 2018/4/10.
 */
public class BeetlTest2 {

    @Test
    public void replace() throws Exception {
        String json = "{\n" +
                "  \"borrowUserId\": 1283972,\n" +
                "  \"borrowerRealName\": \"阿斯顿发\",\n" +
                "  \"borrowerFullPhone\": \"13439054927\",\n" +
                "  \"borrowerFullIdCard\": \"131024198910160057\",\n" +
                "  \"productId\": \"BL-TCXF\",\n" +
                "  \"assetId\": 1,\n" +
                "  \"transactionId\": \"${transactionId}\",\n" +
                "  \"name\": \"${name}\",\n" +
                "  \"amount\": ${amount},\n" +
                "  \"maturity\": ${period} ,\n" +
                "  \"userRate\": ${rate},\n" +
                "  \"projectExtra\": {\n" +
                "    \"firstPayDate\": \"${Random.formatNow('yyyy-MM-dd', 30)}\",\n" +
                "    \"payGoodsFeeDate\": \"2017-05-15\",\n" +
                "    \"transfer\": \"Not_Transfer\",\n" +
                "    \"merchantRate\": 0.001,\n" +
                "    \"merchantType\": \"None\"\n" +
                "  }\n" +
                "}";

//        Configuration cfg = new Configuration(Configuration.VERSION_2_3_0);
//        StringTemplateLoader stringLoader = new StringTemplateLoader();
//        cfg.setTemplateLoader(stringLoader);
//
//        stringLoader.putTemplate("myTemplate",json);
//        Template template = cfg.getTemplate("myTemplate","utf-8");
        Map root = new HashMap();
        root.put("transactionId", "javaboy2012");
        root.put("name", 1231313);
        root.put("period", 6);
        root.put("rate", 0.75);
        root.put("amount", 1000);

//        String result = FreeMarkerFactory.getInstance().process(json, root);
//        System.out.println(result);
        String result2 = BeetlFactory.getInstance().process(json, root);
        System.out.println(result2);
    }

    @Test
    public void replace2() throws Exception {
        String json = "{\n" +
                "  \"borrowUserId\": 1283972,\n" +
                "  \"borrowerRealName\": \"阿斯顿发\",\n" +
                "  \"borrowerFullPhone\": \"13439054927\",\n" +
                "  \"borrowerFullIdCard\": \"131024198910160057\",\n" +
                "  \"productId\": \"BL-TCXF\",\n" +
                "  \"assetId\": 1,\n" +
                "  \"transactionId\": \"#transactionId\",\n" +
                "  \"name\": \"#name\",\n" +
                "  \"amount\": #amount,\n" +
                "  \"maturity\": #period ,\n" +
                "  \"userRate\": #rate,\n" +
                "  \"projectExtra\": {\n" +
//                "    \"firstPayDate\": \"${Random.formatNow('yyyy-MM-dd', 30)}\",\n" +
                "    \"payGoodsFeeDate\": \"2017-05-15\",\n" +
                "    \"transfer\": \"Not_Transfer\",\n" +
                "    \"merchantRate\": 0.001,\n" +
                "    \"merchantType\": \"None\"\n" +
                "  }\n" +
                "}";

//        Configuration cfg = new Configuration(Configuration.VERSION_2_3_0);
//        StringTemplateLoader stringLoader = new StringTemplateLoader();
//        cfg.setTemplateLoader(stringLoader);
//
//        stringLoader.putTemplate("myTemplate",json);
//        Template template = cfg.getTemplate("myTemplate","utf-8");
        Map root = new HashMap();
        root.put("transactionId", "javaboy2012");
        root.put("name", 1231313);
        root.put("period", 6);
        root.put("rate", 0.75);
        root.put("amount", 1000);

//        String result = FreeMarkerFactory.getInstance().process(json, root);
//        System.out.println(result);
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(root);
        Expression exp = parser.parseExpression(json);
        Object object = exp.getValue(context);
        System.out.println(JsonUtil.toJson(object));
    }

    @Test
    public void vv() throws Exception {
        String content = "${Date.format(Date.addMonth(Date.parse(firstPayDay, 'yyyy-MM-dd'),1), 'yyyy-MM-dd')}";
        Map root = new HashMap();
        root.put("firstPayDay", "2018-12-23");
        String value2 = BeetlFactory.getInstance().process(content, root);
        System.out.println("value:" + value2);
    }

    @Test
    public void vv2() throws Exception {
        String content = "${amount * rate/100.0 * changeDay/30.0}";
        Map root = new HashMap();
        root.put("amount", BigDecimal.valueOf(600));
        root.put("rate", BigDecimal.valueOf(1.65));
        root.put("changeDay", 30);

//        String value = FreeMarkerFactory.getInstance().process(content, root);
        String value2 = BeetlFactory.getInstance().process(content, root);
        System.out.println("value:" + value2 + ", value2:" + value2);
    }

    @Test
    public void vv3() throws Exception {
//        String content = "${Lang.scale((amount - refundAmount + feeAmount) * rate/100 * (30-changeDay)/30, 2)}";
        String content = "$((amount-refundAmount-refundAmount2 + feeAmount)*rate/100*(30-changeDay)/30}";
        Map root = new HashMap();
        root.put("amount", BigDecimal.valueOf(3000));
        root.put("rate", BigDecimal.valueOf(1.20));
        root.put("changeDay", 5);
        root.put("changeDay3", 10);
        root.put("refundAmount", BigDecimal.valueOf(100));
        root.put("refundAmount2", BigDecimal.valueOf(5));
        root.put("refundAmount3", BigDecimal.valueOf(5));
        root.put("feeAmount", BigDecimal.valueOf(5.58));
        String value2 = BeetlFactory.getInstance().process(content, root);
        System.out.println("value:" + value2);
    }

    @Test
    public void vv4() throws Exception {
        String content = "${Date.formatNow('yyyy-MM-dd', 0, 1)}";
        Map root = new HashMap();
        root.put("amount", BigDecimal.valueOf(600));
        root.put("rate", BigDecimal.valueOf(1.65));
        root.put("changeDay", 30);

//        String value = FreeMarkerFactory.getInstance().process(content, root);
        String value2 = BeetlFactory.getInstance().process(content, root);
        System.out.println("value:" + value2 + ", value2:" + value2);

        content = "${date}";
        root = new HashMap();
        root.put("date", new Date());
        value2 = BeetlFactory.getInstance().process(content, root);
        System.out.println("value:" + value2 + ", value2:" + value2);

        String cc = "${Lang.max(1, 10)}";
        value2 = BeetlFactory.getInstance().process(cc, root);
        System.out.println("value:" + value2 + ", value2:" + value2);

        cc = "${Lang.scale(amount * rate/100 * Lang.min(changeDay, 30)/30, 2)}";
        root.put("amount", 1000);
        root.put("rate", 1.56);
        root.put("changeDay", 31);
        value2 = BeetlFactory.getInstance().process(cc, root);
        System.out.println("value:" + value2 + ", value2:" + value2);
    }

    @Test
    public void el5() throws Exception {
        String content = "<%\n" +
                "var a = \"sdfas\";\n" +
                "var c = ${Random.formatNow('yyyy-MM-dd', 30)}; //应该是var c = a+\"beetl\"\n" +
                "var result = a+c;" +
                "%>" +
                "hello 2+3=${result}";
        Map root = new HashMap();
        root.put("amount", BigDecimal.valueOf(600));
        root.put("rate", BigDecimal.valueOf(1.65));
        root.put("changeDay", 30);
        String value2 = BeetlFactory.getInstance().process(content, root);
        System.out.println("value:" + value2);
    }

    @Test
    public void vv31() throws Exception {
//        String content = "${Lang.scale((amount - refundAmount + feeAmount) * rate/100 * (30-changeDay)/30, 2)}";
        String content = "#((amount-refundAmount-refundAmount2 + feeAmount)*rate/100*(30-changeDay)/30}";
        Map root = new HashMap();
        root.put("amount", BigDecimal.valueOf(3000));
        root.put("rate", BigDecimal.valueOf(1.20));
        root.put("changeDay", 5);
        root.put("changeDay3", 10);
        root.put("refundAmount", BigDecimal.valueOf(100));
        root.put("refundAmount2", BigDecimal.valueOf(5));
        root.put("refundAmount3", BigDecimal.valueOf(5));
        root.put("feeAmount", BigDecimal.valueOf(5.58));
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(root);

        String content2 = "(#amount-#refundAmount-#refundAmount2 + #feeAmount)*#rate/100*(30-#changeDay)/30";
        Expression exp = parser.parseExpression(content2);
        Object object = exp.getValue(context);
        System.out.println("value:" + object);
    }

    @Test
    public void el6() {
        List<Student> studentList = buildStudentList(10);
        Map<String, Object> root = new HashMap();
        root.put("studentList", studentList);
        root.put("name", "test 0");
        String content = "#studentList[0].name == #name";
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(root);
        Expression exp = parser.parseExpression(content);
        Object object = exp.getValue(context);
        System.out.println(object);
    }

    @Test
    public void el7() throws NoSuchMethodException {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        Map<String, Object> root = new HashMap();
        root.put("a1", "123.00");
        root.put("a2", "100");

        context.setVariables(root);

        Method method = LangUtil.class.getDeclaredMethod("subtract", new Class[] { String.class, String.class });
        context.registerFunction("subtract",method);
        context.registerMethodFilter(LangUtil.class, (methods)->{
            return methods;
        });

        String content = "#subtract(#a1, #a2)";
        BigDecimal amount = parser.parseExpression(content).getValue(context, BigDecimal.class);
        System.out.println(amount);
    }

    private List<Student> buildStudentList(int size) {
        List<Student> result = new ArrayList();
        for(int i=0; i<size; i++) {
            result.add(buildStudent(i));
        }
        return result;
    }

    private Student buildStudent(int i) {
        Student student = new Student();
        student.setId(i);
        student.setName("test " + i);
        student.setAge((short) 12);
        return student;
    }
}
