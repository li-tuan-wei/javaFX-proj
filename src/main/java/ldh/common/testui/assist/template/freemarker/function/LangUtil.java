package ldh.common.testui.assist.template.freemarker.function;

import java.math.BigDecimal;

/**
 * Created by ldh on 2018/12/20.
 */
public class LangUtil {

    public static int toInt(String value) {
        return Integer.valueOf(value);
    }

    public static BigDecimal toBigDecimal(String value) {
        return new BigDecimal(value);
    }

    public static BigDecimal toD(String value) {
        return new BigDecimal(value);
    }

    public static BigDecimal subtract(String amount1, String amount2) {
        BigDecimal a1 = new BigDecimal(amount1);
        BigDecimal a2 = new BigDecimal(amount2);
        return a1.subtract(a2);
    }

    public static BigDecimal add(String amount1, String amount2) {
        BigDecimal a1 = new BigDecimal(amount1);
        BigDecimal a2 = new BigDecimal(amount2);
        return a1.add(a2);
    }
}
