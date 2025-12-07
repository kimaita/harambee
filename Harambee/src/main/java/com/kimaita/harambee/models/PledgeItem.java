package com.kimaita.harambee.models;

import java.time.OffsetDateTime;

public class PledgeItem extends BaseModel {

    private int pledgeId;
    private int itemId;
    private String itemName;
    private int quantity;
    private String units;
    private OffsetDateTime deliveryDate;
    private String status;
    private String donorName;
    private String itemsSummary;

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getItemsSummary() {
        return itemsSummary;
    }

    public void setItemsSummary(String itemsSummary) {
        this.itemsSummary = itemsSummary;
    }

    public PledgeItem(String itemName, int quantity, String units) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.units = units;
    }

    public int getPledgeId() {
        return pledgeId;
    }

    public void setPledgeId(int pledgeId) {
        this.pledgeId = pledgeId;
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

    public OffsetDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(OffsetDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
