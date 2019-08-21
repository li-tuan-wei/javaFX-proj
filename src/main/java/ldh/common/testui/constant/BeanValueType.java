package ldh.common.testui.constant;

/**
 * Created by ldh on 2019/1/11.
 */
public enum BeanValueType {
    Not_null("非空对象"),
    Null("空对象"),
    Empty("队列长度为0"),
    ;

    private String desc;
    private BeanValueType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
