package com.example.maidmanager.payroll.controller;

import com.example.maidmanager.auth.security.OwnerPrincipal;
import com.example.maidmanager.common.enums.PayrollStatus;
import com.example.maidmanager.payroll.dto.MarkPaidRequest;
import com.example.maidmanager.payroll.dto.PayrollAdjustmentDto;
import com.example.maidmanager.payroll.dto.PayrollAdjustmentRequest;
import com.example.maidmanager.payroll.dto.PayrollCalculateRequest;
import com.example.maidmanager.payroll.dto.PayrollRunDto;
import com.example.maidmanager.payroll.engine.PayrollCalculationResult;
import com.example.maidmanager.payroll.service.PayrollService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payroll/runs")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @PostMapping("/calculate")
    public ResponseEntity<PayrollCalculationResult> calculatePreview(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @Valid @RequestBody PayrollCalculateRequest request) {
        YearMonth ym = YearMonth.parse(request.getMonth());
        PayrollCalculationResult result = payrollService.calculatePreview(principal.getId(), request.getMaidId(), ym);
        return ResponseEntity.ok(result);
    }

    @GetMapping
    public ResponseEntity<List<PayrollRunDto>> getPayrollRuns(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @RequestParam(required = false) String month,
            @RequestParam(required = false) PayrollStatus status,
            @RequestParam(required = false) UUID maidId) {
        YearMonth ym = month != null && !month.isBlank() ? YearMonth.parse(month) : null;
        List<PayrollRunDto> runs = payrollService.getPayrollRuns(principal.getId(), ym, status, maidId);
        return ResponseEntity.ok(runs);
    }

    @PostMapping
    public ResponseEntity<PayrollRunDto> createOrGetDraftRun(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @Valid @RequestBody PayrollCalculateRequest request) {
        YearMonth ym = YearMonth.parse(request.getMonth());
        PayrollRunDto run = payrollService.getOrCreateDraftRun(principal.getId(), request.getMaidId(), ym);
        return ResponseEntity.status(HttpStatus.CREATED).body(run);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollRunDto> getPayrollRunById(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID id) {
        PayrollRunDto run = payrollService.getPayrollRunById(principal.getId(), id);
        return ResponseEntity.ok(run);
    }

    @PostMapping("/{id}/adjustments")
    public ResponseEntity<PayrollAdjustmentDto> addAdjustment(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody PayrollAdjustmentRequest request) {
        PayrollAdjustmentDto adjustment = payrollService.addAdjustment(principal.getId(), id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(adjustment);
    }

    @DeleteMapping("/{id}/adjustments/{adjustmentId}")
    public ResponseEntity<Void> deleteAdjustment(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID id,
            @PathVariable UUID adjustmentId) {
        payrollService.deleteAdjustment(principal.getId(), id, adjustmentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/finalize")
    public ResponseEntity<PayrollRunDto> finalizePayrollRun(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID id) {
        PayrollRunDto finalized = payrollService.finalizePayrollRun(principal.getId(), id);
        return ResponseEntity.ok(finalized);
    }

    @PostMapping("/{id}/mark-paid")
    public ResponseEntity<PayrollRunDto> markPaid(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @PathVariable UUID id,
            @Valid @RequestBody MarkPaidRequest request) {
        PayrollRunDto paid = payrollService.markPaid(principal.getId(), id, request);
        return ResponseEntity.ok(paid);
    }
}
