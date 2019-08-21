package ldh.common.testui.vo;

import com.google.gson.annotations.Expose;
import javafx.scene.control.TreeItem;
import ldh.common.testui.constant.ParamCategory;
import ldh.common.testui.handle.RunTreeItem;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.DataUtil;
import lombok.Data;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ldh on 2018/3/28.
 */
@Data
public class SqlCheck {

    @Expose
    private Integer id;
    @Expose
    private String name;
    @Expose
    private Integer treeNodeId;
    @Expose
    private Integer databaseParamId;
    private ParamModel databaseParam;
    @Expose
    private String sql;
    @Expose
    private String args;
    @Expose
    private String sqlStruct;

    public ParamModel getDatabaseParam() {
        if (databaseParam == null && databaseParamId != null) {
            databaseParam = DataUtil.getById(databaseParamId);
        }
        return databaseParam;
    }

}
