package ldh.common.testui.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ldh on 2018/3/23.
 */
public class DateUtil {

    public static Date parse(String dateStr) {
        SimpleDateFormat myFmt1=new SimpleDateFormat("yyyy-MM-dd");
        try {
            return myFmt1.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static Date parse(String dateStr, String format) {
        SimpleDateFormat myFmt1=new SimpleDateFormat(format);
        try {
            return myFmt1.parse(dateStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String format(Date date, String pattern) {
        SimpleDateFormat myFmt1=new SimpleDateFormat(pattern);
        return myFmt1.format(date);
    }
 }
