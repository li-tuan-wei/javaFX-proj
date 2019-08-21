package ldh.common.testui.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by ldh on 2018/12/21.
 */
@Data
public class TestLog {

    private Integer id;
    private String name;
    private Date createTime;
    private String type;
    private Integer parentId;
    private IntegerProperty success = new SimpleIntegerProperty();
    private Integer runSuccess;
    private Integer successNum;
    private Integer failureNum;


    public static TestLog buildTestLog(String name, String type) {
        TestLog testLog = new TestLog();
        testLog.setName(name);
        testLog.setType(type);
        testLog.setCreateTime(new Date());
        testLog.setRunSuccess(0);
        testLog.setSuccessNum(0);
        testLog.setFailureNum(0);
        testLog.setParentId(0);
        return testLog;
    }

    public void setRunSuccess(Integer runSuccess) {
        this.runSuccess = runSuccess;
    }

    public Integer getRunSuccess() {
        return runSuccess;
    }

    public void increaseSuccessNum(int n) {
        successNum+=n;
    }

    public void increaseFailureNum(int n) {
        failureNum+=n;
    }

    public int runStatus() {
        if (successNum > 0 && failureNum == 0) return 1;
        if (successNum == 0 && failureNum > 0) return 2;
        return 3;
    }
}
