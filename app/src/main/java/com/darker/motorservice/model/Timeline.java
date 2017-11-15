package com.darker.motorservice.model;

import android.graphics.Bitmap;

public class Timeline {
    private String key, id, name, message, date, imgName;
    private Bitmap profile, image;

    public Timeline(){}

    public void setKey(String key) {
        this.key = key;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public void setProfile(Bitmap profile) {
        this.profile = profile;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getDate() {
        return date;
    }

    public Bitmap getImage() {
        return image;
    }

    public String getImgName() {
        return imgName;
    }

    public String getName() {
        return name;
    }

    public Bitmap getProfile() {
        return profile;
    }

    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    @Override
    public String toString() {
        return date;
    }
}
