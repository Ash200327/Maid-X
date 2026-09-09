package com.example.maidmanager.employment.dto;

import com.example.maidmanager.common.enums.SalaryMode;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public class CreateEmploymentConfigRequest {

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @NotNull(message = "Salary mode is required")
    private SalaryMode salaryMode;

    @NotNull(message = "Salary amount is required")
    @DecimalMin(value = "0.01", message = "Salary amount must be greater than 0")
    private BigDecimal salaryAmount;

    @NotNull(message = "Expected minutes per day is required")
    @Min(value = 1, message = "Expected minutes per day must be at least 1")
    private Integer expectedMinutesPerDay;

    @NotNull(message = "Shortfall threshold minutes is required")
    @Min(value = 0, message = "Shortfall threshold minutes cannot be negative")
    private Integer shortfallThresholdMinutes;

    private boolean overtimeEnabled = false;

    @DecimalMin(value = "1.000", message = "Overtime multiplier must be at least 1.0")
    private BigDecimal overtimeMultiplier = new BigDecimal("1.000");

    @NotEmpty(message = "At least one working day must be selected")
    private Set<@Min(1) @Max(7) Short> workingDays;

    public CreateEmploymentConfigRequest() {
    }

    public CreateEmploymentConfigRequest(LocalDate effectiveFrom, LocalDate effectiveTo,
                                         SalaryMode salaryMode, BigDecimal salaryAmount,
                                         Integer expectedMinutesPerDay, Integer shortfallThresholdMinutes,
                                         boolean overtimeEnabled, BigDecimal overtimeMultiplier,
                                         Set<Short> workingDays) {
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.salaryMode = salaryMode;
        this.salaryAmount = salaryAmount;
        this.expectedMinutesPerDay = expectedMinutesPerDay;
        this.shortfallThresholdMinutes = shortfallThresholdMinutes;
        this.overtimeEnabled = overtimeEnabled;
        this.overtimeMultiplier = overtimeMultiplier != null ? overtimeMultiplier : new BigDecimal("1.000");
        this.workingDays = workingDays;
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
}
