package ldh.common.testui.demo.common;

import ldh.common.testui.demo.PageResult;
import ldh.common.testui.demo.Student;
import ldh.common.testui.demo.TAddress;
import ldh.common.testui.demo.Teacher;
import ldh.common.testui.util.DateUtil;

import java.util.*;

public class HelloComplexDemo {

    private Date date;
    private String name;

    private boolean isSuccess = false;

    public void setData(Date date) {
        this.date = date;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String hello() {
        String d = DateUtil.format(date, "yyyy-MM-dd HH:mm:ss");
        System.out.println("dd:" + d);
        isSuccess = true;
        return "hello " + d;
    }

    public boolean getIsSuccess() {
        return isSuccess;
    }

    public Teacher getTeacher() {
        Teacher teacher = new Teacher();
        List<Student> studentList = buildStudents(10);
        teacher.setStudentList(studentList);
        return teacher;
    }

    public PageResult<Student> getStudents() {
        PageResult<Student> pageResult = new PageResult<>();
        pageResult.setCount(10);
        pageResult.setBeans(buildStudents(10));
        return pageResult;
    }

    private List<Student> buildStudents(int size) {
        List<Student> students = new ArrayList<>();
        for (int i=0; i<size; i++) {
            Student s = new Student();
            s.setName("test " + i);
            students.add(s);
        }
        return students;
    }

    public List<List<String>> getListList() {
        List<List<String>> result = new ArrayList();
        List<String> l1 = Arrays.asList("a", "b", "c");
        result.add(l1);
        return result;
    }

    public List<Map<String, String>> getListMap() {
        List<Map<String, String>> result = new ArrayList();
        Map<String, String> map1 = new HashMap();
        map1.put("a", "a1");
        map1.put("b", "b");
        result.add(map1);
        return result;
    }

    public List<Map<String, Student>> getListMap2() {
        List<Map<String, Student>> result = new ArrayList();
        Map<String, Student> map1 = new HashMap();
        map1.put("a", new Student());
        map1.put("b", new Student());
        result.add(map1);
        return result;
    }

    public Map<String, String> getCommonMap() {
        Map<String, String> map = new HashMap();
        map.put("test1", "test1");
        map.put("test2", "test2");
        map.put("test3", "test3");
        return map;
    }

    public Map<String, List<Integer>> getMapCommonList() {
        Map<String, List<Integer>> map = new HashMap();
        map.put("test1", Arrays.asList(1, 2, 3));
        map.put("test2", Arrays.asList(1, 2, 3));
        map.put("test3", Arrays.asList(1, 2, 3));
        return map;
    }

    public Map<String, Student> getMap2() {
        Map<String, Student> map1 = new HashMap();
        map1.put("a", new Student());
        map1.put("b", new Student());
        return map1;
    }

    public Map<String, TAddress<String>> getMap3() {
        Map<String, TAddress<String>> map1 = new HashMap();
        map1.put("a", new TAddress<String>("a"));
        map1.put("b", new TAddress<String>("b"));
        return map1;
    }

}
