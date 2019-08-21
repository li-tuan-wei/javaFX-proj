package ldh.common.testui.constant;

import ldh.common.testui.vo.*;

/**
 * Created by ldh on 2018/3/17.
 */
public enum ParamCategory {
    Constant("常量", null),
    Spring("spring配置", new SpringParam()),
    Database("数据库配置", new DatabaseParam("", "", "", "com.mysql.jdbc.Driver", "")),
    Method_Package("测试类路径", null),
//    Sql("SQL", new SqlParam()),
    Other_jar("增加外部类", null),
    Increment("递增变量", new IncrementParam()),
    ;

    private String desc;
    private ParamInterface paramInterface;

    private ParamCategory(String desc, ParamInterface paramInterface) {
        this.desc = desc;
        this.paramInterface = paramInterface;
    }

    public String getDesc() {
        return desc;
    }

    public ParamInterface getParamInterface() {
        return paramInterface;
    }
}
