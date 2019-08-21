package ldh.common.testui.util;

import de.jensd.fx.glyphs.GlyphIcon;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import ldh.common.testui.constant.SqlHandleType;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.controller.*;
import ldh.common.testui.model.TreeNode;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by ldh on 2018/3/21.
 */
public class RegionUtil {

    public static Parent sqlPane(TreeItem<TreeNode> treeItem) {
        Parent root = null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainAppController.class.getResource("/fxml/SqlDataPane.fxml"));
            root = fxmlLoader.load();
            SqlDataPaneController sqlDataPaneController = fxmlLoader.getController();
            sqlDataPaneController.setTreeItem(treeItem);
            if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Data) {
                sqlDataPaneController.setSqlHandleType(SqlHandleType.data_pre);
            } else if (treeItem.getValue().getTreeNodeType() == TreeNodeType.Data2) {
                sqlDataPaneController.setSqlHandleType(SqlHandleType.data_last);
            } else {
                throw new RuntimeException("不支持这种类型:" + treeItem.getValue().getTreeNodeType());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    public static Parent httpPane(TreeItem<TreeNode> treeItem) {
        Parent root = null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainAppController.class.getResource("/fxml/TestForm.fxml"));
            root = fxmlLoader.load();
            TestFormController from = fxmlLoader.getController();
            from.setTreeItem(treeItem);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    public static Parent sqlCheckPane(TreeItem<TreeNode> treeItem) {
        Parent root = null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainAppController.class.getResource("/fxml/SqlCheck.fxml"));
            root = fxmlLoader.load();
            SqlCheckPaneController from = fxmlLoader.getController();
            from.setTreeItem(treeItem);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    public static Parent classPane(TreeItem<TreeNode> treeItem) {
        Parent root = null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainAppController.class.getResource("/fxml/ClassPane.fxml"));
            root = fxmlLoader.load();
            ClassPaneController sqlPaneController = fxmlLoader.getController();
            sqlPaneController.setTreeItem(treeItem);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    public static Parent beanVarPane(TreeItem<TreeNode> treeItem) {
        Parent root = null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainAppController.class.getResource("/fxml/BeanVar.fxml"));
            root = fxmlLoader.load();
            BeanVarController sqlPaneController = fxmlLoader.getController();
            sqlPaneController.setTreeItem(treeItem);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    public static Parent paramPane(TreeItem<TreeNode> treeItem) {
        Parent root = null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainAppController.class.getResource("/fxml/ParamPane.fxml"));
            root = fxmlLoader.load();
            ParamPaneController paramPaneController = fxmlLoader.getController();
            paramPaneController.setTreeItem(treeItem);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    public static Parent functionPane(TreeItem<TreeNode> treeItem) {
        Parent root = null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainAppController.class.getResource("/fxml/FunctionPane.fxml"));
            root = fxmlLoader.load();
            FunctionPaneController functionPaneController = fxmlLoader.getController();
            functionPaneController.setTreeItem(treeItem);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    public static Parent beanPane(TreeItem<TreeNode> treeItem) {
        Parent root = null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainAppController.class.getResource("/fxml/BeanPane.fxml"));
            root = fxmlLoader.load();
            BeanCheckController sqlPaneController = fxmlLoader.getController();
            sqlPaneController.setTreeItem(treeItem);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    public static Parent exportDataPane(TreeItem<TreeNode> treeItem) {
        Parent root = null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(MainAppController.class.getResource("/fxml/DataExportPane.fxml"));
            root = fxmlLoader.load();
            DataExportController dataExportController = fxmlLoader.getController();
            dataExportController.setTreeItem(treeItem);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return root;
    }

    public static GlyphIcon createIcon(GlyphIcon glyphIcon, int size, Color color) {
        glyphIcon.setGlyphSize(size);
        glyphIcon.setFill(color);
        return glyphIcon;
    }

    public static void show(Parent showParent, Parent ... hiddenParents) {
        showParent.setVisible(true);
        Arrays.stream(hiddenParents).forEach(parent -> parent.setVisible(false));
    }
}
