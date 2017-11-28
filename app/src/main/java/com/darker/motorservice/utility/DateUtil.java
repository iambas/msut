package com.darker.motorservice.utility;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Darker on 28/11/60.
 */

public class DateUtil {
    public static int getCurrentMonth(){
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    @SuppressLint("SimpleDateFormat")
    public static String getDateFormat(String pattern) {
        Date date = new Date();
        return new SimpleDateFormat(pattern).format(date);
    }

    public static int getMonthFromChat(String date){
        return Integer.parseInt(date.split("-")[1]);
    }
}
