package db;

import ldh.common.testui.util.DateUtil;
import ldh.common.testui.util.DbUtils;
import org.junit.Test;

import java.sql.Connection;

/**
 * Created by ldh on 2019/1/7.
 */
public class H2Tet {

    @Test
    public void test() throws Exception {
        Connection connection = DbUtils.getConnection("org.h2.Driver", "jdbc:h2:tcp://localhost/mem:test", "sa", "");
        Thread.sleep(1000000);
    }

    @Test
    public void test2() throws Exception {
        Connection connection = DbUtils.getConnection("org.h2.Driver", "jdbc:h2:tcp://localhost/mem:test", "sa", "");
        Thread.sleep(1000000);
    }
}
