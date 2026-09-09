package com.example.maidmanager.attendance.dto;

import com.example.maidmanager.common.enums.AttendanceStatus;

import java.time.Instant;

public class UpdateAttendanceSessionRequest {

    private Instant entryAt;
    private Instant exitAt;
    private AttendanceStatus status;
    private String note;

    public UpdateAttendanceSessionRequest() {
    }

    public UpdateAttendanceSessionRequest(Instant entryAt, Instant exitAt, AttendanceStatus status, String note) {
        this.entryAt = entryAt;
        this.exitAt = exitAt;
        this.status = status;
        this.note = note;
    }

    public Instant getEntryAt() {
        return entryAt;
    }

    public void setEntryAt(Instant entryAt) {
        this.entryAt = entryAt;
    }

    public Instant getExitAt() {
        return exitAt;
    }

    public void setExitAt(Instant exitAt) {
        this.exitAt = exitAt;
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
