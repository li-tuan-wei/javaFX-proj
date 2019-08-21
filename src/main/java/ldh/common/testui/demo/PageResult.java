package ldh.common.testui.demo;

import java.util.List;

/**
 * Created by ldh on 2019/1/14.
 */
public class PageResult<T> {

    int count;
    List<T> beans;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<T> getBeans() {
        return beans;
    }

    public void setBeans(List<T> beans) {
        this.beans = beans;
    }
}
