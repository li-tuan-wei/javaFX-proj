package ldh.common.testui.swagger;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Swagger {

    private Info info;
    private String host;
    private String basePath;
    private List<Tag> tags;

    private Map<String, Map<String, PathInfo>> paths;
}
