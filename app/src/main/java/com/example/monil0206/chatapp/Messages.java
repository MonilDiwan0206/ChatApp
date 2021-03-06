package com.example.monil0206.chatapp;

public class Messages {
    private String message,type;
    private long time;
    private boolean seen;
    private String from;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }




    public Messages(){

    }

    public Messages(String message, boolean seen, String type, long time, String from) {
        this.message = message;
        this.seen = seen;
        this.type = type;
        this.time = time;
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
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
}
