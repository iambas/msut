package com.darker.motorservice.ui.chat.model;

import android.annotation.SuppressLint;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Darker on 30/11/60.
 */

public class DateTimeItem {
    private int year;
    private int month;
    private int day;
    private int hour;
    private int minute;

    public static DateTimeItem getDateTimeItem(String strDate){
        DateTimeItem item = new DateTimeItem();

        @SuppressLint("SimpleDateFormat")
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = dateFormat.parse(strDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            item.setYear(calendar.get(Calendar.YEAR));
            item.setMonth(calendar.get(Calendar.MONTH));
            item.setDay(calendar.get(Calendar.DATE));
            item.setHour(calendar.get(Calendar.HOUR_OF_DAY));
            item.setMinute(calendar.get(Calendar.MINUTE));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return item;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getMinute() {
        return minute;
    }

    public void setMinute(int minute) {
        this.minute = minute;
    }
}
