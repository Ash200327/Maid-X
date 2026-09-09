package com.example.maidmanager.attendance.dto;

import com.example.maidmanager.attendance.entity.AttendanceSession;
import com.example.maidmanager.common.enums.AttendanceStatus;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class AttendanceSessionDto {

    private UUID id;
    private UUID maidId;
    private LocalDate businessDate;
    private Instant entryAt;
    private Instant exitAt;
    private Long durationMinutes;
    private AttendanceStatus status;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;

    public AttendanceSessionDto() {
    }

    public static AttendanceSessionDto fromEntity(AttendanceSession session) {
        if (session == null) {
            return null;
        }
        AttendanceSessionDto dto = new AttendanceSessionDto();
        dto.setId(session.getId());
        dto.setMaidId(session.getMaid().getId());
        dto.setBusinessDate(session.getBusinessDate());
        dto.setEntryAt(session.getEntryAt());
        dto.setExitAt(session.getExitAt());
        if (session.getEntryAt() != null && session.getExitAt() != null) {
            dto.setDurationMinutes(Duration.between(session.getEntryAt(), session.getExitAt()).toMinutes());
        }
        dto.setStatus(session.getStatus());
        dto.setNote(session.getNote());
        dto.setCreatedAt(session.getCreatedAt());
        dto.setUpdatedAt(session.getUpdatedAt());
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

    public LocalDate getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate(LocalDate businessDate) {
        this.businessDate = businessDate;
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

    public Long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Long durationMinutes) {
        this.durationMinutes = durationMinutes;
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
