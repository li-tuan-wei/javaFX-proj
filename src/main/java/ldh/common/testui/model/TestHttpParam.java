package ldh.common.testui.model;

import ldh.common.testui.constant.ParamType;
import lombok.Data;

/**
 * Created by ldh123 on 2017/6/8.
 */
@Data
public class TestHttpParam {

    private Integer id;
    private Integer testHttpId;
    private String name;
    private String content;
    private ParamType paramType;
}
