package com.kimaita.harambee.models;

import java.time.OffsetDateTime;

public class User extends BaseModel {

    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private UserType role;
    private EntityType entityType;
    private OffsetDateTime createdAt;

    public User() {}
    public User(String name, String email, String phoneNumber, String address, UserType role, EntityType entityType) {
        super();
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.entityType = entityType;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public UserType getRole() { return role; }
    public void setRole(UserType role) { this.role = role; }
    public EntityType getEntityType() { return entityType; }
    public void setEntityType(EntityType entityType) { this.entityType = entityType; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() { return name + " (" + role + ")"; }
}