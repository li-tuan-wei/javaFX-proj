package ldh.common.testui.model;

import ldh.common.testui.constant.HttpMethod;
import lombok.Data;

import java.util.List;

/**
 * Created by ldh123 on 2017/6/8.
 */
@Data
public class TestHttp {

    private Integer id;
    private HttpMethod method;
    private String url;
    private Integer treeNodeId;
}
