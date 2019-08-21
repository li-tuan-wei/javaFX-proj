package ldh.common.testui.vo;

import javafx.scene.control.TreeItem;
import ldh.common.testui.dao.IncrementVarDao;
import ldh.common.testui.model.IncrementVar;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.JsonUtil;
import lombok.Data;

/**
 * Created by ldh on 2018/3/28.
 */
@Data
public class IncrementParam extends AbstractParam<IncrementParam> {

    private Integer initValue;
    private Integer step;

    @Override
    public boolean check(TreeItem<TreeNode> treeItem, ParamModel paramModel) {
        checkNull(initValue, "value");
        checkNull(step, "value");
        if (initValue < 1) errorMap.put("initValue", "不能小于1");
        if (step < 1) errorMap.put("step", "不能小于1");
        if (errorMap.size() > 0) return false;

        try{
            IncrementVar var = IncrementVarDao.getByName(paramModel.getName());
            if (var != null) {
                errorMap.put("name", "名称重复");
            }
        } catch (Exception e) {
            errorMap.put("name", "dbError");
        }

        return false;
    }

//    public IncrementParam parse(String data) {
//        IncrementParam incrementParam = JsonUtil.toObject(data, IncrementParam.class);
//        return incrementParam;
//    }
}
