package ldh.common.testui.assist.template.beetl.function;

import java.math.BigDecimal;

/**
 * Created by ldh on 2018/12/25.
 */
public class LangHelp {

    public BigDecimal scale(Object obj, Integer scale) {
        BigDecimal bigDecimal = null;
        if (obj instanceof BigDecimal) {
            bigDecimal = (BigDecimal) obj;
        } else {
            bigDecimal = new BigDecimal(obj.toString());
        }
        return bigDecimal.setScale(scale, BigDecimal.ROUND_HALF_UP);
    }

    public Number max(Number n1, Number n2) {
        return n1.longValue() > n2.longValue() ? n1 : n2;
    }

    public Number min(Number n1, Number n2) {
        return n1.longValue() > n2.longValue() ? n2 : n1;
    }
}
