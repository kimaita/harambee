package com.kimaita.harambee.models;


import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class DonationPledge extends BaseModel {

    private int userId;
    private String description;
    private OffsetDateTime createdAt;
    // Helper to hold items before saving
    private List<PledgeItem> items = new ArrayList<>();

    private String DonorName;
    private String itemsSummary;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<PledgeItem> getItems() {
        return items;
    }

    public void setItems(List<PledgeItem> items) {
        this.items = items;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDonorName() {
        return DonorName;
    }

    public void setDonorName(String donorName) {
        DonorName = donorName;
    }

    public String getItemsSummary() {
        return itemsSummary;
    }

    public void setItemsSummary(String itemsSummary) {
        this.itemsSummary = itemsSummary;
    }
}

