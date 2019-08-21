package ldh.common.testui.util;

/**
 * Created by ldh on 2019/1/17.
 */
public class StringUtil {

    public static boolean isUpperString(String str) {
        for(int i=0; i<str.length(); i++){
            char c = str.charAt(i);
            if(c >= 97 && c <= 122) {
                return false;
            }
        }
        return true;
    }
}
