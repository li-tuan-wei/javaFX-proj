package ldh.common.testui.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Data;

/**
 * Created by ldh on 2018/3/22.
 */
@Data
public class TestMethodData {

    private Integer id = 0;
    private Integer testMethodId = 0;
    private String testName = "";
    private String data = "";
    private String varName = "";
}
