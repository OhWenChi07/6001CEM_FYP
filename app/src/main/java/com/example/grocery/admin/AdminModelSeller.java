package com.example.grocery.admin;

public class AdminModelSeller {

    private String sellerId, name, shopName, email, phoneNumber, timestamp, uid;

    public AdminModelSeller() {

    }

    public AdminModelSeller(String sellerId, String shopName, String name, String email, String phoneNumber, String timestamp, String uid) {
        this.sellerId = sellerId;
        this.shopName = shopName;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.timestamp = timestamp;
        this.uid = uid;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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
