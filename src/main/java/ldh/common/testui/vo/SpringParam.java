package ldh.common.testui.vo;

import javafx.scene.control.TreeItem;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.JsonUtil;
import ldh.common.testui.util.VarUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * Created by ldh on 2018/3/19.
 */
@Data
public class SpringParam extends AbstractParam<SpringParam> {

    private String xmlConfig;
    private String properties;

    @Override
    public boolean check(TreeItem<TreeNode> treeItem, ParamModel paramModel) {
        SpringParam sp = JsonUtil.toObject(VarUtil.replaceLine(paramModel.getValue()), SpringParam.class);
        xmlConfig = sp.getXmlConfig();
        properties = sp.getProperties();
        if (StringUtils.isEmpty(xmlConfig) && StringUtils.isEmpty(properties)) {
            errorMap.put("all", "不能都为空");
            return false;
        }
        return true;
    }
}
