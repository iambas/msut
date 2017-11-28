package com.darker.motorservice.utility;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Darker on 15/11/60.
 */

public class StringUtil {
    private static final String PHONE_PATTERN = "\\d{9,10}";
    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public StringUtil() {
    }

    @SuppressLint("SimpleDateFormat")
    public static String getDateFormate(String pattern) {
        Date date = new Date();
        return new SimpleDateFormat(pattern).format(date);
    }

    public static boolean stringOk(String string) {
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
