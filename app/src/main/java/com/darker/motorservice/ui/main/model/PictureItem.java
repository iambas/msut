package com.darker.motorservice.ui.main.model;

public class PictureItem {

    private String name;
    private byte[] picture;

    public PictureItem(){}

    public PictureItem(String name, byte[] picture){
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
