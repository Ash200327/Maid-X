package com.example.maidmanager.payroll.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MonthlyReportResponse {

    private String month; // "YYYY-MM"
    private int totalMaids;
    private int draftCount;
    private int finalizedCount;
    private int paidCount;
    private BigDecimal totalPayable;
    private BigDecimal totalPaid;
    private String currency;
    private List<PayrollRunDto> runs = new ArrayList<>();

    public MonthlyReportResponse() {
    }

    public MonthlyReportResponse(String month, int totalMaids, int draftCount, int finalizedCount,
                                 int paidCount, BigDecimal totalPayable, BigDecimal totalPaid,
                                 String currency, List<PayrollRunDto> runs) {
        this.month = month;
        this.totalMaids = totalMaids;
        this.draftCount = draftCount;
        this.finalizedCount = finalizedCount;
        this.paidCount = paidCount;
        this.totalPayable = totalPayable;
        this.totalPaid = totalPaid;
        this.currency = currency;
        this.runs = runs;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public int getTotalMaids() {
        return totalMaids;
    }

    public void setTotalMaids(int totalMaids) {
        this.totalMaids = totalMaids;
    }

    public int getDraftCount() {
        return draftCount;
    }

    public void setDraftCount(int draftCount) {
        this.draftCount = draftCount;
    }

    public int getFinalizedCount() {
        return finalizedCount;
    }

    public void setFinalizedCount(int finalizedCount) {
        this.finalizedCount = finalizedCount;
    }

    public int getPaidCount() {
        return paidCount;
    }

    public void setPaidCount(int paidCount) {
        this.paidCount = paidCount;
    }

    public BigDecimal getTotalPayable() {
        return totalPayable;
    }

    public void setTotalPayable(BigDecimal totalPayable) {
        this.totalPayable = totalPayable;
    }

    public BigDecimal getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(BigDecimal totalPaid) {
        this.totalPaid = totalPaid;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public List<PayrollRunDto> getRuns() {
        return runs;
    }

    public void setRuns(List<PayrollRunDto> runs) {
        this.runs = runs;
    }
}
