package ldh.common.testui.demo;

import lombok.Data;

@Data
public class Student {

    private Integer id;
    private String name;
    private Short age;

    private Student student;
}
