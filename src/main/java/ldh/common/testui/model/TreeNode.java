package ldh.common.testui.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import ldh.common.testui.constant.TreeNodeType;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
/**
 * Created by ldh on 2017/6/5.
 */
@Data
public class TreeNode {

    private Integer id;
    private String name;
    private String desc;
    private Integer parentId = 0;
    private TreeNodeType treeNodeType;
    private List<TreeNode> children;
    private Integer index;
    private String path;
    private Boolean enable;
    private BooleanProperty enableProperty = new SimpleBooleanProperty(true);
    private IntegerProperty runSuccess = new SimpleIntegerProperty();

    public TreeNode(){}

    public TreeNode(String name, TreeNodeType treeNodeType) {
        this.id = 0;
        this.name = name;
        this.treeNodeType = treeNodeType;
    }

    public Boolean getEnable() {
        if (enable == null) {
            enable = true;
            enableProperty.set(enable);
        }
        return enable;
    }

    public void setEnableNoProperty(Boolean enable) {
        this.enable = enable == null ? true : enable;
    }

    public void setEnable(Boolean enable) {
        this.enable = enable == null ? true : enable;
        enableProperty.set(this.enable);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof TreeNode)) return false;
        TreeNode pm = (TreeNode) obj;
        return pm.getId().equals(this.getId());
    }

    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }
}
