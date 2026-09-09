package com.example.maidmanager.owner.dto;

import com.example.maidmanager.owner.entity.Owner;

import java.time.Instant;
import java.util.UUID;

public class OwnerDto {

    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String timezone;
    private String currencyCode;
    private Instant createdAt;

    public OwnerDto() {
    }

    public OwnerDto(UUID id, String name, String email, String phone, String timezone, String currencyCode, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.timezone = timezone;
        this.currencyCode = currencyCode;
        this.createdAt = createdAt;
    }

    public static OwnerDto fromEntity(Owner owner) {
        if (owner == null) {
            return null;
        }
        return new OwnerDto(
                owner.getId(),
                owner.getName(),
                owner.getEmail(),
                owner.getPhone(),
                owner.getTimezone(),
                owner.getCurrencyCode(),
                owner.getCreatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
