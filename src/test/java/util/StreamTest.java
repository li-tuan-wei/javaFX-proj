package util;

import ldh.common.testui.util.VarUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Created by ldh on 2019/1/22.
 */
public class StreamTest {

    @Test
    public void streamTest() {
        List<String> tt = Arrays.asList("a", "b", "c");
        tt.stream().forEach(str->{
            System.out.println("str:" + str);
//            throw new RuntimeException("tt");
        });
        System.out.println("end:");
    }

    @Test
    public void el() {
        String context = "${content.id}";
        Set<String> setList = VarUtil.getElVarNames(context);
        setList.stream().forEach(System.out::println);
    }

    @Test
    public void str() {
        String url = "jdbc:mysql://localhost:3306/school";
        int index = url.lastIndexOf("/");
        int index2 = url.indexOf("?");
        String dd = "";
        if (index2 > 0) {
            dd = url.substring(index+1, index2);
        } else {
            dd = url.substring(index+1);
        }
        System.out.println("dd:" +dd);
    }
}
