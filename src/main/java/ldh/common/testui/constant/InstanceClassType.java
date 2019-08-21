package ldh.common.testui.constant;

/**
 * Created by ldh on 2018/3/29.
 */
public enum InstanceClassType {
    Reflect("反射"),
    Spring("Spring容器"),

    ;

    private String desc;
    private InstanceClassType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }


}
