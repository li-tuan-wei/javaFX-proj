package ldh.common.testui.vo;

import javafx.scene.control.TreeItem;
import ldh.common.testui.constant.ParamCategory;
import ldh.common.testui.handle.RunTreeItem;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ldh on 2018/3/28.
 */
@Data
public class SqlParam extends AbstractParam<SqlParam> {

    private String databaseName;
    private String sql;
    private List<String> args = new ArrayList<>();

    @Override
    public boolean check(TreeItem<TreeNode> treeItem, ParamModel paramModel) {
        checkEmpty(databaseName, "databaseName");
        checkEmpty(sql, "sql");
        if (errorMap.size() > 0) return false;
        checkDatabaseName(treeItem);
        checkSql();
        if (errorMap.size() > 0) return false;
        return true;
    }

    private void checkSql() {
        int size = findArgsTotal();;
        int argsSize = args.size();
        if (size != argsSize) {
            errorMap.put("sql", "错误");
        }
        for (String arg : args) {
            String a = arg.trim();
            if (a.startsWith("#{") && a.endsWith("}")) {
                errorMap.put("args", "错误");
            }
        }
    }

    private int findArgsTotal() {
        int i = 0;
        int idx = sql.indexOf("?");
        while(idx != -1) {
            idx = sql.indexOf("?", idx);
            i++;
        }
        return i;
    }

    private void checkDatabaseName(TreeItem<TreeNode> treeItem) {
        try {
            List<ParamModel> paramModelList = RunTreeItem.getAllParamModel(treeItem);
            for (ParamModel pm : paramModelList) {
                if (pm.getParamCategory() == ParamCategory.Database) {
                    if (pm.getName().equals(databaseName)) return;
                }
            }
            errorMap.put("databaseName", "没有找到对应的数据源");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
