package com.example.maidmanager.attendance.controller;

import com.example.maidmanager.attendance.dto.DashboardResponse;
import com.example.maidmanager.attendance.service.AttendanceService;
import com.example.maidmanager.auth.security.OwnerPrincipal;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final AttendanceService attendanceService;

    public DashboardController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        DashboardResponse dashboard = attendanceService.getDashboard(principal.getId(), date);
        return ResponseEntity.ok(dashboard);
    }
}
