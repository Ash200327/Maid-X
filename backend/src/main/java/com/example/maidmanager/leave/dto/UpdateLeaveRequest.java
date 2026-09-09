package com.example.maidmanager.leave.dto;

import com.example.maidmanager.common.enums.LeaveType;

public class UpdateLeaveRequest {

    private LeaveType leaveType;
    private String note;

    public UpdateLeaveRequest() {
    }

    public UpdateLeaveRequest(LeaveType leaveType, String note) {
        this.leaveType = leaveType;
        this.note = note;
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
