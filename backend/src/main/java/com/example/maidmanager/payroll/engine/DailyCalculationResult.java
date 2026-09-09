package com.example.maidmanager.payroll.engine;

import com.example.maidmanager.common.enums.LeaveType;

import java.time.LocalDate;

public class DailyCalculationResult {

    private LocalDate date;
    private boolean isExpectedWorkday;
    private boolean isHoliday;
    private boolean isLeave;
    private LeaveType leaveType;
    private int expectedMinutes;
    private int actualMinutes;
    private int shortfallMinutes;
    private boolean thresholdCrossed;
    private int overtimeMinutes;
    private boolean hasIncompleteSession;

    public DailyCalculationResult() {
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isExpectedWorkday() {
        return isExpectedWorkday;
    }

    public void setExpectedWorkday(boolean expectedWorkday) {
        isExpectedWorkday = expectedWorkday;
    }

    public boolean isHoliday() {
        return isHoliday;
    }

    public void setHoliday(boolean holiday) {
        isHoliday = holiday;
    }

    public boolean isLeave() {
        return isLeave;
    }

    public void setLeave(boolean leave) {
        isLeave = leave;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public int getExpectedMinutes() {
        return expectedMinutes;
    }

    public void setExpectedMinutes(int expectedMinutes) {
        this.expectedMinutes = expectedMinutes;
    }

    public int getActualMinutes() {
        return actualMinutes;
    }

    public void setActualMinutes(int actualMinutes) {
        this.actualMinutes = actualMinutes;
    }

    public int getShortfallMinutes() {
        return shortfallMinutes;
    }

    public void setShortfallMinutes(int shortfallMinutes) {
        this.shortfallMinutes = shortfallMinutes;
    }

    public boolean isThresholdCrossed() {
        return thresholdCrossed;
    }

    public void setThresholdCrossed(boolean thresholdCrossed) {
        this.thresholdCrossed = thresholdCrossed;
    }

    public int getOvertimeMinutes() {
        return overtimeMinutes;
    }

    public void setOvertimeMinutes(int overtimeMinutes) {
        this.overtimeMinutes = overtimeMinutes;
    }

    public boolean isHasIncompleteSession() {
        return hasIncompleteSession;
    }

    public void setHasIncompleteSession(boolean hasIncompleteSession) {
        this.hasIncompleteSession = hasIncompleteSession;
    }
}
