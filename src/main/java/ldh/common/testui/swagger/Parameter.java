package ldh.common.testui.swagger;

import lombok.Data;

@Data
public class Parameter {

    private String name;
    private String in;
    private String description;
    private Boolean required;
    private String type;
    private String format;
}
