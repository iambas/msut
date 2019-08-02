package com.darker.motorservice.utility;

import android.annotation.SuppressLint;
import android.content.Context;

import com.darker.motorservice.R;
import com.darker.motorservice.ui.chat.model.DateTimeItem;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Darker on 28/11/60.
 */

public class DateUtil {

    public class DatePattern{
        public static final String YEAR_MONTH = "yyyy-MM";
        public static final String DATE = "dd-MM-yyyy";
        public static final String TIME_DATE = "yyyy-MM-dd HH:mm:ss";
    }

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

    public static String getDateTime(Context context, String strDate){
        DateTimeItem item = DateTimeItem.getDateTimeItem(strDate);
        String[] months = context.getResources().getStringArray(R.array.short_month);
        return item.getDay() + " " + months[item.getMonth()] + " " + (item.getYear() + 543);
    }

    public static String getTimeChat(String strDate){
        DateTimeItem item = DateTimeItem.getDateTimeItem(strDate);
        return getTimeTwoLetter(item.getHour()) + ":" + getTimeTwoLetter(item.getMinute());
    }

    public static boolean isDayDiffer(String strDate1, String strDAte2){
        String[] date1 = strDate1.split(" ");
        String[] date2 = strDAte2.split(" ");
        return date1[0].equals(date2[0]);
    }

    private static String getTimeTwoLetter(int number){
        return number > 10 ? "0" + number : "" + number;
    }
}
