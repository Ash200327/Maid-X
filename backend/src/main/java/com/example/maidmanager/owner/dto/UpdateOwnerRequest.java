package com.example.maidmanager.owner.dto;

import jakarta.validation.constraints.Size;

public class UpdateOwnerRequest {

    @Size(max = 120, message = "Name must not exceed 120 characters")
    private String name;

    @Size(max = 30, message = "Phone must not exceed 30 characters")
    private String phone;

    @Size(max = 64, message = "Timezone must not exceed 64 characters")
    private String timezone;

    @Size(min = 3, max = 3, message = "Currency code must be 3 characters")
    private String currencyCode;

    public UpdateOwnerRequest() {
    }

    public UpdateOwnerRequest(String name, String phone, String timezone, String currencyCode) {
        this.name = name;
        this.phone = phone;
        this.timezone = timezone;
        this.currencyCode = currencyCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
