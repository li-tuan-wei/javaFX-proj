package ldh.common.testui.assist.template.el;

import ldh.common.testui.assist.template.beetl.BeetlFactory;
import ldh.common.testui.assist.template.beetl.function.*;
import ldh.common.testui.util.JsonUtil;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by ldh on 2019/1/10.
 */
public class ElFactory {

    private final static Logger LOGGER = Logger.getLogger(ElFactory.class.getSimpleName());

    private static ElFactory instance = null;

    public static ElFactory getInstance() {
        if (instance == null) {
            synchronized (BeetlFactory.class) {
                if (instance == null) {
                    instance = new ElFactory();
                }
            }
        }
        return instance;
    }

    private ElFactory() {
    }

    public Object process(String content, Map<String, Object> dataMap) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(dataMap);
        try {
            Expression exp = parser.parseExpression(content);
            Object object = exp.getValue(context);
            return object;
        } catch (Exception e) {
            LOGGER.info("process error: " + JsonUtil.toJson(dataMap));
            throw new RuntimeException(e);
        }
    }

    private void initJavaMethod(StandardEvaluationContext context) {
        addObject(context,"Json", new JsonHelp());
//        groupTemplate.registerFunctionPackage("JsonPath", new JsonPath());
        addObject(context,"Random", new RandomHelp());
        addObject(context,"Sql", new SqlHelp());
        addObject(context,"Method", new MethodHelp());
        addObject(context,"Lang", new LangHelp());
        addObject(context,"Date", new DateHelp());
    }

    private void addObject(StandardEvaluationContext context, String name, Object bean) {
        context.registerMethodFilter(bean.getClass(), (methods) -> methods);

//        methodSet.add(name);
//        List<Method> methods = MethodUtil.getMethods(bean.getClass());
//        for(Method method : methods) {
//            methodSet.add(method.getName());
//        }
    }

}
