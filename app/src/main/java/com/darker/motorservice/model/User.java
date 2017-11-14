package com.darker.motorservice.model;

public class User {

    private String name, photo;

    public User(){}

    public User(String name, String photo){
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
