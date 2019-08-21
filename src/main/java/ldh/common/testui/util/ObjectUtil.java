package ldh.common.testui.util;

import ldh.common.testui.assist.convert.ConvertFactory;
import ldh.common.testui.assist.convert.Null;
import org.apache.commons.lang3.ObjectUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by ldh on 2018/12/24.
 */
public class ObjectUtil {

    public static boolean isEqual(Object var1, Object var2) {
        try {
            if (var1 == null && var2 == null) return true;
            if(var1 == null) return false;
            if (var1 instanceof Float || var1 instanceof Double) {
                BigDecimal b1 = new BigDecimal(var1.toString());
                BigDecimal b2 = new BigDecimal(var2.toString());
                return b1.subtract(b2).compareTo(BigDecimal.ZERO) == 0;
            }
            if (var1.getClass().isPrimitive()) {
                return var1.equals(var2);
            }
            if (var1 instanceof BigDecimal) {
                BigDecimal b1 = (BigDecimal)var1;
                BigDecimal b2 = null;
                if (var2 instanceof BigDecimal) {
                    b2 = (BigDecimal)var2;
                } else {
                    b2 = new BigDecimal(var2.toString());
                }
                return b1.subtract(b2).compareTo(BigDecimal.ZERO) == 0;
            }
            if (var1 instanceof Number) {
                BigDecimal b1 = new BigDecimal(var1.toString());
                BigDecimal b2 = new BigDecimal(var2.toString());
                return b1.subtract(b2).compareTo(BigDecimal.ZERO) == 0;
            }
            if (var1 instanceof Date) {
                Date d1 = (Date) var1;
                Date d2 = null;
                if (var2 instanceof Date) {
                    d2 = (Date) var2;
                } else {
                    d2 = (Date) ConvertFactory.getInstance().get("Date(yyyy-MM-dd)").parse(var2.toString());
                }
                return DateUtil.format(d1, "yyyy-MM-dd").equals(DateUtil.format(d2, "yyyy-MM-dd"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return var1.equals(var2);
    }

    public static List<Class> commonClass() {
        List<Class> result = new ArrayList<>();
        result.addAll(Arrays.asList(Integer.class, int.class, Short.class, short.class, Byte.class, byte.class, Boolean.class,
                boolean.class, String.class, Long.class, long.class, Double.class, double.class, BigDecimal.class, Float.class,
                float.class, Date.class, Null.class)
        );
        return result;
    }

    public static boolean isArray(Object obj) {
        return obj != null && obj.getClass().isArray();
    }

    public static boolean isList(Object obj) {
        return obj != null && obj instanceof List;
    }

    public static boolean isSet(Object obj) {
        return obj != null && obj instanceof Set;
    }

    public static boolean isMap(Object obj) {
        return obj != null && obj instanceof Map;
    }
}
