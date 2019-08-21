package ldh.common.testui.controller.pane;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import ldh.common.testui.cell.ObjectListCell;
import ldh.common.testui.cell.ObjectStringConverter;
import ldh.common.testui.constant.ParamCategory;
import ldh.common.testui.controller.BaseController;
import ldh.common.testui.model.ParamModel;
import ldh.common.testui.model.TreeNode;
import ldh.common.testui.util.DialogUtil;
import ldh.common.testui.util.ThreadUtilFactory;
import ldh.common.testui.util.VarUtil;
import ldh.common.testui.vo.SqlCheck;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

/**
 * Created by ldh on 2018/4/6.
 */
public class SqlCheckController extends BaseController implements Initializable {

    @FXML ComboBox<ParamModel> databaseChoiceBox;
    @FXML TextField nameTextField;
    @FXML TextArea sqlTextArea;
    @FXML TextArea sqlStructTextArea;
    @FXML TextField argsTextField;

    private SqlCheck sqlCheck = null;

    public void setTreeItem(TreeItem<TreeNode> treeItem) {
        this.treeItem = treeItem;
        Set<ParamModel> paramDatabaseSet = getParamModels(ParamCategory.Database);
        databaseChoiceBox.getItems().addAll(paramDatabaseSet);
    }

    public SqlCheck buildSqlCheck() {
        if (sqlCheck == null)
            sqlCheck = new SqlCheck();
        sqlCheck.setName(nameTextField.getText().trim());
        sqlCheck.setDatabaseParamId(databaseChoiceBox.getSelectionModel().getSelectedItem().getId());
        sqlCheck.setDatabaseParam(databaseChoiceBox.getSelectionModel().getSelectedItem());
        sqlCheck.setArgs(argsTextField.getText().trim());
        sqlCheck.setSql(sqlTextArea.getText().trim());
        sqlCheck.setSqlStruct(sqlStructTextArea.getText().trim());
        return sqlCheck;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        databaseChoiceBox.setCellFactory(new ObjectListCell<>(pm->pm.getName()));
        databaseChoiceBox.setConverter(new ObjectStringConverter<>(pm->pm.getName()));

        sqlTextArea.setWrapText(true);
        sqlStructTextArea.setWrapText(true);

        sqlTextArea.focusedProperty().addListener((b, o, n)-> {
            if (!n) {
                String sql = sqlTextArea.getText().trim();
                if (StringUtils.isEmpty(sql) && sql.toLowerCase().indexOf("where") == -1) return ;
                sqlStructTextArea.setText(sql.substring(0, sql.toLowerCase().indexOf("where")) + " limit 1");
            }
        });
    }

    public void setInitData(SqlCheck initData) {
        if (initData == null) return;
        sqlCheck = initData;
        nameTextField.setText(initData.getName());
        sqlTextArea.setText(initData.getSql());
        sqlStructTextArea.setText(initData.getSqlStruct());
        databaseChoiceBox.getSelectionModel().select(initData.getDatabaseParam());
        argsTextField.setText(initData.getArgs());
    }

    public void checkVarAction() {
        String argsText = argsTextField.getText();
        if (argsText == null) return;
        List<String> values = VarUtil.getElExpressions(argsText);

        if (values.size() < 0){
            DialogUtil.alert(String.format("没有变量，不需要检查"), Alert.AlertType.INFORMATION);
            return;
        }

        int index = Integer.MAX_VALUE;
        Task<Void> task = VarUtil.buildCheckVarTask(treeItem, values, index);
        ThreadUtilFactory.getInstance().submit(task);
    }
}
