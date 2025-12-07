package com.kimaita.harambee.models;

import java.time.LocalDate;

public class RequestItem extends BaseModel {
    private int requestId;
    private int itemId;
    private String itemName;
    private int quantity;
    private String units;
    private LocalDate dateNeeded;
    private int fulfilledQuantity;

    public RequestItem(String itemName, int quantity, String units) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.units = units;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public LocalDate getDateNeeded() {
        return dateNeeded;
    }

    public void setDateNeeded(LocalDate dateNeeded) {
        this.dateNeeded = dateNeeded;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getFulfilledQuantity() {
        return fulfilledQuantity;
    }

    public void setFulfilledQuantity(int fulfilledQuantity) {
        this.fulfilledQuantity = fulfilledQuantity;
    }

    // Helper to calculate remaining needed
    public int getRemainingQuantity() {
        return Math.max(0, quantity - fulfilledQuantity);
    }
}