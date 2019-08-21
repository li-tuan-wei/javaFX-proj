package ldh.common.testui.cell;

import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.*;
import javafx.scene.paint.Color;
import ldh.common.testui.constant.TreeNodeType;
import ldh.common.testui.dao.TreeDao;
import ldh.common.testui.model.TreeNode;

/**
 * Created by ldh on 2018/4/2.
 */
public class DraggableTreeCell extends TreeCell<TreeNode> {

    private TreeView<TreeNode> treeView;

    public DraggableTreeCell(TreeView<TreeNode> treeView) {
        this.treeView = treeView;

        this.setOnDragDetected(this::dragDetected);
        this.setOnDragOver(this::dragOver);
        this.setOnDragDropped(this::dragDropped);
        this.setOnDragExited(this::dragExited);
    }

    private void dragExited(DragEvent dragEvent) {
    }

    private void dragDropped(DragEvent event) {
        TreeItem<TreeNode> target = ((DraggableTreeCell)event.getGestureTarget()).getTreeItem();
        TreeItem<TreeNode> treeItem = treeView.getSelectionModel().getSelectedItem();
        if (target.getParent() != treeItem.getParent()) {
            event.consume();return;
        }
        try {
            TreeItem<TreeNode> parent = target.getParent();
            int idx1 = parent.getChildren().indexOf(target);

            parent.getChildren().removeAll(treeItem);
            parent.getChildren().add(idx1, treeItem);
            for (int i=idx1-1; i<parent.getChildren().size(); i++) {
                int k = i < 0 ? 0 : i;
                TreeItem<TreeNode> tmp = parent.getChildren().get(k);
                tmp.getValue().setIndex(k+1);
                TreeDao.save(tmp.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        event.consume();
    }

    private void dragOver(DragEvent event) {
        if (event.getGestureSource() != treeView || !event.getDragboard().hasString()) {
            event.consume();
            return;
        }

        event.acceptTransferModes(TransferMode.MOVE);
        event.consume();
    }

    private void dragDetected(MouseEvent event) {
        Dragboard dragboard = treeView.startDragAndDrop(TransferMode.ANY);
        ClipboardContent content = new ClipboardContent();
        content.putString(this.getTreeItem().getValue().getName());
        dragboard.setContent(content);

        event.consume();
    }

    @Override protected void updateItem(TreeNode item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setGraphic(null);
            setText(null);
        } else {
            GlyphIcon icon = buildTreeItemGraphic(item);
            item.getRunSuccess().addListener((ob, o, n) -> {
                setIconColor(icon, item);
            });
            item.getEnableProperty().addListener((ob, o, n) -> {
                setIconColor(icon, item);
            });

            setIconColor(icon, item);
            setText(item.getName());
            setGraphic(icon);
        }
    }

    private void setIconColor(GlyphIcon icon, TreeNode item) {
        icon.setGlyphSize(18);
        if (item.getRunSuccess().get() == 1) {
            icon.setFill(Color.GREEN);
        } else if (item.getRunSuccess().get() == 2) {
            icon.setFill(Color.RED);
        } else if (item.getRunSuccess().get() == 3) {
            icon.setFill(Color.YELLOW);
        } else if (!item.getEnable()) {
            icon.setGlyphSize(16);
            icon.setFill(Color.GRAY);
        } else {
            icon.setFill(Color.STEELBLUE);
        }
    }

    private GlyphIcon buildTreeItemGraphic(TreeNode treeNode) {
        GlyphIcon icon = null;
        if (treeNode.getParentId() == 0) {
            icon = icon = new FontAwesomeIconView(FontAwesomeIcon.OBJECT_GROUP);
            icon.setGlyphSize(23);
            icon.setFill(Color.STEELBLUE);
            return icon;
        } else if (treeNode.getTreeNodeType() == TreeNodeType.Node) {
            icon = new FontAwesomeIconView(FontAwesomeIcon.OBJECT_GROUP);
        } else if (treeNode.getTreeNodeType() == TreeNodeType.Param) {
            icon = new MaterialDesignIconView(MaterialDesignIcon.SETTINGS_BOX);
        } else if (treeNode.getTreeNodeType() == TreeNodeType.Case) {
            icon = new MaterialDesignIconView(MaterialDesignIcon.VIEW_HEADLINE);
        } else if (treeNode.getTreeNodeType() == TreeNodeType.Test) {
            icon = new MaterialDesignIconView(MaterialDesignIcon.HOUZZ_BOX);
        } else if (treeNode.getTreeNodeType() == TreeNodeType.Method) {
            icon = new MaterialDesignIconView(MaterialDesignIcon.LAN);
        } else if (treeNode.getTreeNodeType() == TreeNodeType.Http) {
            icon = new MaterialDesignIconView(MaterialDesignIcon.SPOTLIGHT_BEAM);
        } else if (treeNode.getTreeNodeType() == TreeNodeType.Data) {
            icon = new MaterialDesignIconView(MaterialDesignIcon.DATABASE_PLUS);
        } else if (treeNode.getTreeNodeType() == TreeNodeType.Data2) {
            icon = new MaterialDesignIconView(MaterialDesignIcon.DATABASE_MINUS);
        } else if (treeNode.getTreeNodeType() == TreeNodeType.Bean) {
            icon = new FontAwesomeIconView(FontAwesomeIcon.USER_PLUS);
        } else if (treeNode.getTreeNodeType() == TreeNodeType.BeanVar) {
            icon = new MaterialDesignIconView(MaterialDesignIcon.SETTINGS);
        } else if (treeNode.getTreeNodeType() == TreeNodeType.SqlCheckData) {
            icon = new MaterialDesignIconView(MaterialDesignIcon.ACCOUNT_ALERT);
        } else if (treeNode.getTreeNodeType() == TreeNodeType.Function) {
            icon = new MaterialDesignIconView(MaterialDesignIcon.FUNCTION);
        } else if (treeNode.getTreeNodeType() == TreeNodeType.ExportData) {
            icon = new MaterialDesignIconView(MaterialDesignIcon.EXPORT);
        } else {
            icon = new MaterialDesignIconView(MaterialDesignIcon.LEAF);
        }

        setIconColor(icon, treeNode);
        return icon;
    }
}
