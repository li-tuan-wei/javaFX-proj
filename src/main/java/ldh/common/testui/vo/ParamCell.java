package ldh.common.testui.vo;

import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import ldh.common.testui.constant.CompareType;
import ldh.common.testui.constant.MethodType;

import java.lang.reflect.Type;
import java.time.LocalDate;


/**
 * Created by ldh on 2018/3/23.
 */
public class ParamCell {

    private Region region;
    private ComboBox<String> convert;
    private MethodType methodType;
    private Class clazz;
    private Type type;
    private String checkName;

    public ParamCell(Region region, ComboBox<String> convert, MethodType methodTypte, Class clazz, Type type, String checkName) {
        this.region = region;
        this.convert = convert;
        this.methodType = methodTypte;
        this.clazz = clazz;
        this.type = type;
        this.checkName = checkName;
    }

    public String getValue() {
        if (region instanceof TextField) {
            return ((TextField) region).getText().trim();
        } else if (region instanceof DatePicker) {
            return ((DatePicker) region).getValue().toString();
        } else if (region instanceof CheckBox) {
            return ((CheckBox)region).isSelected() + "";
        } else if (region instanceof ComboBox) {
            Object value = ((ComboBox)region).getSelectionModel().getSelectedItem();
            if (value == null) return null;
            return value.toString();
        } else if (region instanceof TextArea) {
            return ((TextArea)region).getText().trim();
        } else if (region instanceof StackPane) {
            String convertt = convert.getSelectionModel().getSelectedItem();
            if (convertt.equals("Json")) {
                return ((TextArea)((StackPane) region).getChildren().get(1)).getText().trim();
            } else {
                return ((TextField)((StackPane) region).getChildren().get(0)).getText().trim();
            }
        } else if (region instanceof ChoiceBox) {
            Object value = ((ChoiceBox)region).getSelectionModel().getSelectedItem();
            if (value == null) return null;
            return value.toString();
        } else {
            throw new RuntimeException("暂时不支持这种类型");
        }
    }

    public void setData(String data) {
        if (data == null) return;
        if (region instanceof TextField) {
            ((TextField) region).setText(data);
        } else if (region instanceof DatePicker) {
            ((DatePicker) region).setValue(LocalDate.now());
        } else if (region instanceof CheckBox) {
            ((CheckBox)region).selectedProperty().set(Boolean.valueOf(data));
        } else if (region instanceof ComboBox) {
            ((ComboBox)region).getSelectionModel().select(data);
        } else if (region instanceof TextArea) {
            ((TextArea)region).setText(data);
        } else if (region instanceof StackPane) {
            String convertt = convert.getSelectionModel().getSelectedItem();
            if (convertt.equals("Json")) {
                ((TextArea)((StackPane) region).getChildren().get(1)).setText(data);
            } else {
                ((TextField)((StackPane) region).getChildren().get(0)).setText(data);
            }
        } else if (region instanceof ChoiceBox) {
            ((ChoiceBox)region).getSelectionModel().select(data);
        } else {
            throw new RuntimeException("暂时不支持这种类型");
        }
    }

    public void setConvert(String convertt) {
        if (convertt == null) return;
        convert.getSelectionModel().select(convertt);
    }

    public Region getRegion() {
        return region;
    }

    public ComboBox<String> getConvert() {
        return convert;
    }

    public MethodType getMethodType() {
        return methodType;
    }

    public Class getClazz() {
        return clazz;
    }

    public Type getType() {
        return type;
    }

    public String getCheckName() {
        return checkName;
    }
}
