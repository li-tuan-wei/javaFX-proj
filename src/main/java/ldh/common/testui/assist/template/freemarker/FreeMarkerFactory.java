package ldh.common.testui.assist.template.freemarker;

import com.jayway.jsonpath.JsonPath;
//import freemarker.cache.StringTemplateLoader;
//import freemarker.ext.beans.BeansWrapper;
//import freemarker.template.*;
import ldh.common.testui.assist.template.freemarker.function.LangUtil;
import ldh.common.testui.util.JsonUtil;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Created by ldh on 2018/4/11.
 */
public class FreeMarkerFactory {

    private final static Logger LOGGER = Logger.getLogger(FreeMarkerFactory.class.getSimpleName());

    private static FreeMarkerFactory instance = null;
//    private Configuration configuration = null;
//    private final static BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
//    private final static TemplateHashModel staticModels = wrapper.getStaticModels();

    public static FreeMarkerFactory getInstance() {
        if (instance == null) {
            synchronized (FreeMarkerFactory.class) {
                if (instance == null) {
                    instance = new FreeMarkerFactory();
                }
            }
        }
        return instance;
    }

    private FreeMarkerFactory() {
//        configuration = new Configuration(Configuration.VERSION_2_3_0);
//        configuration.setNumberFormat("#.00");
    }

//    public String process(String content, Map<String, Object> dataMap) throws Exception {
//        initJavaMethod(dataMap);
//        StringTemplateLoader stringLoader = new StringTemplateLoader();
//        configuration.setTemplateLoader(stringLoader);
//
//        stringLoader.putTemplate("template",content);
//        Template template = configuration.getTemplate("template","utf-8");
//        StringWriter writer = new StringWriter();
//        try {
//            template.process(dataMap, writer);
//        } catch (Exception e) {
//            LOGGER.info("process error: " + JsonUtil.toJson(filterParamMap(dataMap)));
//            throw new RuntimeException(e);
//        }
//
//        return writer.toString();
//    }

    private void initJavaMethod(Map<String, Object> dataMap) {
        initMethod(dataMap, "Random", ldh.common.testui.assist.template.freemarker.function.RandomUtil.class);
        initMethod(dataMap, "Sql", ldh.common.testui.assist.template.freemarker.function.SqlUtil.class);
        initMethod(dataMap, "Method", ldh.common.testui.assist.template.freemarker.function.MethodUtil.class);
        initMethod(dataMap, "Json", ldh.common.testui.assist.template.freemarker.function.JsonHelp.class);
        initMethod(dataMap, "JsonPath", JsonPath.class);
        initMethod(dataMap, "Lang", LangUtil.class);
    }

    private void initMethod(Map<String, Object> dataMap, String name, Class clazz) {
//        try {
//            TemplateHashModel randomStatics = (TemplateHashModel) staticModels.get(clazz.getName());
//            dataMap.putIfAbsent(name, randomStatics);
//        } catch (TemplateModelException e) {
//            e.printStackTrace();
//        }
    }

    public static Map<String, Object> filterParamMap(Map<String, Object> paramMap) {
        List<String> keys = Arrays.asList("Random", "Sql", "Method", "Json", "JsonPath", "Lang", "-beanVar-");
        Map<String, Object> result = new HashMap();
        for(Map.Entry<String, Object> entry : paramMap.entrySet()) {
            if(keys.contains(entry.getKey())) continue;
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

}
