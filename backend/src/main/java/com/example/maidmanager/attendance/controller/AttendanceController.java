package com.example.maidmanager.attendance.controller;

import com.example.maidmanager.attendance.dto.AttendanceSessionDto;
import com.example.maidmanager.attendance.dto.EntryRequest;
import com.example.maidmanager.attendance.dto.ExitRequest;
import com.example.maidmanager.attendance.dto.ManualStatusRequest;
import com.example.maidmanager.attendance.dto.UpdateAttendanceSessionRequest;
import com.example.maidmanager.attendance.service.AttendanceService;
import com.example.maidmanager.auth.security.OwnerPrincipal;
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
@RequestMapping("/api/v1/maids/{maidId}/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @GetMapping
    public ResponseEntity<List<AttendanceSessionDto>> getAttendance(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID maidId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<AttendanceSessionDto> sessions = attendanceService.getSessions(principal.getId(), maidId, from, to);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/sessions/entry")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AttendanceSessionDto> recordEntry(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID maidId,
            @RequestBody(required = false) EntryRequest request) {
        AttendanceSessionDto session = attendanceService.recordEntry(principal.getId(), maidId, request);
        return new ResponseEntity<>(session, HttpStatus.CREATED);
    }

    @PostMapping("/sessions/{sessionId}/exit")
    public ResponseEntity<AttendanceSessionDto> recordExit(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID maidId,
            @PathVariable UUID sessionId,
            @RequestBody(required = false) ExitRequest request) {
        AttendanceSessionDto session = attendanceService.recordExit(principal.getId(), maidId, sessionId, request);
        return ResponseEntity.ok(session);
    }

    @PatchMapping("/sessions/{sessionId}")
    public ResponseEntity<AttendanceSessionDto> updateSession(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID maidId,
            @PathVariable UUID sessionId,
            @Valid @RequestBody UpdateAttendanceSessionRequest request) {
        AttendanceSessionDto updated = attendanceService.updateSession(principal.getId(), maidId, sessionId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> deleteSession(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID maidId,
            @PathVariable UUID sessionId) {
        attendanceService.deleteSession(principal.getId(), maidId, sessionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/manual-status")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<AttendanceSessionDto> recordManualStatus(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID maidId,
            @Valid @RequestBody ManualStatusRequest request) {
        AttendanceSessionDto session = attendanceService.recordManualStatus(principal.getId(), maidId, request);
        return new ResponseEntity<>(session, HttpStatus.CREATED);
    }
}
