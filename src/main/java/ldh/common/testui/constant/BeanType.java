package ldh.common.testui.constant;

/**
 * Created by ldh on 2018/3/27.
 */
public enum BeanType {
    String("字符串"),
    Json("Json"),
    Object("PoJo对象"),
    EL("EL表达式"),
    ;

    private String desc;
    private BeanType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
