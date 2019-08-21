package ldh.common.testui.model;

import ldh.common.testui.constant.TestLogType;
import lombok.Data;

import java.util.Date;

/**
 * Created by ldh on 2018/12/21.
 */
@Data
public class TestLogData {

    private Integer id;
    private String name;
    private Integer testLogId;
    private TestLogType type;
    private String content;


    public static TestLogData buildTestLogData(Integer testLogId, String name, TestLogType logType, String content) {
        TestLogData testLogData = new TestLogData();
        testLogData.setName(name);
        testLogData.setTestLogId(testLogId);
        testLogData.setType(logType);
        testLogData.setContent(content);
        return testLogData;
    }
}
