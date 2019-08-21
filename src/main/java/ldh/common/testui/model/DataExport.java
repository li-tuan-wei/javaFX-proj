package ldh.common.testui.model;

import ldh.common.testui.util.DataUtil;
import lombok.Data;

@Data
public class DataExport {

    private Long id;
    private Integer treeNodeId;
    private Integer databaseParamId;
    private transient ParamModel databaseParam = null;
    private String dir;
    private String name;
    private String data;

    public ParamModel getDatabaseParamModel() {
        if (databaseParam == null && databaseParamId != null) {
            databaseParam = DataUtil.getById(databaseParamId);
        }
        return databaseParam;
    }

}
