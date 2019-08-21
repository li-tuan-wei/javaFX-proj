package ldh.common.testui.model;

import ldh.common.testui.constant.BeanValueType;
import ldh.common.testui.constant.CompareType;
import lombok.Data;

@Data
public class BeanData implements Comparable<BeanData> {

    private Integer index;             // 序号，对列表非常有用
    private String checkName;         // 检查对象
//    private String secondCheckName;
    private String exceptedValue;    // 期望值
    private Object value;             // 实际值
    private String desc;              // 检查描叙
    private String classType;        // 值类型
    private Boolean success;         // 是否成功
    private CompareType compareType;      // 比较方式
    private BeanValueType beanValueType;  // 对象值类型： 空对象，非空对象，列表为0
    private transient Object exceptedObjectValue;    // 期望值实际值

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof BeanData) || obj == null) return false;
        BeanData bc = (BeanData) obj;
        return bc.getIndex().equals(index);
    }

    @Override
    public int hashCode() {
        return index.hashCode();
    }

    @Override
    public int compareTo(BeanData o) {
        if (o == null) return 1;
        return this.getIndex().compareTo(o.getIndex());
    }
}
