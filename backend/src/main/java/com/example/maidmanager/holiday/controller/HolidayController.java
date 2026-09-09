package com.example.maidmanager.holiday.controller;

import com.example.maidmanager.auth.security.OwnerPrincipal;
import com.example.maidmanager.holiday.dto.CreateHolidayRequest;
import com.example.maidmanager.holiday.dto.HolidayDto;
import com.example.maidmanager.holiday.dto.UpdateHolidayRequest;
import com.example.maidmanager.holiday.service.HolidayService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/holidays")
public class HolidayController {

    private final HolidayService holidayService;

    public HolidayController(HolidayService holidayService) {
        this.holidayService = holidayService;
    }

    @GetMapping
    public ResponseEntity<List<HolidayDto>> listHolidays(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<HolidayDto> holidays = holidayService.listHolidays(principal.getId(), year, month, from, to);
        return ResponseEntity.ok(holidays);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<HolidayDto> createHoliday(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @Valid @RequestBody CreateHolidayRequest request) {
        HolidayDto holiday = holidayService.createHoliday(principal.getId(), request);
        return new ResponseEntity<>(holiday, HttpStatus.CREATED);
    }

    @PatchMapping("/{holidayId}")
    public ResponseEntity<HolidayDto> updateHoliday(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID holidayId,
            @Valid @RequestBody UpdateHolidayRequest request) {
        HolidayDto updated = holidayService.updateHoliday(principal.getId(), holidayId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{holidayId}")
    public ResponseEntity<Void> deleteHoliday(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID holidayId) {
        holidayService.deleteHoliday(principal.getId(), holidayId);
        return ResponseEntity.noContent().build();
    }
}
