package util;

import ldh.common.testui.util.FileUtil;
import ldh.common.testui.util.LibLoaderFactory;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

@Ignore
public class FileUtilTest {

    @Test
    public void otherClass() throws Exception {
        String file = "E:\\testuiclass";
        List<String> result = FileUtil.searchFiles(file);
        result.stream().forEach(clazz->{
            System.out.println("clazz:" + clazz);
        });
    }

    @Test
    public void otherClass2() throws Exception {
        String file = "E:\\testuiclass\\";
        LibLoaderFactory.getInstance().loadLib(file);
        List<Class> result = FileUtil.searchClass("util");
        result.stream().forEach(clazz->{
            System.out.println("clazz:" + clazz);
        });
    }
}
