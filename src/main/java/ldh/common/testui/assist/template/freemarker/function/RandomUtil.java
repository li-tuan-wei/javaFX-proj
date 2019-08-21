package ldh.common.testui.assist.template.freemarker.function;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ldh on 2018/4/11.
 */
public class RandomUtil {

    private static final Random RANDOM = new Random();
    private static final AtomicInteger atomicInteger = new AtomicInteger(1);

    public static Integer random(int minValue, int maxValue) {
        int value = RANDOM.nextInt(maxValue);
        while(value < minValue) {
            value = RANDOM.nextInt(maxValue);
        }
        return value;
    }

    public static Date nowDate() {
        return new Date();
    }

    public static Date parse(String dateStr, String format) throws ParseException {
        SimpleDateFormat myFmt=new SimpleDateFormat(format);
        return myFmt.parse(dateStr);
    }

    public static String format(Date date, String format) {
        SimpleDateFormat myFmt=new SimpleDateFormat(format);
        return myFmt.format(date);
    }

    public static String formatNow(String format) {
        SimpleDateFormat myFmt=new SimpleDateFormat(format);
        return myFmt.format(new Date());
    }

    public static String formatNow(String format, Integer changeDays) {
        SimpleDateFormat myFmt=new SimpleDateFormat(format);
        Date date = new Date();
        date.setTime(date.getTime() + changeDays * 24L * 60L * 60L * 1000L);
        return myFmt.format(date);
    }

    public static Integer seq() {
        return atomicInteger.incrementAndGet();
    }
}
