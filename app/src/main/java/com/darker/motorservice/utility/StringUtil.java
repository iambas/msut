package com.darker.motorservice.utility;

/**
 * Created by Darker on 15/11/60.
 */

public class StringUtil {
    private static final String PHONE_PATTERN = "\\d{9,10}";
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public StringUtil() {
    }

    public static boolean isStringOk(String string) {
        if (string == null) return false;
        if (string.isEmpty()) return false;
        return true;
    }

    public static boolean isPhoneNumber(String textTest) {
        if (textTest == null) return false;
        return textTest.matches(PHONE_PATTERN);
    }

    public static boolean isEmail(String email) {
        if (email == null) {
            return false;
        }
        return email.matches(EMAIL_PATTERN);
    }
}
