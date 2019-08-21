package ldh.common.testui.demo;

import lombok.Data;

import java.util.List;

@Data
public class Teacher {

    private Integer id;
    private String name;
    private Short age;

    private List<Student> studentList;
}
