package com.example.maidmanager.payroll.service;

import com.example.maidmanager.attendance.entity.AttendanceSession;
import com.example.maidmanager.attendance.repository.AttendanceSessionRepository;
import com.example.maidmanager.audit.entity.AuditEvent;
import com.example.maidmanager.audit.repository.AuditEventRepository;
import com.example.maidmanager.common.enums.PayrollStatus;
import com.example.maidmanager.common.exception.BadRequestException;
import com.example.maidmanager.common.exception.ResourceNotFoundException;
import com.example.maidmanager.employment.entity.EmploymentConfig;
import com.example.maidmanager.employment.repository.EmploymentConfigRepository;
import com.example.maidmanager.holiday.entity.Holiday;
import com.example.maidmanager.holiday.repository.HolidayRepository;
import com.example.maidmanager.leave.entity.LeaveRecord;
import com.example.maidmanager.leave.repository.LeaveRecordRepository;
import com.example.maidmanager.maid.entity.Maid;
import com.example.maidmanager.maid.repository.MaidRepository;
import com.example.maidmanager.owner.entity.Owner;
import com.example.maidmanager.owner.repository.OwnerRepository;
import com.example.maidmanager.payroll.dto.MarkPaidRequest;
import com.example.maidmanager.payroll.dto.MonthlyReportResponse;
import com.example.maidmanager.payroll.dto.PayrollAdjustmentDto;
import com.example.maidmanager.payroll.dto.PayrollAdjustmentRequest;
import com.example.maidmanager.payroll.dto.PayrollRunDto;
import com.example.maidmanager.payroll.engine.PayrollCalculationContext;
import com.example.maidmanager.payroll.engine.PayrollCalculationResult;
import com.example.maidmanager.payroll.engine.SalaryEngine;
import com.example.maidmanager.payroll.entity.PayrollAdjustment;
import com.example.maidmanager.payroll.entity.PayrollCalculationSnapshot;
import com.example.maidmanager.payroll.entity.PayrollRun;
import com.example.maidmanager.payroll.repository.PayrollAdjustmentRepository;
import com.example.maidmanager.payroll.repository.PayrollCalculationSnapshotRepository;
import com.example.maidmanager.payroll.repository.PayrollRunRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PayrollService {

    private static final String ENGINE_VERSION = "v1.0";

    private final PayrollRunRepository payrollRunRepository;
    private final PayrollAdjustmentRepository payrollAdjustmentRepository;
    private final PayrollCalculationSnapshotRepository snapshotRepository;
    private final MaidRepository maidRepository;
    private final OwnerRepository ownerRepository;
    private final EmploymentConfigRepository configRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final LeaveRecordRepository leaveRepository;
    private final HolidayRepository holidayRepository;
    private final AuditEventRepository auditEventRepository;
    private final SalaryEngine salaryEngine;
    private final ObjectMapper objectMapper;

    public PayrollService(PayrollRunRepository payrollRunRepository,
                          PayrollAdjustmentRepository payrollAdjustmentRepository,
                          PayrollCalculationSnapshotRepository snapshotRepository,
                          MaidRepository maidRepository,
                          OwnerRepository ownerRepository,
                          EmploymentConfigRepository configRepository,
                          AttendanceSessionRepository sessionRepository,
                          LeaveRecordRepository leaveRepository,
                          HolidayRepository holidayRepository,
                          AuditEventRepository auditEventRepository,
                          SalaryEngine salaryEngine,
                          ObjectMapper objectMapper) {
        this.payrollRunRepository = payrollRunRepository;
        this.payrollAdjustmentRepository = payrollAdjustmentRepository;
        this.snapshotRepository = snapshotRepository;
        this.maidRepository = maidRepository;
        this.ownerRepository = ownerRepository;
        this.configRepository = configRepository;
        this.sessionRepository = sessionRepository;
        this.leaveRepository = leaveRepository;
        this.holidayRepository = holidayRepository;
        this.auditEventRepository = auditEventRepository;
        this.salaryEngine = salaryEngine;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public PayrollCalculationResult calculatePreview(UUID ownerId, UUID maidId, YearMonth month) {
        Owner owner = getOwnerOrThrow(ownerId);
        Maid maid = getMaidOrThrow(maidId, ownerId);

        // Check if there is an existing draft run to load its adjustments
        LocalDate monthDate = month.atDay(1);
        Optional<PayrollRun> runOpt = payrollRunRepository.findByMaidIdAndMonth(maidId, ownerId, monthDate);
        List<PayrollAdjustment> adjustments = runOpt.isPresent()
                ? payrollAdjustmentRepository.findAllByPayrollRunIdAndOwnerId(runOpt.get().getId(), ownerId)
                : List.of();

        return computeCalculation(owner, maid, month, adjustments);
    }

    @Transactional
    public PayrollRunDto getOrCreateDraftRun(UUID ownerId, UUID maidId, YearMonth month) {
        Owner owner = getOwnerOrThrow(ownerId);
        Maid maid = getMaidOrThrow(maidId, ownerId);
        LocalDate monthDate = month.atDay(1);

        PayrollRun run = payrollRunRepository.findByMaidIdAndMonth(maidId, ownerId, monthDate)
                .orElseGet(() -> {
                    PayrollRun newRun = new PayrollRun(UUID.randomUUID(), owner, maid, monthDate, PayrollStatus.DRAFT);
                    PayrollRun saved = payrollRunRepository.save(newRun);
                    recordAudit(owner, "PAYROLL_RUN", saved.getId(), "CREATE", null, saved);
                    return saved;
                });

        PayrollCalculationResult calculation = resolveCalculationForResult(owner, run);
        return buildRunDto(run, calculation, ownerId);
    }

    @Transactional(readOnly = true)
    public List<PayrollRunDto> getPayrollRuns(UUID ownerId, YearMonth month, PayrollStatus status, UUID maidId) {
        Owner owner = getOwnerOrThrow(ownerId);
        List<PayrollRun> runs;

        if (maidId != null && month != null) {
            runs = payrollRunRepository.findAllByOwnerIdAndMaidIdAndMonth(ownerId, maidId, month.atDay(1));
        } else if (maidId != null) {
            runs = payrollRunRepository.findAllByOwnerIdAndMaidId(ownerId, maidId);
        } else if (month != null && status != null) {
            runs = payrollRunRepository.findAllByOwnerIdAndMonthAndStatus(ownerId, month.atDay(1), status);
        } else if (month != null) {
            runs = payrollRunRepository.findAllByOwnerIdAndMonth(ownerId, month.atDay(1));
        } else {
            runs = payrollRunRepository.findAllByOwnerId(ownerId);
        }

        return runs.stream()
                .map(run -> {
                    PayrollCalculationResult calculation = resolveCalculationForResult(owner, run);
                    return buildRunDto(run, calculation, ownerId);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PayrollRunDto getPayrollRunById(UUID ownerId, UUID runId) {
        Owner owner = getOwnerOrThrow(ownerId);
        PayrollRun run = payrollRunRepository.findByIdAndOwnerId(runId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found."));

        PayrollCalculationResult calculation = resolveCalculationForResult(owner, run);
        return buildRunDto(run, calculation, ownerId);
    }

    @Transactional
    public PayrollAdjustmentDto addAdjustment(UUID ownerId, UUID runId, PayrollAdjustmentRequest request) {
        Owner owner = getOwnerOrThrow(ownerId);
        PayrollRun run = payrollRunRepository.findByIdAndOwnerId(runId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found."));

        if (run.getStatus() != PayrollStatus.DRAFT) {
            throw new BadRequestException("Cannot modify adjustments on a finalized or paid payroll run.");
        }

        PayrollAdjustment adjustment = new PayrollAdjustment(
                UUID.randomUUID(),
                run,
                request.getType(),
                request.getAmount(),
                request.getAdjustmentDate(),
                request.getReason()
        );

        PayrollAdjustment saved = payrollAdjustmentRepository.save(adjustment);
        recordAudit(owner, "PAYROLL_ADJUSTMENT", saved.getId(), "CREATE", null, saved);

        return PayrollAdjustmentDto.fromEntity(saved);
    }

    @Transactional
    public void deleteAdjustment(UUID ownerId, UUID runId, UUID adjustmentId) {
        Owner owner = getOwnerOrThrow(ownerId);
        PayrollRun run = payrollRunRepository.findByIdAndOwnerId(runId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found."));

        if (run.getStatus() != PayrollStatus.DRAFT) {
            throw new BadRequestException("Cannot delete adjustments from a finalized or paid payroll run.");
        }

        PayrollAdjustment adjustment = payrollAdjustmentRepository.findByIdAndOwnerId(adjustmentId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll adjustment not found."));

        if (!adjustment.getPayrollRun().getId().equals(runId)) {
            throw new BadRequestException("Adjustment does not belong to the specified payroll run.");
        }

        String beforeJson = serializeQuietly(PayrollAdjustmentDto.fromEntity(adjustment));
        payrollAdjustmentRepository.delete(adjustment);
        recordAudit(owner, "PAYROLL_ADJUSTMENT", adjustmentId, "DELETE", beforeJson, null);
    }

    @Transactional
    public PayrollRunDto finalizePayrollRun(UUID ownerId, UUID runId) {
        Owner owner = getOwnerOrThrow(ownerId);
        PayrollRun run = payrollRunRepository.findByIdAndOwnerId(runId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found."));

        if (run.getStatus() != PayrollStatus.DRAFT) {
            throw new BadRequestException("Only DRAFT payroll runs can be finalized.");
        }

        YearMonth month = YearMonth.from(run.getPayrollMonth());
        List<PayrollAdjustment> adjustments = payrollAdjustmentRepository.findAllByPayrollRunIdAndOwnerId(run.getId(), ownerId);
        PayrollCalculationResult result = computeCalculation(owner, run.getMaid(), month, adjustments);

        // Snapshot inputs and result
        Map<String, Object> inputSnapshotMap = new HashMap<>();
        inputSnapshotMap.put("maidId", run.getMaid().getId());
        inputSnapshotMap.put("maidName", run.getMaid().getName());
        inputSnapshotMap.put("month", run.getPayrollMonth().toString());
        inputSnapshotMap.put("adjustments", adjustments.stream().map(PayrollAdjustmentDto::fromEntity).toList());

        String inputJson = serializeQuietly(inputSnapshotMap);
        String resultJson = serializeQuietly(result);

        PayrollCalculationSnapshot snapshot = new PayrollCalculationSnapshot(
                UUID.randomUUID(),
                run,
                inputJson,
                resultJson,
                ENGINE_VERSION
        );

        run.setSnapshot(snapshot);
        run.setStatus(PayrollStatus.FINALIZED);
        run.setFinalizedAt(Instant.now());

        snapshotRepository.save(snapshot);
        PayrollRun saved = payrollRunRepository.save(run);
        recordAudit(owner, "PAYROLL_RUN", saved.getId(), "FINALIZE", null, saved);

        return PayrollRunDto.fromEntity(saved, result);
    }

    @Transactional
    public PayrollRunDto markPaid(UUID ownerId, UUID runId, MarkPaidRequest request) {
        Owner owner = getOwnerOrThrow(ownerId);
        PayrollRun run = payrollRunRepository.findByIdAndOwnerId(runId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Payroll run not found."));

        if (run.getStatus() == PayrollStatus.DRAFT) {
            throw new BadRequestException("Payroll run must be FINALIZED before marking as PAID.");
        }
        if (run.getStatus() == PayrollStatus.PAID) {
            throw new BadRequestException("Payroll run is already marked as PAID.");
        }

        run.setStatus(PayrollStatus.PAID);
        run.setPaidAt(request.getPaidAt() != null ? request.getPaidAt() : Instant.now());
        run.setPaymentMethod(request.getPaymentMethod());
        run.setPaymentNote(request.getPaymentNote());

        PayrollRun saved = payrollRunRepository.save(run);
        recordAudit(owner, "PAYROLL_RUN", saved.getId(), "MARK_PAID", null, saved);

        PayrollCalculationResult calculation = resolveCalculationForResult(owner, saved);
        return PayrollRunDto.fromEntity(saved, calculation);
    }

    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(UUID ownerId, YearMonth month) {
        Owner owner = getOwnerOrThrow(ownerId);
        LocalDate monthDate = month.atDay(1);

        List<Maid> activeMaids = maidRepository.findAllByOwnerIdAndIsActiveOrderByCreatedAtDesc(ownerId, true);
        List<PayrollRunDto> runs = new ArrayList<>();

        int draftCount = 0;
        int finalizedCount = 0;
        int paidCount = 0;
        BigDecimal totalPayable = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;

        for (Maid maid : activeMaids) {
            Optional<PayrollRun> runOpt = payrollRunRepository.findByMaidIdAndMonth(maid.getId(), ownerId, monthDate);
            PayrollRunDto runDto;
            if (runOpt.isPresent()) {
                PayrollRun run = runOpt.get();
                PayrollCalculationResult calculation = resolveCalculationForResult(owner, run);
                runDto = PayrollRunDto.fromEntity(run, calculation);
            } else {
                // Not yet drafted - compute preview for reporting
                PayrollCalculationResult preview = computeCalculation(owner, maid, month, List.of());
                runDto = new PayrollRunDto();
                runDto.setMaidId(maid.getId());
                runDto.setMaidName(maid.getName());
                runDto.setPayrollMonth(month.toString());
                runDto.setStatus(PayrollStatus.DRAFT);
                runDto.setCalculation(preview);
                runDto.setBaseEarnings(preview.getBaseEarnings());
                runDto.setShortfallDeduction(preview.getShortfallDeduction());
                runDto.setOvertimePay(preview.getOvertimePay());
                runDto.setAdditions(preview.getAdditions());
                runDto.setDeductions(preview.getDeductions());
                runDto.setFinalPayable(preview.getFinalPayable());
                runDto.setCurrency(preview.getCurrency());
            }

            runs.add(runDto);

            if (runDto.getStatus() == PayrollStatus.PAID) {
                paidCount++;
                totalPaid = totalPaid.add(runDto.getFinalPayable() != null ? runDto.getFinalPayable() : BigDecimal.ZERO);
                totalPayable = totalPayable.add(runDto.getFinalPayable() != null ? runDto.getFinalPayable() : BigDecimal.ZERO);
            } else if (runDto.getStatus() == PayrollStatus.FINALIZED) {
                finalizedCount++;
                totalPayable = totalPayable.add(runDto.getFinalPayable() != null ? runDto.getFinalPayable() : BigDecimal.ZERO);
            } else {
                draftCount++;
                totalPayable = totalPayable.add(runDto.getFinalPayable() != null ? runDto.getFinalPayable() : BigDecimal.ZERO);
            }
        }

        return new MonthlyReportResponse(
                month.toString(),
                activeMaids.size(),
                draftCount,
                finalizedCount,
                paidCount,
                totalPayable.setScale(2, RoundingMode.HALF_UP),
                totalPaid.setScale(2, RoundingMode.HALF_UP),
                owner.getCurrencyCode(),
                runs
        );
    }

    @Transactional(readOnly = true)
    public String exportMonthlyReportCsv(UUID ownerId, YearMonth month) {
        MonthlyReportResponse report = getMonthlyReport(ownerId, month);
        StringWriter writer = new StringWriter();

        // CSV Header
        writer.write("Maid Name,Month,Status,Configured Salary,Expected Days,Worked Days,Base Earnings,Shortfall Deduction,Overtime Pay,Additions,Deductions,Final Payable,Currency,Payment Method,Paid At\n");

        for (PayrollRunDto run : report.getRuns()) {
            PayrollCalculationResult calc = run.getCalculation();
            String configuredSalary = calc != null && calc.getConfiguredSalary() != null ? calc.getConfiguredSalary().toString() : "0.00";
            int expectedDays = calc != null ? calc.getEligibleWorkingDays() : 0;
            int workedDays = calc != null ? calc.getWorkedDays() : 0;
            String base = run.getBaseEarnings() != null ? run.getBaseEarnings().toString() : "0.00";
            String shortfall = run.getShortfallDeduction() != null ? run.getShortfallDeduction().toString() : "0.00";
            String overtime = run.getOvertimePay() != null ? run.getOvertimePay().toString() : "0.00";
            String additions = run.getAdditions() != null ? run.getAdditions().toString() : "0.00";
            String deductions = run.getDeductions() != null ? run.getDeductions().toString() : "0.00";
            String finalPay = run.getFinalPayable() != null ? run.getFinalPayable().toString() : "0.00";
            String paymentMethod = run.getPaymentMethod() != null ? run.getPaymentMethod() : "";
            String paidAt = run.getPaidAt() != null ? run.getPaidAt().toString() : "";

            writer.write(String.format("\"%s\",\"%s\",\"%s\",%s,%d,%d,%s,%s,%s,%s,%s,%s,\"%s\",\"%s\",\"%s\"\n",
                    escapeCsv(run.getMaidName()),
                    run.getPayrollMonth(),
                    run.getStatus().name(),
                    configuredSalary,
                    expectedDays,
                    workedDays,
                    base,
                    shortfall,
                    overtime,
                    additions,
                    deductions,
                    finalPay,
                    run.getCurrency(),
                    escapeCsv(paymentMethod),
                    paidAt
            ));
        }

        return writer.toString();
    }

    private String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace("\"", "\"\"");
    }

    private PayrollRunDto buildRunDto(PayrollRun run, PayrollCalculationResult calculation, UUID ownerId) {
        List<PayrollAdjustment> adjustments = payrollAdjustmentRepository.findAllByPayrollRunIdAndOwnerId(run.getId(), ownerId);
        PayrollRunDto dto = PayrollRunDto.fromEntity(run, calculation);
        dto.setAdjustments(adjustments.stream().map(PayrollAdjustmentDto::fromEntity).collect(Collectors.toList()));
        return dto;
    }

    private PayrollCalculationResult resolveCalculationForResult(Owner owner, PayrollRun run) {
        if (run.getStatus() != PayrollStatus.DRAFT && run.getSnapshot() != null) {
            try {
                return objectMapper.readValue(run.getSnapshot().getResultSnapshot(), PayrollCalculationResult.class);
            } catch (Exception ignored) {
            }
        }
        YearMonth month = YearMonth.from(run.getPayrollMonth());
        List<PayrollAdjustment> adjustments = payrollAdjustmentRepository.findAllByPayrollRunIdAndOwnerId(run.getId(), owner.getId());
        return computeCalculation(owner, run.getMaid(), month, adjustments);
    }

    private PayrollCalculationResult computeCalculation(Owner owner, Maid maid, YearMonth month, List<PayrollAdjustment> adjustments) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();

        List<EmploymentConfig> configs = configRepository.findConfigsForMonth(maid.getId(), owner.getId(), startDate, endDate);
        if (configs.isEmpty()) {
            throw new BadRequestException("No employment configuration found for worker in month " + month);
        }

        List<AttendanceSession> sessions = sessionRepository.findAllByMaidIdAndDateRange(
                maid.getId(), owner.getId(), startDate, endDate
        );

        List<LeaveRecord> leaveRecords = leaveRepository.findAllByMaidIdAndDateRange(
                maid.getId(), owner.getId(), startDate, endDate
        );

        List<Holiday> holidays = holidayRepository.findAllByOwnerIdAndDateRange(
                owner.getId(), startDate, endDate
        );

        PayrollCalculationContext context = new PayrollCalculationContext(
                maid.getId(),
                owner.getId(),
                month,
                maid.getJoiningDate(),
                maid.getLeavingDate(),
                configs,
                sessions,
                leaveRecords,
                holidays,
                adjustments != null ? adjustments : List.of(),
                owner.getCurrencyCode()
        );

        return salaryEngine.calculate(context);
    }

    private Owner getOwnerOrThrow(UUID ownerId) {
        return ownerRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found."));
    }

    private Maid getMaidOrThrow(UUID maidId, UUID ownerId) {
        return maidRepository.findByIdAndOwnerId(maidId, ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Worker not found or does not belong to owner."));
    }

    private void recordAudit(Owner owner, String entityType, UUID entityId, String action, String beforeState, Object afterEntity) {
        String afterJson = null;
        if (afterEntity instanceof PayrollAdjustment adj) {
            afterJson = serializeQuietly(PayrollAdjustmentDto.fromEntity(adj));
        } else if (afterEntity instanceof PayrollRun run) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", run.getId());
            map.put("maidId", run.getMaid() != null ? run.getMaid().getId() : null);
            map.put("status", run.getStatus());
            map.put("payrollMonth", run.getPayrollMonth() != null ? run.getPayrollMonth().toString() : null);
            map.put("finalizedAt", run.getFinalizedAt());
            map.put("paidAt", run.getPaidAt());
            map.put("paymentMethod", run.getPaymentMethod());
            afterJson = serializeQuietly(map);
        } else if (afterEntity != null) {
            afterJson = serializeQuietly(afterEntity);
        }

        AuditEvent event = new AuditEvent(
                UUID.randomUUID(),
                owner,
                owner,
                entityType,
                entityId,
                action,
                beforeState,
                afterJson
        );
        auditEventRepository.save(event);
    }

    private String serializeQuietly(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return "{}";
        }
    }
}
