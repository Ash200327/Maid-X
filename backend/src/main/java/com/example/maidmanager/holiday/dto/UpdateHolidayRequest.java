package com.example.maidmanager.holiday.dto;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class UpdateHolidayRequest {

    private LocalDate holidayDate;

    @Size(max = 120, message = "Holiday name must not exceed 120 characters")
    private String name;

    public UpdateHolidayRequest() {
    }

    public UpdateHolidayRequest(LocalDate holidayDate, String name) {
        this.holidayDate = holidayDate;
        this.name = name;
    }

    public LocalDate getHolidayDate() {
        return holidayDate;
    }

    public void setHolidayDate(LocalDate holidayDate) {
        this.holidayDate = holidayDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
