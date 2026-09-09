package com.example.maidmanager.payroll.entity;

import com.example.maidmanager.common.entity.BaseAuditableEntity;
import com.example.maidmanager.common.enums.PayrollStatus;
import com.example.maidmanager.maid.entity.Maid;
import com.example.maidmanager.owner.entity.Owner;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
    name = "payroll_runs",
    uniqueConstraints = @UniqueConstraint(columnNames = {"maid_id", "payroll_month"})
)
public class PayrollRun extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private Owner owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maid_id", nullable = false)
    private Maid maid;

    @Column(name = "payroll_month", nullable = false)
    private LocalDate payrollMonth;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PayrollStatus status;

    @Column(name = "finalized_at")
    private Instant finalizedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "payment_note", columnDefinition = "TEXT")
    private String paymentNote;

    @OneToMany(mappedBy = "payrollRun", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<PayrollAdjustment> adjustments = new ArrayList<>();

    @OneToOne(mappedBy = "payrollRun", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PayrollCalculationSnapshot snapshot;

    public PayrollRun() {
    }

    public PayrollRun(UUID id, Owner owner, Maid maid, LocalDate payrollMonth, PayrollStatus status) {
        this.id = id != null ? id : UUID.randomUUID();
        this.owner = owner;
        this.maid = maid;
        this.payrollMonth = payrollMonth;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public Maid getMaid() {
        return maid;
    }

    public void setMaid(Maid maid) {
        this.maid = maid;
    }

    public LocalDate getPayrollMonth() {
        return payrollMonth;
    }

    public void setPayrollMonth(LocalDate payrollMonth) {
        this.payrollMonth = payrollMonth;
    }

    public PayrollStatus getStatus() {
        return status;
    }

    public void setStatus(PayrollStatus status) {
        this.status = status;
    }

    public Instant getFinalizedAt() {
        return finalizedAt;
    }

    public void setFinalizedAt(Instant finalizedAt) {
        this.finalizedAt = finalizedAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentNote() {
        return paymentNote;
    }

    public void setPaymentNote(String paymentNote) {
        this.paymentNote = paymentNote;
    }

    public List<PayrollAdjustment> getAdjustments() {
        return adjustments;
    }

    public void setAdjustments(List<PayrollAdjustment> adjustments) {
        this.adjustments = adjustments;
    }

    public PayrollCalculationSnapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(PayrollCalculationSnapshot snapshot) {
        this.snapshot = snapshot;
    }
}
