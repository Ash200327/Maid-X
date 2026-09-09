package com.example.maidmanager.maid.dto;

import com.example.maidmanager.common.enums.SalaryMode;
import com.example.maidmanager.employment.entity.EmploymentConfig;
import com.example.maidmanager.maid.entity.Maid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class MaidSummaryDto {

    private UUID id;
    private String name;
    private String phone;
    private LocalDate joiningDate;
    private LocalDate leavingDate;
    private boolean isActive;
    private SalaryMode activeSalaryMode;
    private BigDecimal activeSalaryAmount;

    public MaidSummaryDto() {
    }

    public static MaidSummaryDto fromEntity(Maid maid, EmploymentConfig currentConfig) {
        MaidSummaryDto dto = new MaidSummaryDto();
        dto.setId(maid.getId());
        dto.setName(maid.getName());
        dto.setPhone(maid.getPhone());
        dto.setJoiningDate(maid.getJoiningDate());
        dto.setLeavingDate(maid.getLeavingDate());
        dto.setActive(maid.isActive());
        if (currentConfig != null) {
            dto.setActiveSalaryMode(currentConfig.getSalaryMode());
            dto.setActiveSalaryAmount(currentConfig.getSalaryAmount());
        }
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public SalaryMode getActiveSalaryMode() {
        return activeSalaryMode;
    }

    public void setActiveSalaryMode(SalaryMode activeSalaryMode) {
        this.activeSalaryMode = activeSalaryMode;
    }

    public BigDecimal getActiveSalaryAmount() {
        return activeSalaryAmount;
    }

    public void setActiveSalaryAmount(BigDecimal activeSalaryAmount) {
        this.activeSalaryAmount = activeSalaryAmount;
    }
}
