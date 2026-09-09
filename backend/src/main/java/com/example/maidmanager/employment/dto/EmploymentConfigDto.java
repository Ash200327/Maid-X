package com.example.maidmanager.employment.dto;

import com.example.maidmanager.common.enums.SalaryMode;
import com.example.maidmanager.employment.entity.EmploymentConfig;
import com.example.maidmanager.employment.entity.MaidWorkingDay;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class EmploymentConfigDto {

    private UUID id;
    private UUID maidId;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
    private SalaryMode salaryMode;
    private BigDecimal salaryAmount;
    private Integer expectedMinutesPerDay;
    private Integer shortfallThresholdMinutes;
    private boolean overtimeEnabled;
    private BigDecimal overtimeMultiplier;
    private Set<Short> workingDays;
    private Instant createdAt;

    public EmploymentConfigDto() {
    }

    public static EmploymentConfigDto fromEntity(EmploymentConfig config) {
        if (config == null) {
            return null;
        }
        EmploymentConfigDto dto = new EmploymentConfigDto();
        dto.setId(config.getId());
        dto.setMaidId(config.getMaid().getId());
        dto.setEffectiveFrom(config.getEffectiveFrom());
        dto.setEffectiveTo(config.getEffectiveTo());
        dto.setSalaryMode(config.getSalaryMode());
        dto.setSalaryAmount(config.getSalaryAmount());
        dto.setExpectedMinutesPerDay(config.getExpectedMinutesPerDay());
        dto.setShortfallThresholdMinutes(config.getShortfallThresholdMinutes());
        dto.setOvertimeEnabled(config.isOvertimeEnabled());
        dto.setOvertimeMultiplier(config.getOvertimeMultiplier());
        if (config.getWorkingDays() != null) {
            dto.setWorkingDays(config.getWorkingDays().stream()
                    .map(MaidWorkingDay::getWeekday)
                    .collect(Collectors.toSet()));
        }
        dto.setCreatedAt(config.getCreatedAt());
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getMaidId() {
        return maidId;
    }

    public void setMaidId(UUID maidId) {
        this.maidId = maidId;
    }

    public LocalDate getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(LocalDate effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public LocalDate getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(LocalDate effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public SalaryMode getSalaryMode() {
        return salaryMode;
    }

    public void setSalaryMode(SalaryMode salaryMode) {
        this.salaryMode = salaryMode;
    }

    public BigDecimal getSalaryAmount() {
        return salaryAmount;
    }

    public void setSalaryAmount(BigDecimal salaryAmount) {
        this.salaryAmount = salaryAmount;
    }

    public Integer getExpectedMinutesPerDay() {
        return expectedMinutesPerDay;
    }

    public void setExpectedMinutesPerDay(Integer expectedMinutesPerDay) {
        this.expectedMinutesPerDay = expectedMinutesPerDay;
    }

    public Integer getShortfallThresholdMinutes() {
        return shortfallThresholdMinutes;
    }

    public void setShortfallThresholdMinutes(Integer shortfallThresholdMinutes) {
        this.shortfallThresholdMinutes = shortfallThresholdMinutes;
    }

    public boolean isOvertimeEnabled() {
        return overtimeEnabled;
    }

    public void setOvertimeEnabled(boolean overtimeEnabled) {
        this.overtimeEnabled = overtimeEnabled;
    }

    public BigDecimal getOvertimeMultiplier() {
        return overtimeMultiplier;
    }

    public void setOvertimeMultiplier(BigDecimal overtimeMultiplier) {
        this.overtimeMultiplier = overtimeMultiplier;
    }

    public Set<Short> getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(Set<Short> workingDays) {
        this.workingDays = workingDays;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
