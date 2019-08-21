package ldh.common.testui.assist.template.beetl.function;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ldh on 2019/1/3.
 */
public class DateHelp {

    public Date nowDate() {
        return new Date();
    }

    public Date parse(String dateStr, String format) throws ParseException {
        SimpleDateFormat myFmt=new SimpleDateFormat(format);
        return myFmt.parse(dateStr);
    }

    public String format(Date date, String format) {
        SimpleDateFormat myFmt=new SimpleDateFormat(format);
        return myFmt.format(date);
    }

    public String formatNow(String format) {
        SimpleDateFormat myFmt=new SimpleDateFormat(format);
        return myFmt.format(new Date());
    }

    public String formatNow(String format, Integer changeDays) {
        SimpleDateFormat myFmt=new SimpleDateFormat(format);
        Date date = new Date();
        date.setTime(date.getTime() + changeDays * 24L * 60L * 60L * 1000L);
        return myFmt.format(date);
    }

    public String formatNow(String format, Integer changeDays, Integer addMonthDay) {
        SimpleDateFormat myFmt=new SimpleDateFormat(format);
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, changeDays);
        calendar.add(Calendar.MONTH, addMonthDay);
        date = calendar.getTime();
        return myFmt.format(date);
    }

    public Date addMonth(Date date, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) + day);
        return calendar.getTime();
    }
}
