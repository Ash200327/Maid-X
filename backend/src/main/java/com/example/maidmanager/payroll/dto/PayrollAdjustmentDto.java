package com.example.maidmanager.payroll.dto;

import com.example.maidmanager.common.enums.PayrollAdjustmentType;
import com.example.maidmanager.payroll.entity.PayrollAdjustment;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class PayrollAdjustmentDto {

    private UUID id;
    private PayrollAdjustmentType type;
    private BigDecimal amount;
    private LocalDate adjustmentDate;
    private String reason;
    private Instant createdAt;

    public PayrollAdjustmentDto() {
    }

    public PayrollAdjustmentDto(UUID id, PayrollAdjustmentType type, BigDecimal amount,
                                LocalDate adjustmentDate, String reason, Instant createdAt) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.adjustmentDate = adjustmentDate;
        this.reason = reason;
        this.createdAt = createdAt;
    }

    public static PayrollAdjustmentDto fromEntity(PayrollAdjustment entity) {
        if (entity == null) {
            return null;
        }
        return new PayrollAdjustmentDto(
                entity.getId(),
                entity.getType(),
                entity.getAmount(),
                entity.getAdjustmentDate(),
                entity.getReason(),
                entity.getCreatedAt()
        );
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PayrollAdjustmentType getType() {
        return type;
    }

    public void setType(PayrollAdjustmentType type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getAdjustmentDate() {
        return adjustmentDate;
    }

    public void setAdjustmentDate(LocalDate adjustmentDate) {
        this.adjustmentDate = adjustmentDate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
