package com.example.maidmanager.maid.dto;

import com.example.maidmanager.employment.dto.EmploymentConfigDto;
import com.example.maidmanager.employment.entity.EmploymentConfig;
import com.example.maidmanager.maid.entity.Maid;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class MaidDetailDto {

    private UUID id;
    private String name;
    private String phone;
    private LocalDate joiningDate;
    private LocalDate leavingDate;
    private String notes;
    private boolean isActive;
    private EmploymentConfigDto currentConfig;
    private Instant createdAt;

    public MaidDetailDto() {
    }

    public static MaidDetailDto fromEntity(Maid maid, EmploymentConfig currentConfig) {
        MaidDetailDto dto = new MaidDetailDto();
        dto.setId(maid.getId());
        dto.setName(maid.getName());
        dto.setPhone(maid.getPhone());
        dto.setJoiningDate(maid.getJoiningDate());
        dto.setLeavingDate(maid.getLeavingDate());
        dto.setNotes(maid.getNotes());
        dto.setActive(maid.isActive());
        dto.setCurrentConfig(EmploymentConfigDto.fromEntity(currentConfig));
        dto.setCreatedAt(maid.getCreatedAt());
        return dto;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public EmploymentConfigDto getCurrentConfig() {
        return currentConfig;
    }

    public void setCurrentConfig(EmploymentConfigDto currentConfig) {
        this.currentConfig = currentConfig;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
