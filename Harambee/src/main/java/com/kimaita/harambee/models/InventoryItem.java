package com.kimaita.harambee.models;

public class InventoryItem extends BaseModel {
    private String name;
    private String description;
    private int quantity;
    private String units;
    private int categoryId;
    private String categoryName;

    public InventoryItem(String name, String categoryName, String units) {
        this.name = name;
        this.categoryName = categoryName; //TODO In a real app, we'd look up the ID
        this.units = units;
        this.quantity = 0;
    }

    public InventoryItem() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}