package ldh.common.testui.vo;

import com.google.gson.annotations.Expose;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;

@Data
public class DataExportItem {

    private Long id;
    @Expose
    private String tableName;
    @Expose
    private Boolean selected;
    private BooleanProperty selectedProperty = new SimpleBooleanProperty(true);
    private StringProperty whereProperty = new SimpleStringProperty();
    @Expose
    private String where;
    @Expose
    private String tableDesc;

    public DataExportItem() {
        selectedProperty.addListener((b, o, n)->selected = n);
        whereProperty.addListener((b, o, n)->where = n);
        selectedProperty.set(true);
        whereProperty.set("");
    }

    public Boolean getSelected() {
        if (selected != null) return selected;
        return selectedProperty.get();
    }

    public void setSelected(Boolean isSelected) {
        selectedProperty.setValue(isSelected);
    }

    public BooleanProperty selectedProperty() {
        return selectedProperty;
    }

    public String getWhere() {
        if (!where.equals("")) return where;
        return whereProperty.get();
    }

    public void setWhere(String where) {
        whereProperty.set(where);
    }

    public StringProperty whereProperty() {
        return whereProperty;
    }
}
