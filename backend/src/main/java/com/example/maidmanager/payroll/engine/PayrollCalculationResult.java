package com.example.maidmanager.payroll.engine;

import com.example.maidmanager.common.enums.SalaryMode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PayrollCalculationResult {

    private String month;
    private SalaryMode salaryMode;
    private BigDecimal configuredSalary;
    private int fullMonthExpectedWorkingDays;
    private int eligibleWorkingDays;
    private int workedDays;
    private int paidLeaveDays;
    private int unpaidLeaveDays;
    private int expectedMinutes;
    private int actualWorkedMinutes;
    private int shortfallMinutes;
    private int thresholdCrossedDays;
    private int overtimeMinutes;
    private BigDecimal baseEarnings;
    private BigDecimal shortfallDeduction;
    private BigDecimal overtimePay;
    private BigDecimal additions;
    private BigDecimal deductions;
    private BigDecimal finalPayable;
    private String currency;
    private List<String> warnings = new ArrayList<>();
    private List<DailyCalculationResult> dailyBreakdowns = new ArrayList<>();

    public PayrollCalculationResult() {
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public SalaryMode getSalaryMode() {
        return salaryMode;
    }

    public void setSalaryMode(SalaryMode salaryMode) {
        this.salaryMode = salaryMode;
    }

    public BigDecimal getConfiguredSalary() {
        return configuredSalary;
    }

    public void setConfiguredSalary(BigDecimal configuredSalary) {
        this.configuredSalary = configuredSalary;
    }

    public int getFullMonthExpectedWorkingDays() {
        return fullMonthExpectedWorkingDays;
    }

    public void setFullMonthExpectedWorkingDays(int fullMonthExpectedWorkingDays) {
        this.fullMonthExpectedWorkingDays = fullMonthExpectedWorkingDays;
    }

    public int getEligibleWorkingDays() {
        return eligibleWorkingDays;
    }

    public void setEligibleWorkingDays(int eligibleWorkingDays) {
        this.eligibleWorkingDays = eligibleWorkingDays;
    }

    public int getWorkedDays() {
        return workedDays;
    }

    public void setWorkedDays(int workedDays) {
        this.workedDays = workedDays;
    }

    public int getPaidLeaveDays() {
        return paidLeaveDays;
    }

    public void setPaidLeaveDays(int paidLeaveDays) {
        this.paidLeaveDays = paidLeaveDays;
    }

    public int getUnpaidLeaveDays() {
        return unpaidLeaveDays;
    }

    public void setUnpaidLeaveDays(int unpaidLeaveDays) {
        this.unpaidLeaveDays = unpaidLeaveDays;
    }

    public int getExpectedMinutes() {
        return expectedMinutes;
    }

    public void setExpectedMinutes(int expectedMinutes) {
        this.expectedMinutes = expectedMinutes;
    }

    public int getActualWorkedMinutes() {
        return actualWorkedMinutes;
    }

    public void setActualWorkedMinutes(int actualWorkedMinutes) {
        this.actualWorkedMinutes = actualWorkedMinutes;
    }

    public int getShortfallMinutes() {
        return shortfallMinutes;
    }

    public void setShortfallMinutes(int shortfallMinutes) {
        this.shortfallMinutes = shortfallMinutes;
    }

    public int getThresholdCrossedDays() {
        return thresholdCrossedDays;
    }

    public void setThresholdCrossedDays(int thresholdCrossedDays) {
        this.thresholdCrossedDays = thresholdCrossedDays;
    }

    public int getOvertimeMinutes() {
        return overtimeMinutes;
    }

    public void setOvertimeMinutes(int overtimeMinutes) {
        this.overtimeMinutes = overtimeMinutes;
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

    public List<String> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<DailyCalculationResult> getDailyBreakdowns() {
        return dailyBreakdowns;
    }

    public void setDailyBreakdowns(List<DailyCalculationResult> dailyBreakdowns) {
        this.dailyBreakdowns = dailyBreakdowns;
    }
}
