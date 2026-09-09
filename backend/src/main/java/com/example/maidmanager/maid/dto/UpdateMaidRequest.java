package com.example.maidmanager.maid.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdateMaidRequest {

    @Size(max = 120, message = "Worker name must not exceed 120 characters")
    private String name;

    @Size(max = 30, message = "Phone must not exceed 30 characters")
    private String phone;

    private LocalDate joiningDate;

    private LocalDate leavingDate;

    private String notes;

    private Boolean isActive;

    public UpdateMaidRequest() {
    }

    public UpdateMaidRequest(String name, String phone, LocalDate joiningDate, LocalDate leavingDate, String notes, Boolean isActive) {
        this.name = name;
        this.phone = phone;
        this.joiningDate = joiningDate;
        this.leavingDate = leavingDate;
        this.notes = notes;
        this.isActive = isActive;
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

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public LocalDate getLeavingDate() {
        return leavingDate;
    }

    public void setLeavingDate(LocalDate leavingDate) {
        this.leavingDate = leavingDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
