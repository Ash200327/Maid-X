package com.example.maidmanager.attendance.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class DashboardResponse {

    private LocalDate date;
    private int activeMaidCount;
    private List<DashboardMaidEntryDto> entries;
    private CurrentMonthSnapshot currentMonth;

    public DashboardResponse() {
    }

    public DashboardResponse(LocalDate date, int activeMaidCount, List<DashboardMaidEntryDto> entries, CurrentMonthSnapshot currentMonth) {
        this.date = date;
        this.activeMaidCount = activeMaidCount;
        this.entries = entries;
        this.currentMonth = currentMonth;
    }

    public static class CurrentMonthSnapshot {
        private BigDecimal salaryPayable;
        private String currency;

        public CurrentMonthSnapshot() {
        }

        public CurrentMonthSnapshot(BigDecimal salaryPayable, String currency) {
            this.salaryPayable = salaryPayable;
            this.currency = currency;
        }

        public BigDecimal getSalaryPayable() {
            return salaryPayable;
        }

        public void setSalaryPayable(BigDecimal salaryPayable) {
            this.salaryPayable = salaryPayable;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getActiveMaidCount() {
        return activeMaidCount;
    }

    public void setActiveMaidCount(int activeMaidCount) {
        this.activeMaidCount = activeMaidCount;
    }

    public List<DashboardMaidEntryDto> getEntries() {
        return entries;
    }

    public void setEntries(List<DashboardMaidEntryDto> entries) {
        this.entries = entries;
    }

    public CurrentMonthSnapshot getCurrentMonth() {
        return currentMonth;
    }

    public void setCurrentMonth(CurrentMonthSnapshot currentMonth) {
        this.currentMonth = currentMonth;
    }
}
