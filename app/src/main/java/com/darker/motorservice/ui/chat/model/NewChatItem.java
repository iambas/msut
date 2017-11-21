package com.darker.motorservice.ui.chat.model;

public class NewChatItem {
    private String service, user;

    public NewChatItem(){}

    public NewChatItem(String service, String user){
        this.service  = service;
        this.user = user;
    }

    public String getService() {
        return service;
    }

    public String getUser() {
        return user;
    }
}
