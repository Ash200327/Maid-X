package com.example.maidmanager.leave.dto;

import com.example.maidmanager.common.enums.LeaveType;
import com.example.maidmanager.leave.entity.LeaveRecord;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class LeaveRecordDto {

    private UUID id;
    private UUID maidId;
    private LocalDate leaveDate;
    private LeaveType leaveType;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;

    public LeaveRecordDto() {
    }

    public static LeaveRecordDto fromEntity(LeaveRecord record) {
        if (record == null) {
            return null;
        }
        LeaveRecordDto dto = new LeaveRecordDto();
        dto.setId(record.getId());
        dto.setMaidId(record.getMaid().getId());
        dto.setLeaveDate(record.getLeaveDate());
        dto.setLeaveType(record.getLeaveType());
        dto.setNote(record.getNote());
        dto.setCreatedAt(record.getCreatedAt());
        dto.setUpdatedAt(record.getUpdatedAt());
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
