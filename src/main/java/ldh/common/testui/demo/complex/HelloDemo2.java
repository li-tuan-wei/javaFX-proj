package ldh.common.testui.demo.complex;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ldh on 2018/3/29.
 */
public class HelloDemo2 {

    public List<String> listTest(String str) {
        return Arrays.asList(str.split(","));
    }

    public List<String> mapTest(Map<String, String> data) {
        return Arrays.asList("test1", "test2");
    }

    public <T> T test(T t) {
        return t;
    }

    public String arrayTest(String[] args) {
        String result = "";
        for (String s : args) {
            result = result + "," + s;
        }
        return result;
    }

    public String[] arrayTest2(String[] args) {
        return args;
    }

    public Map<String, String> mapTest(String key, Map<String, String> data) {
        return data;
    }

    public Set<Integer> testSet(Set<Integer> sets) {
        return sets;
    }
}
