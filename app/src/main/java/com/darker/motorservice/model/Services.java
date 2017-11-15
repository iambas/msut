package com.darker.motorservice.model;

public class Services {
    private String id, name, pos, email, tel, photo, cover, latlng, workTime, service, distribute;
    private byte[] imgCover, imgProfile;

    public Services() {}

    public Services(String id, String name, String pos, String email, String tel,
                    String photo, String cover, String latlng, String workTime,
                    String service, String distribute) {
        this.id = id;
        this.name = name;
        this.pos = pos;
        this.email = email;
        this.tel = tel;
        this.photo = photo;
        this.cover = cover;
        this.latlng = latlng;
        this.workTime = workTime;
        this.service = service;
        this.distribute = distribute;
    }

    public void setImgCover(byte[] imgCover) {
        this.imgCover = imgCover;
    }

    public byte[] getImgCover() {
        return imgCover;
    }

    public void setImgProfile(byte[] imgProfile) {
        this.imgProfile = imgProfile;
    }

    public byte[] getImgProfile() {
        return imgProfile;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setLatlng(String latlng) {
        this.latlng = latlng;
    }

    public void setWorkTime(String workTime) {
        this.workTime = workTime;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setDistribute(String distribute) {
        this.distribute = distribute;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getCover() {
        return cover;
    }

    public String getDistribute() {
        return distribute;
    }

    public String getLatlng() {
        return latlng;
    }

    public String getPos() {
        return pos;
    }

    public String getPhoto() {
        return photo;
    }

    public String getService() {
        return service;
    }

    public String getTel() {
        return tel;
    }

    public String getWorkTime() {
        return workTime;
    }
}
