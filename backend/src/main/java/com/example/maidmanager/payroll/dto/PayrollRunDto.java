package com.example.maidmanager.payroll.dto;

import com.example.maidmanager.common.enums.PayrollStatus;
import com.example.maidmanager.payroll.engine.PayrollCalculationResult;
import com.example.maidmanager.payroll.entity.PayrollRun;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PayrollRunDto {

    private UUID id;
    private UUID maidId;
    private String maidName;
    private String payrollMonth; // "YYYY-MM"
    private PayrollStatus status;
    private Instant finalizedAt;
    private Instant paidAt;
    private String paymentMethod;
    private String paymentNote;
    private BigDecimal baseEarnings;
    private BigDecimal shortfallDeduction;
    private BigDecimal overtimePay;
    private BigDecimal additions;
    private BigDecimal deductions;
    private BigDecimal finalPayable;
    private String currency;
    private List<PayrollAdjustmentDto> adjustments = new ArrayList<>();
    private PayrollCalculationResult calculation;

    public PayrollRunDto() {
    }

    public static PayrollRunDto fromEntity(PayrollRun run, PayrollCalculationResult calculation) {
        if (run == null) {
            return null;
        }
        PayrollRunDto dto = new PayrollRunDto();
        dto.setId(run.getId());
        dto.setMaidId(run.getMaid().getId());
        dto.setMaidName(run.getMaid().getName());
        dto.setPayrollMonth(run.getPayrollMonth().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        dto.setStatus(run.getStatus());
        dto.setFinalizedAt(run.getFinalizedAt());
        dto.setPaidAt(run.getPaidAt());
        dto.setPaymentMethod(run.getPaymentMethod());
        dto.setPaymentNote(run.getPaymentNote());

        if (run.getAdjustments() != null) {
            dto.setAdjustments(run.getAdjustments().stream()
                    .map(PayrollAdjustmentDto::fromEntity)
                    .collect(Collectors.toList()));
        }

        if (calculation != null) {
            dto.setCalculation(calculation);
            dto.setBaseEarnings(calculation.getBaseEarnings());
            dto.setShortfallDeduction(calculation.getShortfallDeduction());
            dto.setOvertimePay(calculation.getOvertimePay());
            dto.setAdditions(calculation.getAdditions());
            dto.setDeductions(calculation.getDeductions());
            dto.setFinalPayable(calculation.getFinalPayable());
            dto.setCurrency(calculation.getCurrency());
        }

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

    public String getMaidName() {
        return maidName;
    }

    public void setMaidName(String maidName) {
        this.maidName = maidName;
    }

    public String getPayrollMonth() {
        return payrollMonth;
    }

    public void setPayrollMonth(String payrollMonth) {
        this.payrollMonth = payrollMonth;
    }

    public PayrollStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollStatus status) {
        this.status = status;
    }

    public Instant getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(Instant finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentNote() {
        return paymentNote;
    }

    public void setPaymentNote(String paymentNote) {
        this.paymentNote = paymentNote;
    }

    public BigDecimal getBaseEarnings() {
        return baseEarnings;
    }

    public void setBaseEarnings(BigDecimal baseEarnings) {
        this.baseEarnings = baseEarnings;
    }

    public BigDecimal getShortfallDeduction() {
        return shortfallDeduction;
    }

    public void setShortfallDeduction(BigDecimal shortfallDeduction) {
        this.shortfallDeduction = shortfallDeduction;
    }

    public BigDecimal getOvertimePay() {
        return overtimePay;
    }

    public void setOvertimePay(BigDecimal overtimePay) {
        this.overtimePay = overtimePay;
    }

    public BigDecimal getAdditions() {
        return additions;
    }

    public void setAdditions(BigDecimal additions) {
        this.additions = additions;
    }

    public BigDecimal getDeductions() {
        return deductions;
    }

    public void setDeductions(BigDecimal deductions) {
        this.deductions = deductions;
    }

    public BigDecimal getFinalPayable() {
        return finalPayable;
    }

    public void setFinalPayable(BigDecimal finalPayable) {
        this.finalPayable = finalPayable;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<PayrollAdjustmentDto> getAdjustments() {
        return adjustments;
    }

    public void setAdjustments(List<PayrollAdjustmentDto> adjustments) {
        this.adjustments = adjustments;
    }

    public PayrollCalculationResult getCalculation() {
        return calculation;
    }

    public void setCalculation(PayrollCalculationResult calculation) {
        this.calculation = calculation;
    }
}
