package com.darker.motorservice.data;

public class NewChat {
    private String service, user;

    public NewChat(){}

    public NewChat(String service, String user){
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
