package com.ciandt.digitalday.friendlychat;

/**
 * Created by felipearimateia on 01/09/16.
 */

public class Message {

    private String id;
    private String text;
    private String name;
    private String avatar;
    private String photo;
    private int typeMessage;

    public Message() {
    }

    public Message(String text, String name, String avatar) {
        this.text = text;
        this.name = name;
        this.avatar = avatar;
        this.typeMessage = 1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getTypeMessage() {
        return typeMessage;
    }

    public void setTypeMessage(int typeMessage) {
        this.typeMessage = typeMessage;
    }
}
