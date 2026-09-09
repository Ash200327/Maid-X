package com.example.maidmanager.payroll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public class PayrollCalculateRequest {

    @NotNull(message = "Worker ID is required.")
    private UUID maidId;

    @NotBlank(message = "Payroll month is required.")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Payroll month must be in YYYY-MM format.")
    private String month;

    public PayrollCalculateRequest() {
    }

    public PayrollCalculateRequest(UUID maidId, String month) {
        this.maidId = maidId;
        this.month = month;
    }

    public UUID getMaidId() {
        return maidId;
    }

    public void setMaidId(UUID maidId) {
        this.maidId = maidId;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }
}
