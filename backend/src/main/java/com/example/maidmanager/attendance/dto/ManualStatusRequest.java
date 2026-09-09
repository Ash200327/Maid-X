package com.example.maidmanager.attendance.dto;

import com.example.maidmanager.common.enums.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class ManualStatusRequest {

    @NotNull(message = "Business date is required")
    private LocalDate businessDate;

    @NotNull(message = "Status is required")
    private AttendanceStatus status;

    private String note;

    public ManualStatusRequest() {
    }

    public ManualStatusRequest(LocalDate businessDate, AttendanceStatus status, String note) {
        this.businessDate = businessDate;
        this.status = status;
        this.note = note;
    }

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
