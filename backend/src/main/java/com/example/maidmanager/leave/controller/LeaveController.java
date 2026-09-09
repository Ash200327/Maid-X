package com.example.maidmanager.leave.controller;

import com.example.maidmanager.auth.security.OwnerPrincipal;
import com.example.maidmanager.leave.dto.CreateLeaveRequest;
import com.example.maidmanager.leave.dto.LeaveRecordDto;
import com.example.maidmanager.leave.dto.UpdateLeaveRequest;
import com.example.maidmanager.leave.service.LeaveService;
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
@RequestMapping("/api/v1/maids/{maidId}/leave")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    @GetMapping
    public ResponseEntity<List<LeaveRecordDto>> getLeaveRecords(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID maidId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<LeaveRecordDto> records = leaveService.getLeaveRecords(principal.getId(), maidId, from, to);
        return ResponseEntity.ok(records);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<LeaveRecordDto> createLeave(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID maidId,
            @Valid @RequestBody CreateLeaveRequest request) {
        LeaveRecordDto record = leaveService.createLeave(principal.getId(), maidId, request);
        return new ResponseEntity<>(record, HttpStatus.CREATED);
    }

    @PatchMapping("/{leaveId}")
    public ResponseEntity<LeaveRecordDto> updateLeave(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID maidId,
            @PathVariable UUID leaveId,
            @RequestBody UpdateLeaveRequest request) {
        LeaveRecordDto updated = leaveService.updateLeave(principal.getId(), maidId, leaveId, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{leaveId}")
    public ResponseEntity<Void> deleteLeave(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID maidId,
            @PathVariable UUID leaveId) {
        leaveService.deleteLeave(principal.getId(), maidId, leaveId);
        return ResponseEntity.noContent().build();
    }
}
