package com.darker.motorservice.data;

public class Stat {
    private String date, numCall, numChat;

    public Stat(){}

    public Stat(String date, String numCall, String numChat){
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
