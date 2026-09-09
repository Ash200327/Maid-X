package com.example.maidmanager.leave.dto;

import com.example.maidmanager.common.enums.LeaveType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class CreateLeaveRequest {

    @NotNull(message = "Leave date is required")
    private LocalDate leaveDate;

    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    private String note;

    public CreateLeaveRequest() {
    }

    public CreateLeaveRequest(LocalDate leaveDate, LeaveType leaveType, String note) {
        this.leaveDate = leaveDate;
        this.leaveType = leaveType;
        this.note = note;
    }

    public LocalDate getLeaveDate() {
        return leaveDate;
    }

    public void setLeaveDate(LocalDate leaveDate) {
        this.leaveDate = leaveDate;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(LeaveType leaveType) {
        this.leaveType = leaveType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
