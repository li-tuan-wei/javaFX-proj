package ldh.common.testui.demo.common;

import ldh.common.testui.util.DateUtil;

import java.util.*;

public class HelloSimpleDemo {

    private String desc;

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String hello(String name, Date date) {
        String d = DateUtil.format(date, "yyyy-MM-dd hh:mm:ss");
        System.out.println("dd:" + d);
        return "hello " + desc == null ? "null" : desc;
    }

    public String[] getType() {
        return new String[] {"aa", "bb"};
    }

    public List<String> getNames() {
        return Arrays.asList("a", "b", "c", "d");
    }

    public Set<String> getNameSet() {
        Set<String> set = new HashSet<>();
        set.add("a");
        set.add("b");
        set.add("ab");
        return set;
    }

    public Map<Integer, String> getMap() {
        Map<Integer, String> map = new HashMap();
        map.put(11, "11a");
        map.put(22, "22b");
        map.put(33, "33c");
        return map;
    }

    public String getDesc() {
        return desc;
    }
}
