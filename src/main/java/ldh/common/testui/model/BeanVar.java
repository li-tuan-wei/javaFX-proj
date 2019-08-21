package ldh.common.testui.model;

import ldh.common.testui.constant.BeanVarType;
import ldh.common.testui.constant.InstanceClassType;
import ldh.common.testui.util.DataUtil;
import ldh.common.testui.util.MethodUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

/**
 * Created by ldh on 2018/4/1.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BeanVar {

    private Integer id;
    private Integer index;
    private Integer treeNodeId;
    private String name;
    private BeanVarType type;
    private Integer databaseParamId;
    private String sql;
    private String args;

    private Integer packageParamId;
    private String className;
    private InstanceClassType instanceClassType;
    private transient Method method;
    private String methodName;

    public void setMethodName(String methodName) {
        this.methodName = methodName;
        try {
            if (methodName != null && !methodName.equals("")) {
                method = MethodUtil.forMethod(Class.forName(className), methodName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getDatabaseParam() {
        if (databaseParamId != null) {
           return DataUtil.getById(databaseParamId).getName();
        }
        return "";
    }

    public String getPackageParam() {
        if (packageParamId != null) {
            if (packageParamId == -1) return "基础类型";
            return DataUtil.getById(packageParamId).getName();
        }
        return "";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof BeanVar)) return false;
        BeanVar pm = (BeanVar) obj;
        return pm.getId().equals(this.getId());
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }
}
