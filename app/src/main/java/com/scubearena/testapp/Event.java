package com.scubearena.testapp;

public class Event {
    private int imageId;
    private String title;
    private String date;

    public Event(int imageId, String title, String date) {
        this.imageId = imageId;
        this.title = title;
        this.date = date;
    }
    public Event() {
    }
    public int getImageId() {
        return imageId;
    }
    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String desc) {
        this.date = desc;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    @Override
    public String toString() {
        return title + "\n" + date;
    }


}
