package com.darker.motorservice.ui.main.model;

public class ChatItem {
    private String keyChat, chatWithId, chatWithName, message, date, read, status, photo;

    public ChatItem() {
    }

    public void setKeyChat(String keyChat) {
        this.keyChat = keyChat;
    }

    public void setChatWithId(String chatWithId) {
        this.chatWithId = chatWithId;
    }

    public void setChatWithName(String chatWithName) {
        this.chatWithName = chatWithName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setRead(String read) {
        this.read = read;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPhoto(String photo) {
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
