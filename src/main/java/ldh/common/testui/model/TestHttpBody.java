package ldh.common.testui.model;

import ldh.common.testui.constant.ParamType;
import lombok.Data;

/**
 * Created by ldh123 on 2017/6/8.
 */
@Data
public class TestHttpBody {

    private Integer id;
    private Integer testHttpId;
    private String contentType;
    private String body;
}
