package com.darker.motorservice.utils;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Darker on 15/11/60.
 */

public class StringUtils {
    public StringUtils(){}

    @SuppressLint("SimpleDateFormat")
    public static String getDateFormate(String pattern) {
        Date date = new Date();
        return new SimpleDateFormat(pattern).format(date);
    }

    public static boolean stringOk(String string) {
        if (string == null) return false;
        if (string.equals("")) return false;
        return true;
    }
}
