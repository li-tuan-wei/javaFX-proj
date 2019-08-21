package ldh.common.testui.assist.template.beetl;

import ldh.common.testui.assist.template.beetl.function.*;
import ldh.common.testui.model.BeanVar;
import ldh.common.testui.util.JsonUtil;
import ldh.common.testui.util.MethodUtil;
import ldh.common.testui.util.VarUtil;
import org.beetl.core.Configuration;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.resource.StringTemplateResourceLoader;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by ldh on 2018/12/25.
 */
public class BeetlFactory {

    private final static Logger LOGGER = Logger.getLogger(BeetlFactory.class.getSimpleName());

    private static BeetlFactory instance = null;
    private StringTemplateResourceLoader resourceLoader = new StringTemplateResourceLoader();
    private Configuration configuration = null;

    private Map<String, Object> varClassMap = new HashMap();

    private Set<String> methodSet = new HashSet();

    public static BeetlFactory getInstance() {
        if (instance == null) {
            synchronized (BeetlFactory.class) {
                if (instance == null) {
                    instance = new BeetlFactory();
                }
            }
        }
        return instance;
    }

    private BeetlFactory() {
        try {
            configuration = Configuration.defaultConfiguration();
//            process("", new HashMap());
            initJavaMethod(null);
        } catch (Exception e) {
            LOGGER.warning(e.getMessage());
        }
    }

    public String process(String content, Map<String, Object> dataMap) throws Exception {
        Set<String> elVarNames = VarUtil.getElVarNames(content);
        Map<String, BeanVar> beanVarMap = (Map<String, BeanVar>) dataMap.get("-beanVar-");
        if (beanVarMap != null) {
            for(String elVarName : elVarNames) {
                if (beanVarMap.containsKey(elVarName)) {
                    VarUtil.vars(elVarName, dataMap);
                }
            }
        }

        GroupTemplate groupTemplate = new GroupTemplate(resourceLoader, configuration);
        Template template = groupTemplate.getTemplate(content);
        StringWriter writer = new StringWriter();
        try {
            initJavaMethod(groupTemplate);
            template.binding(dataMap);
            template.renderTo(writer);
        } catch (Exception e) {
            LOGGER.info("process error: " + JsonUtil.toJson(dataMap));
            throw new RuntimeException(e);
        }

        return writer.toString();
    }

    public void addVarClass(String name, Object bean) {
        varClassMap.put(name, bean);

        methodSet.add(name);
        List<Method> methods = MethodUtil.getMethods(bean.getClass());
        for(Method method : methods) {
            methodSet.add(method.getName());
        }
    }

    private void initJavaMethod(GroupTemplate groupTemplate) {
        addObject(groupTemplate,"Json", new JsonHelp());
//        groupTemplate.registerFunctionPackage("JsonPath", new JsonPath());
        addObject(groupTemplate,"Random", new RandomHelp());
        addObject(groupTemplate,"Sql", new SqlHelp());
        addObject(groupTemplate,"Method", new MethodHelp());
        addObject(groupTemplate,"Lang", new LangHelp());
        addObject(groupTemplate,"Date", new DateHelp());

        varClassMap.forEach((key, value)->{
            addObject(groupTemplate,key, value);
        });
    }

    private void addObject(GroupTemplate groupTemplate, String name, Object bean) {
        if (groupTemplate != null) {
            groupTemplate.registerFunctionPackage(name, bean);
        }

        methodSet.add(name);
        List<Method> methods = MethodUtil.getMethods(bean.getClass());
        for(Method method : methods) {
            methodSet.add(method.getName());
        }
    }

    public Set<String> getCommonMethodNames() {
        return methodSet;
    }

}
