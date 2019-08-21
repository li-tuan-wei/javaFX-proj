package ldh.common.testui.constant;

/**
 * Created by ldh on 2018/3/27.
 */
public enum CompareType {
    Equal("相等"),
    Regix("正则表达式"),
    Contain("包含"),

    ;

    private String desc;
    private CompareType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
