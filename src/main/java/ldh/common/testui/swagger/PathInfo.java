package ldh.common.testui.swagger;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class PathInfo {

    private List<String> tags;
    private String summary;
    private String operationId;
    private List<String> produces;
    private Boolean deprecated;

    private List<Parameter> parameters;
    private Map<String, Response> responses;

}
