package com.example.maidmanager.payroll.controller;

import com.example.maidmanager.auth.security.OwnerPrincipal;
import com.example.maidmanager.payroll.dto.MonthlyReportResponse;
import com.example.maidmanager.payroll.service.PayrollService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final PayrollService payrollService;

    public ReportController(PayrollService payrollService) {
        this.payrollService = payrollService;
    }

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @RequestParam(required = false) String month) {
        YearMonth ym = month != null && !month.isBlank() ? YearMonth.parse(month) : YearMonth.now();
        MonthlyReportResponse report = payrollService.getMonthlyReport(principal.getId(), ym);
        return ResponseEntity.ok(report);
    }

    @GetMapping(value = "/monthly.csv", produces = "text/csv")
    public ResponseEntity<String> exportMonthlyReportCsv(
            @AuthenticationPrincipal OwnerPrincipal principal,
            @RequestParam(required = false) String month) {
        YearMonth ym = month != null && !month.isBlank() ? YearMonth.parse(month) : YearMonth.now();
        String csv = payrollService.exportMonthlyReportCsv(principal.getId(), ym);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"payroll-report-" + ym + ".csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }
}
