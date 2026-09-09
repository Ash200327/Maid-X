package com.example.maidmanager.leave.entity;

import com.example.maidmanager.common.entity.BaseAuditableEntity;
import com.example.maidmanager.common.enums.LeaveType;
import com.example.maidmanager.maid.entity.Maid;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
    name = "leave_records",
    uniqueConstraints = @UniqueConstraint(columnNames = {"maid_id", "leave_date"})
)
public class LeaveRecord extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maid_id", nullable = false)
    private Maid maid;

    @Column(name = "leave_date", nullable = false)
    private LocalDate leaveDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 10)
    private LeaveType leaveType;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    public LeaveRecord() {
    }

    public LeaveRecord(UUID id, Maid maid, LocalDate leaveDate, LeaveType leaveType, String note) {
        this.id = id != null ? id : UUID.randomUUID();
        this.maid = maid;
        this.leaveDate = leaveDate;
        this.leaveType = leaveType;
        this.note = note;
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
