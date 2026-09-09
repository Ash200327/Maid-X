package com.example.maidmanager.employment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(
    name = "maid_working_days",
    uniqueConstraints = @UniqueConstraint(columnNames = {"employment_config_id", "weekday"})
)
public class MaidWorkingDay {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employment_config_id", nullable = false)
    private EmploymentConfig employmentConfig;

    @Column(name = "weekday", nullable = false)
    private short weekday; // 1 = Monday ... 7 = Sunday

    public MaidWorkingDay() {
    }

    public MaidWorkingDay(UUID id, EmploymentConfig employmentConfig, short weekday) {
        this.id = id != null ? id : UUID.randomUUID();
        this.employmentConfig = employmentConfig;
        this.weekday = weekday;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public EmploymentConfig getEmploymentConfig() {
        return employmentConfig;
    }

    public void setEmploymentConfig(EmploymentConfig employmentConfig) {
        this.employmentConfig = employmentConfig;
    }

    public short getWeekday() {
        return weekday;
    }

    public void setWeekday(short weekday) {
        this.weekday = weekday;
    }
}
