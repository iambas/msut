package com.darker.motorservice.data;

public class Pictures {

    private String name;
    private byte[] picture;

    public Pictures(){}

    public Pictures(String name, byte[] picture){
        this.name = name;
        this.picture = picture;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public String getName() {
        return name;
    }

    public byte[] getPicture() {
        return picture;
    }
}
