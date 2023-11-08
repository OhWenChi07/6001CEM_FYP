package com.example.grocery.models;

public class ModelCommentPost {
    //use same spellings of variables as used in sending to firebase
    String uid, comment, timestamp;

    public ModelCommentPost() {

    }

    public ModelCommentPost (String uid, String comment, String timestamp) {
        this.uid = uid;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid (String uid) {
        this.uid = uid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment (String comment) {
        this.comment = comment;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp (String timestamp) {
        this.timestamp = timestamp;
    }
}
