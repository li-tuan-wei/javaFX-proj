package ldh.common.testui.util;

import ldh.common.testui.model.SqlData;
import lombok.Data;

import java.util.List;

public class OrderUtil {

    @Data
    public static class OrderAble {
        private Integer index;
    }

    public static <T> T pre(List<T> orderAbleList, Integer index) {
        T preRow = null;
        for (T t : orderAbleList) {
            if (t instanceof OrderAble) throw new RuntimeException("不是orderAble类型");
            OrderAble orderAble = (OrderAble) t;
            int currentIndex = orderAble.getIndex();
            if (currentIndex < index) {
                preRow = t;
                continue;
            } else if (currentIndex >= index) {
                break;
            }
        }
        return preRow;
    }

    public static <T> T next(List<T> orderAbleList, int index) {
        for (T t : orderAbleList) {
            if (t instanceof OrderAble) throw new RuntimeException("不是orderAble类型");
            OrderAble orderAble = (OrderAble) t;
            int currentIndex = orderAble.getIndex();
            if (currentIndex < index) continue;
            if (currentIndex == index) {
                continue;
            }
            return t;
        }
        return null;
    }

    public static <T> int maxIndex(List<T> orderAbleList) {
        int maxIndex = 1;
        for (T t : orderAbleList) {
            if (!(t instanceof OrderAble)) throw new RuntimeException("不是orderAble类型");
            OrderAble orderAble = (OrderAble) t;
            int index = orderAble.getIndex();
            if (maxIndex < index) maxIndex = index;
        }
        return maxIndex;
    }
}
