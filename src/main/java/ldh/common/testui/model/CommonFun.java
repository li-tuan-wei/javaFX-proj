package ldh.common.testui.model;

import ldh.common.testui.util.DataUtil;
import lombok.Data;

@Data
public class CommonFun {

    private Integer id;
    private String name;
    private Integer treeNodeId;
    private Integer packageParamId;
    private String className;
    private String desc;

    private transient ParamModel paramModel = null;

    public String getPackageParam() {
        if (paramModel != null) {
            return paramModel.getName();
        }
        if (packageParamId != null) {
            if (packageParamId == -1) return "基础类型";
            paramModel = DataUtil.getById(packageParamId);
            return paramModel.getName();
        }
        return "";
    }

    public ParamModel getParamModel() {
        if (paramModel == null) {
            paramModel = DataUtil.getById(packageParamId);
        }
        return paramModel;
    }

}
