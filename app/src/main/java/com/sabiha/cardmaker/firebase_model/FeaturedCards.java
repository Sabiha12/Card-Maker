package com.sabiha.cardmaker.firebase_model;

public class FeaturedCards {
    private String image, pid;

    public FeaturedCards() {

    }

    public FeaturedCards(String image, String pid) {
        this.image = image;
        this.pid = pid;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

}
