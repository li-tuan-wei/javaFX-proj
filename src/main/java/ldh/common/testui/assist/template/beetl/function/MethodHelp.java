package ldh.common.testui.assist.template.beetl.function;

import java.util.List;
import java.util.Map;

/**
 * Created by ldh on 2018/12/25.
 */
public class MethodHelp {

    public Object getForList(Object bean, int index) {
        if (bean instanceof List) {
            List<?> beanList = (List<?>) bean;
            if (beanList.size() < index) {
                throw new RuntimeException("队列超界:" + index);
            }
            return beanList.get(index);
        }
        throw new RuntimeException("对象不是队列");
    }

    public Object getForArray(Object bean, int index) {
        if (bean.getClass().isArray()) {
            Object[] arrays = (Object[]) bean;
            if (arrays.length < index) {
                throw new RuntimeException("数组超界:" + index);
            }
            return arrays[index];
        }
        throw new RuntimeException("对象不是数组");
    }

    public Object getForMap(Object bean, Object key) {
        if (bean instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) bean;
            if (!map.containsKey(key)) {
                throw new RuntimeException("没有这个键值:" + key );
            }
            return map.get(key);
        }
        throw new RuntimeException("对象不是Map");
    }
}
