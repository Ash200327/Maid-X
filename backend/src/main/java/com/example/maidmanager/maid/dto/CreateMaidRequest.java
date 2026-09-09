package com.example.maidmanager.maid.dto;

import com.example.maidmanager.employment.dto.CreateEmploymentConfigRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class CreateMaidRequest {

    @NotBlank(message = "Worker name is required")
    @Size(max = 120, message = "Worker name must not exceed 120 characters")
    private String name;

    @Size(max = 30, message = "Phone must not exceed 30 characters")
    private String phone;

    @NotNull(message = "Joining date is required")
    private LocalDate joiningDate;

    private LocalDate leavingDate;

    private String notes;

    @NotNull(message = "Employment configuration is required")
    @Valid
    private CreateEmploymentConfigRequest employmentConfig;

    public CreateMaidRequest() {
    }

    public CreateMaidRequest(String name, String phone, LocalDate joiningDate, LocalDate leavingDate,
                             String notes, CreateEmploymentConfigRequest employmentConfig) {
        this.name = name;
        this.phone = phone;
        this.joiningDate = joiningDate;
        this.leavingDate = leavingDate;
        this.notes = notes;
        this.employmentConfig = employmentConfig;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(LocalDate joiningDate) {
        this.joiningDate = joiningDate;
    }

    public LocalDate getLeavingDate() {
        return leavingDate;
    }

    public void setLeavingDate(LocalDate leavingDate) {
        this.leavingDate = leavingDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public CreateEmploymentConfigRequest getEmploymentConfig() {
        return employmentConfig;
    }

    public void setEmploymentConfig(CreateEmploymentConfigRequest employmentConfig) {
        this.employmentConfig = employmentConfig;
    }
}
