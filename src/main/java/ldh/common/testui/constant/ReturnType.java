package ldh.common.testui.constant;

/**
 * Created by ldh on 2018/3/27.
 */
public enum ReturnType {
    Enum("枚举"),
    Array("数组"),
    List("列表"),
    Set("Set列表"),
    Map("哈希表"),
    ListMap("列表套哈希"),
    Common("常用类型"),
    Bean("普通对象"),
    TBean("泛型对象"),

    ;

    private String desc;
    private ReturnType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
