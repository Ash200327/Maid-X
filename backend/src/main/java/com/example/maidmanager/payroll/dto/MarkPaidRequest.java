package com.example.maidmanager.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public class MarkPaidRequest {

    private Instant paidAt;

    @NotBlank(message = "Payment method is required (e.g. UPI, CASH, BANK_TRANSFER).")
    @Size(max = 30, message = "Payment method must not exceed 30 characters.")
    private String paymentMethod;

    @Size(max = 500, message = "Payment note must not exceed 500 characters.")
    private String paymentNote;

    public MarkPaidRequest() {
    }

    public MarkPaidRequest(Instant paidAt, String paymentMethod, String paymentNote) {
        this.paidAt = paidAt;
        this.paymentMethod = paymentMethod;
        this.paymentNote = paymentNote;
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
}
