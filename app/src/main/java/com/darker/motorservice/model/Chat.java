package com.darker.motorservice.model;

public class Chat {

    private String keyChat, chatWithId, chatWithName, message, date, read, status, photo;

    public Chat(){}

    public Chat(String keyChat, String chatWithId, String chatWithName, String message, String date, String read, String status, String photo){
        this.keyChat = keyChat;
        this.chatWithId = chatWithId;
        this.chatWithName = chatWithName;
        this.message = message;
        this.date = date;
        this.read = read;
        this.status = status;
        this.photo = photo;
    }

    public String getKeyChat() {
        return keyChat;
    }

    public String getChatWithId() {
        return chatWithId;
    }

    public String getChatWithName() {
        return chatWithName;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public String getRead() {
        return read;
    }

    public String getStatus() {
        return status;
    }

    public String getPhoto() {
        return photo;
    }

    @Override
    public String toString() {
        return this.date;
    }
}
