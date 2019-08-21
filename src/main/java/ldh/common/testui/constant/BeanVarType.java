package ldh.common.testui.constant;

/**
 * Created by ldh on 2018/3/27.
 */
public enum BeanVarType {
    Sql("Sql"),
    Method("方法"),
    Clazz("类"),

    ;

    private String desc;
    private BeanVarType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
