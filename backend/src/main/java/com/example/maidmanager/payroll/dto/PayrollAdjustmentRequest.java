package com.example.maidmanager.payroll.dto;

import com.example.maidmanager.common.enums.PayrollAdjustmentType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PayrollAdjustmentRequest {

    @NotNull(message = "Adjustment type is required.")
    private PayrollAdjustmentType type;

    @NotNull(message = "Adjustment amount is required.")
    @DecimalMin(value = "0.01", message = "Amount must be strictly greater than 0.")
    private BigDecimal amount;

    @NotNull(message = "Adjustment date is required.")
    private LocalDate adjustmentDate;

    @Size(max = 255, message = "Reason cannot exceed 255 characters.")
    private String reason;

    public PayrollAdjustmentRequest() {
    }

    public PayrollAdjustmentRequest(PayrollAdjustmentType type, BigDecimal amount, LocalDate adjustmentDate, String reason) {
        this.type = type;
        this.amount = amount;
        this.adjustmentDate = adjustmentDate;
        this.reason = reason;
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
}
