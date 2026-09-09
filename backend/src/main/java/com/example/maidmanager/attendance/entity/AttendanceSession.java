package com.example.maidmanager.attendance.entity;

import com.example.maidmanager.common.entity.BaseAuditableEntity;
import com.example.maidmanager.common.enums.AttendanceStatus;
import com.example.maidmanager.maid.entity.Maid;
import com.example.maidmanager.owner.entity.Owner;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "attendance_sessions")
public class AttendanceSession extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maid_id", nullable = false)
    private Maid maid;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Column(name = "entry_at")
    private Instant entryAt;

    @Column(name = "exit_at")
    private Instant exitAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttendanceStatus status;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private Owner createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private Owner updatedBy;

    public AttendanceSession() {
    }

    public AttendanceSession(UUID id, Maid maid, LocalDate businessDate, Instant entryAt, Instant exitAt,
                             AttendanceStatus status, String note, Owner createdBy, Owner updatedBy) {
        this.id = id != null ? id : UUID.randomUUID();
        this.maid = maid;
        this.businessDate = businessDate;
        this.entryAt = entryAt;
        this.exitAt = exitAt;
        this.status = status;
        this.note = note;
        this.createdBy = createdBy;
        this.updatedBy = updatedBy;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Maid getMaid() {
        return maid;
    }

    public void setMaid(Maid maid) {
        this.maid = maid;
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

    public Owner getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Owner createdBy) {
        this.createdBy = createdBy;
    }

    public Owner getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Owner updatedBy) {
        this.updatedBy = updatedBy;
    }
}
