package com.example.grocery.models;

public class ModelPost {

    private String postId, postTitle, postDescription, postImage, timestamp, uid;

    public ModelPost() {

    }

    public ModelPost(String postId, String postTitle, String postDescription, String postImage, String timestamp, String uid) {
        this.postId = postId;
        this.postTitle = postTitle;
        this.postDescription = postDescription;
        this.postImage = postImage;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    public String getPostId() {
        return postId;
    }

    public void setProductId(String productId) {
        this.postId = postId;
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
