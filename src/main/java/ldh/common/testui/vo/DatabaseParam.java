package ldh.common.testui.vo;

import javafx.scene.control.TreeItem;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.JsonUtil;
import ldh.common.testui.util.VarUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

/**
 * Created by ldh on 2018/3/19.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DatabaseParam extends AbstractParam<DatabaseParam>{

    private String url;
    private String userName;
    private String password;
    private String driverName = "com.mysql.jdbc.Driver";
    private String springDatabaseName;

    @Override
    public boolean check(TreeItem<TreeNode> treeItem, ParamModel paramModel) {
        DatabaseParam dp = JsonUtil.toObject(VarUtil.replaceLine(paramModel.getValue()), DatabaseParam.class);
        if (!StringUtils.isEmpty(dp.getSpringDatabaseName())) {
            return true;
        }
        checkEmpty(dp.getUrl(), "url");
        checkEmpty(dp.getUserName(), "userName");
//        checkEmpty(dp.getPassword(), "password");
        checkEmpty(dp.getDriverName(), "driverName");
        if (errorMap.size() > 0) return false;
        return true;
    }

    public String getDbName() {
        int index = url.lastIndexOf("/");
        int index2 = url.indexOf("?");
        if (index2 > 0) {
            return url.substring(index+1, index2);
        }
        return url.substring(index+1);

    }
}
