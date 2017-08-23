package com.darker.motorservice.data;

import android.graphics.Bitmap;

public class ChatMessage {
    private String sender, message, date, status, read;
    private Bitmap bitmap;

    public ChatMessage(){}

    public ChatMessage(String date, String sender, String message, String status, String read) {
        this.sender = sender;
        this.message = message;
        this.date = date;
        this.status = status;
        this.read = read;
    }

    public String getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public String getStatus() {
        return status;
    }

    public String getRead() {
        return read;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    public String toString() {
        return date;
    }
}
