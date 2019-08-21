package ldh.common.testui.model;

import javafx.beans.property.*;
import ldh.common.testui.constant.ParamCategory;
import lombok.Data;

/**
 * Created by ldh on 2018/3/22.
 */
@Data
public class TestMethod {

    private Integer id = 0;
    private Integer treeNodeId = 0;
    private Integer paramId = 0;
    private String className = "";
    private String methodName = "";
    private String instanceClassName = "";
}
