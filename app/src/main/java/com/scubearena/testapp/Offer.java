package com.scubearena.testapp;

public class Offer {
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private String imageUrl;

    public Offer(String text, String imageUrl) {
        this.text = text;
        this.imageUrl = imageUrl;
    }

    public Offer()
    {

    }

}

