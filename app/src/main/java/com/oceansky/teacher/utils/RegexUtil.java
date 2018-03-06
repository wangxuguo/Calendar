package com.oceansky.teacher.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by tangqifa on 16/3/17.
 */
public class RegexUtil {
    /**
     * @descrition:手机号码段规则 13段：130、131、132、133、134、135、136、137、138、139
     * 14段：145、147
     * 15段：150、151、152、153、155、156、157、158、159
     * 17段：170、176、177、178
     * 18段：180、181、182、183、184、185、186、187、188、189
     */
    public static boolean isMobileNO(String mobiles) {
        Pattern p = Pattern.compile("^((13[0-9])|(14[5,7])|(15[^4,\\D])|(17[0-1,3,5-8])|(18[0-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);
        return m.matches();
    }

    public static boolean isPhoneOrQQNumber(String number) {
        Pattern p = Pattern.compile("^(13[0-9]|15[012356789]|17[0135678]|18[0-9]|14[57])[0-9]{8}|[1-9]\\d{4,9}$");
        Matcher m = p.matcher(number);
        return m.matches();
    }

    public static boolean checkEmail(String email) {
        String regex = "[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public static boolean checkName(String name) {
        String regex = "(([\\u4E00-\\u9FA5]{1,15})|([a-zA-Z]{1,15}))";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(name);
        return m.matches();
    }

    public static boolean checkSchoolName(String schoolName) {
        String regex = "^[a-zA-Z0-9-()\\u4e00-\\u9fa5]+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(schoolName);
        return m.matches();
    }
}
