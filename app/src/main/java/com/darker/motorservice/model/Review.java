package com.darker.motorservice.model;

public class Review {

    private String msg, date;
    private float rate;

    public Review(){}

    public Review(String msg, String date, float rate){
        this.msg = msg;
        this.date = date;
        this.rate = rate;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setRate(float rate) {
        this.rate = rate;
    }

    public float getRate() {
        return rate;
    }

    @Override
    public String toString() {
        return date;
    }
}
