package com.example.maidmanager.holiday.dto;

import com.example.maidmanager.holiday.entity.Holiday;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class HolidayDto {

    private UUID id;
    private LocalDate holidayDate;
    private String name;
    private Instant createdAt;
    private Instant updatedAt;

    public HolidayDto() {
    }

    public static HolidayDto fromEntity(Holiday holiday) {
        if (holiday == null) {
            return null;
        }
        HolidayDto dto = new HolidayDto();
        dto.setId(holiday.getId());
        dto.setHolidayDate(holiday.getHolidayDate());
        dto.setName(holiday.getName());
        dto.setCreatedAt(holiday.getCreatedAt());
        dto.setUpdatedAt(holiday.getUpdatedAt());
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
