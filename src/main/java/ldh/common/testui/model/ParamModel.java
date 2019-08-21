package ldh.common.testui.model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.beans.property.*;
import ldh.common.testui.constant.ParamCategory;

/**
 * Created by ldh on 2018/3/17.
 */
public class ParamModel extends RecursiveTreeObject<ParamModel> {

    private IntegerProperty id = new SimpleIntegerProperty();
    private IntegerProperty treeNodeId = new SimpleIntegerProperty();
    private IntegerProperty seq = new SimpleIntegerProperty();
    private StringProperty name = new SimpleStringProperty();
    private ObjectProperty<ParamCategory> paramCategory = new SimpleObjectProperty<>();
    private StringProperty value = new SimpleStringProperty();
    private StringProperty desc = new SimpleStringProperty();
    private StringProperty className = new SimpleStringProperty("json");

    public Integer getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(Integer id) {
        this.id.set(id);
    }

    public Integer getIndex() {
        return seq.get();
    }

    public void setSeq(Integer seq) {
        indexProperty().set(seq);
    }

    public IntegerProperty seqProperty() {
        return seq;
    }

    public Integer getSeq() {
        return seq.get();
    }

    public IntegerProperty indexProperty() {
        return seq;
    }

    public void setIndex(int index) {
        this.seq.set(index);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public ParamCategory getParamCategory() {
        return paramCategory.get();
    }

    public ObjectProperty<ParamCategory> paramCategoryProperty() {
        return paramCategory;
    }

    public void setParamCategory(ParamCategory paramCategory) {
        this.paramCategory.set(paramCategory);
    }

    public String getValue() {
        return value.get();
    }

    public StringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public String getDesc() {
        return desc.get();
    }

    public StringProperty descProperty() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc.set(desc);
    }

    public int getTreeNodeId() {
        return treeNodeId.get();
    }

    public IntegerProperty treeNodeIdProperty() {
        return treeNodeId;
    }

    public void setTreeNodeId(int treeNodeId) {
        this.treeNodeId.set(treeNodeId);
    }

    public String getClassName() {
        return className.get();
    }

    public StringProperty classNameProperty() {
        return className;
    }

    public void setClassName(String className) {
        this.className.set(className);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ParamModel)) return false;
        ParamModel pm = (ParamModel) obj;
        return pm.getName().equals(this.getName());
    }

    @Override
    public int hashCode() {
        return this.getName().hashCode();
    }
}
