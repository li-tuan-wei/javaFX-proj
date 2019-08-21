package ldh.common.testui.demo.common;

import ldh.common.testui.constant.MethodType;
import ldh.common.testui.demo.Student;

import java.util.*;

/**
 * Created by ldh on 2018/3/21.
 */
public class HelloDemo {

    private Student student;

    public String hello(String name, String hello) {
        return "hi:" + name + ", " + hello;
    }

    public void hello(String name) {

    }

    public void hello() {

    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void helloException() {
        throw new RuntimeException("error");
    }

    public void helloException2() throws Exception {
        throw new RuntimeException("error");
    }

    public int add(int a, int b) {
        return a + b;
    }

    public Student getById(Integer id) {
        Student s = new Student();
        s.setId(1);
        s.setName("asd");
        s.setAge((short)18);

        Student s2 = new Student();
        s2.setId(2);
        s2.setName("asd2");
        s2.setAge((short)23);
        s.setStudent(s2);
        return s;
    }

    public List<Student> getStudents() {
        Student s = new Student();
        s.setId(1);
        s.setName("asd");
        s.setAge((short)18);

        Student s2 = new Student();
        s2.setId(2);
        s2.setName("asd2");
        s2.setAge((short)23);
        s.setStudent(s2);
        return Arrays.asList(s);
    }

    public List<Integer> getCommonList() {
        return Arrays.asList(1, 2, 3);
    }

    public Set<Student> getStudentsForSet() {
        return new HashSet<Student>();
    }

    public Map<Integer, Student> getStudentsForMap() {
        Student s = new Student();
        s.setId(1);
        s.setName("asd");
        s.setAge((short)18);

        Student s2 = new Student();
        s2.setId(2);
        s2.setName("asd2");
        s2.setAge((short)23);
        s.setStudent(s2);
        Map<Integer, Student> map = new HashMap<Integer, Student>();
        map.put(12, s);
        return map;
    }

    public MethodType getMethodType() {
        return MethodType.Exception;
    }

    public Student getStudent() {
        return student;
    }
}
