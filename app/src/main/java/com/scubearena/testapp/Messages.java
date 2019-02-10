package com.scubearena.testapp;

public class Messages {

    private String messages;
    private String type;
    private long  time;
    private boolean seen;
    private String from;
    private String to;


    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }


    public Messages(String from) {
        this.from = from;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Messages(String message, String type, long time, boolean seen) {
        this.messages= message;
        this.type = type;
        this.time = time;
        this.seen = seen;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public Messages(){

    }

}
