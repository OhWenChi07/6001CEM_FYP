package com.example.grocery.admin;

public class AdminModelOrders {

    String orderId, orderTime, orderStatus, orderCost, orderBy, orderTo;

    public AdminModelOrders() {

    }

    public AdminModelOrders (String orderId, String orderTime, String orderStatus, String orderCost) {
        this.orderId = orderId;
        this.orderTime = orderTime;
        this.orderStatus = orderStatus;
        this.orderCost = orderCost;
        this.orderBy = orderBy;
        this.orderTo = orderTo;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId (String orderId) {
        this.orderId = orderId;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime (String orderTime) {
        this.orderTime = orderTime;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus (String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getOrderCost() {
        return orderCost;
    }

    public void setOrderCost (String orderCost) {
        this.orderCost = orderCost;
    }

    public String getOrderBy() {
        return orderBy;
    }

    public void setOrderBy (String orderBy) {
        this.orderBy = orderBy;
    }

    public String getOrderTo() {
        return orderTo;
    }

    public void setOrderTo (String orderTo) {
        this.orderTo = orderTo;
    }
}
