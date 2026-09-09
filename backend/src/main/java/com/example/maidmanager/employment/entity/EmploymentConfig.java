package com.example.maidmanager.employment.entity;

import com.example.maidmanager.common.entity.BaseAuditableEntity;
import com.example.maidmanager.common.enums.SalaryMode;
import com.example.maidmanager.maid.entity.Maid;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "employment_configs")
public class EmploymentConfig extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maid_id", nullable = false)
    private Maid maid;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "salary_mode", nullable = false, length = 20)
    private SalaryMode salaryMode;

    @Column(name = "salary_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal salaryAmount;

    @Column(name = "expected_minutes_per_day", nullable = false)
    private Integer expectedMinutesPerDay;

    @Column(name = "shortfall_threshold_minutes", nullable = false)
    private Integer shortfallThresholdMinutes;

    @Column(name = "overtime_enabled", nullable = false)
    private boolean overtimeEnabled = false;

    @Column(name = "overtime_multiplier", nullable = false, precision = 6, scale = 3)
    private BigDecimal overtimeMultiplier = new BigDecimal("1.000");

    @OneToMany(mappedBy = "employmentConfig", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<MaidWorkingDay> workingDays = new HashSet<>();

    public EmploymentConfig() {
    }

    public EmploymentConfig(UUID id, Maid maid, LocalDate effectiveFrom, LocalDate effectiveTo,
                            SalaryMode salaryMode, BigDecimal salaryAmount,
                            Integer expectedMinutesPerDay, Integer shortfallThresholdMinutes,
                            boolean overtimeEnabled, BigDecimal overtimeMultiplier) {
        this.id = id != null ? id : UUID.randomUUID();
        this.maid = maid;
        this.effectiveFrom = effectiveFrom;
        this.effectiveTo = effectiveTo;
        this.salaryMode = salaryMode;
        this.salaryAmount = salaryAmount;
        this.expectedMinutesPerDay = expectedMinutesPerDay;
        this.shortfallThresholdMinutes = shortfallThresholdMinutes;
        this.overtimeEnabled = overtimeEnabled;
        this.overtimeMultiplier = overtimeMultiplier != null ? overtimeMultiplier : new BigDecimal("1.000");
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Maid getMaid() {
        return maid;
    }

    public void setMaid(Maid maid) {
        this.maid = maid;
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

    public Set<MaidWorkingDay> getWorkingDays() {
        return workingDays;
    }

    public void setWorkingDays(Set<MaidWorkingDay> workingDays) {
        this.workingDays = workingDays;
    }
}
