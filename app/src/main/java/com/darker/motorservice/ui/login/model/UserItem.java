package com.darker.motorservice.ui.login.model;

public class UserItem {

    private String name, photo;

    public UserItem(){}

    public UserItem(String name, String photo){
        this.name = name;
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public String getPhoto() {
        return photo;
    }
}
