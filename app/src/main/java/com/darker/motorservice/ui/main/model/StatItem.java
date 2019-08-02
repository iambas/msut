package com.darker.motorservice.ui.main.model;

public class StatItem {
    private String date, numCall, numChat;

    public StatItem(){}

    public StatItem(String date, String numCall, String numChat){
        this.date = date;
        this.numCall = numCall;
        this.numChat = numChat;
    }

    public String getDate() {
        return date;
    }

    public String getNumCall() {
        return numCall;
    }

    public String getNumChat() {
        return numChat;
    }

    @Override
    public String toString(){
        return date;
    }
}
