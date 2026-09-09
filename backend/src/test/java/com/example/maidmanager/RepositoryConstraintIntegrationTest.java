package com.example.maidmanager;

import com.example.maidmanager.attendance.entity.AttendanceSession;
import com.example.maidmanager.attendance.repository.AttendanceSessionRepository;
import com.example.maidmanager.audit.entity.AuditEvent;
import com.example.maidmanager.audit.repository.AuditEventRepository;
import com.example.maidmanager.auth.entity.AuthRefreshToken;
import com.example.maidmanager.auth.repository.AuthRefreshTokenRepository;
import com.example.maidmanager.common.enums.AttendanceStatus;
import com.example.maidmanager.common.enums.LeaveType;
import com.example.maidmanager.common.enums.PayrollAdjustmentType;
import com.example.maidmanager.common.enums.PayrollStatus;
import com.example.maidmanager.common.enums.SalaryMode;
import com.example.maidmanager.employment.entity.EmploymentConfig;
import com.example.maidmanager.employment.entity.MaidWorkingDay;
import com.example.maidmanager.employment.repository.EmploymentConfigRepository;
import com.example.maidmanager.holiday.entity.Holiday;
import com.example.maidmanager.holiday.repository.HolidayRepository;
import com.example.maidmanager.leave.entity.LeaveRecord;
import com.example.maidmanager.leave.repository.LeaveRecordRepository;
import com.example.maidmanager.maid.entity.Maid;
import com.example.maidmanager.maid.repository.MaidRepository;
import com.example.maidmanager.owner.entity.Owner;
import com.example.maidmanager.owner.repository.OwnerRepository;
import com.example.maidmanager.payroll.entity.PayrollAdjustment;
import com.example.maidmanager.payroll.entity.PayrollCalculationSnapshot;
import com.example.maidmanager.payroll.entity.PayrollRun;
import com.example.maidmanager.payroll.repository.PayrollAdjustmentRepository;
import com.example.maidmanager.payroll.repository.PayrollCalculationSnapshotRepository;
import com.example.maidmanager.payroll.repository.PayrollRunRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RepositoryConstraintIntegrationTest {

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private AuthRefreshTokenRepository authRefreshTokenRepository;

    @Autowired
    private MaidRepository maidRepository;

    @Autowired
    private EmploymentConfigRepository employmentConfigRepository;

    @Autowired
    private AttendanceSessionRepository attendanceSessionRepository;

    @Autowired
    private LeaveRecordRepository leaveRecordRepository;

    @Autowired
    private HolidayRepository holidayRepository;

    @Autowired
    private PayrollRunRepository payrollRunRepository;

    @Autowired
    private PayrollAdjustmentRepository payrollAdjustmentRepository;

    @Autowired
    private PayrollCalculationSnapshotRepository snapshotRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    private Owner ownerA;
    private Owner ownerB;
    private Maid maidA;

    @BeforeEach
    void setUp() {
        ownerA = new Owner(UUID.randomUUID(), "Owner A", "+919876543210", "ownerA@example.com", "hashed_pwd_A", "Asia/Kolkata", "INR");
        ownerA = ownerRepository.saveAndFlush(ownerA);

        ownerB = new Owner(UUID.randomUUID(), "Owner B", "+919876543211", "ownerB@example.com", "hashed_pwd_B", "Asia/Kolkata", "INR");
        ownerB = ownerRepository.saveAndFlush(ownerB);

        maidA = new Maid(UUID.randomUUID(), ownerA, "Rani", "+919876543212", LocalDate.of(2026, 1, 1), null, "Morning maid", true);
        maidA = maidRepository.saveAndFlush(maidA);
    }

    @Test
    @DisplayName("Verify auditable timestamps are automatically populated")
    void testAuditableTimestamps() {
        assertThat(ownerA.getCreatedAt()).isNotNull();
        assertThat(ownerA.getUpdatedAt()).isNotNull();
        assertThat(maidA.getCreatedAt()).isNotNull();
        assertThat(maidA.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Verify tenant isolation: Owner B cannot find Owner A's maid by ID")
    void testTenantIsolationOnMaid() {
        Optional<Maid> foundByOwnerA = maidRepository.findByIdAndOwnerId(maidA.getId(), ownerA.getId());
        Optional<Maid> foundByOwnerB = maidRepository.findByIdAndOwnerId(maidA.getId(), ownerB.getId());

        assertThat(foundByOwnerA).isPresent();
        assertThat(foundByOwnerB).isEmpty();
    }

    @Test
    @DisplayName("Verify employment config with working days")
    void testEmploymentConfigAndWorkingDays() {
        EmploymentConfig config = new EmploymentConfig(
                UUID.randomUUID(), maidA, LocalDate.of(2026, 1, 1), null,
                SalaryMode.MONTHLY, new BigDecimal("15000.00"),
                480, 450, false, new BigDecimal("1.000")
        );

        MaidWorkingDay day1 = new MaidWorkingDay(UUID.randomUUID(), config, (short) 1);
        MaidWorkingDay day2 = new MaidWorkingDay(UUID.randomUUID(), config, (short) 2);
        config.setWorkingDays(Set.of(day1, day2));

        EmploymentConfig saved = employmentConfigRepository.saveAndFlush(config);

        Optional<EmploymentConfig> effective = employmentConfigRepository.findEffectiveConfigAtDate(
                maidA.getId(), ownerA.getId(), LocalDate.of(2026, 5, 1)
        );

        assertThat(effective).isPresent();
        assertThat(effective.get().getSalaryAmount()).isEqualByComparingTo("15000.00");
        assertThat(effective.get().getWorkingDays()).hasSize(2);
    }

    @Test
    @DisplayName("Verify attendance sessions support multiple sessions per business day")
    void testMultipleAttendanceSessionsPerDay() {
        LocalDate today = LocalDate.of(2026, 9, 9);

        AttendanceSession session1 = new AttendanceSession(
                UUID.randomUUID(), maidA, today,
                Instant.parse("2026-09-09T03:30:00Z"), Instant.parse("2026-09-09T07:30:00Z"),
                AttendanceStatus.COMPLETED, "Morning shift", ownerA, ownerA
        );

        AttendanceSession session2 = new AttendanceSession(
                UUID.randomUUID(), maidA, today,
                Instant.parse("2026-09-09T10:30:00Z"), Instant.parse("2026-09-09T12:30:00Z"),
                AttendanceStatus.COMPLETED, "Evening shift", ownerA, ownerA
        );

        attendanceSessionRepository.saveAndFlush(session1);
        attendanceSessionRepository.saveAndFlush(session2);

        List<AttendanceSession> sessions = attendanceSessionRepository.findAllByMaidIdAndDate(maidA.getId(), ownerA.getId(), today);
        assertThat(sessions).hasSize(2);
    }

    @Test
    @DisplayName("Verify unique constraint on Leave: same maid cannot have duplicate leave on same date")
    void testLeaveUniqueConstraint() {
        LocalDate date = LocalDate.of(2026, 9, 15);
        LeaveRecord leave1 = new LeaveRecord(UUID.randomUUID(), maidA, date, LeaveType.PAID, "Doctor appointment");
        leaveRecordRepository.saveAndFlush(leave1);

        LeaveRecord leave2 = new LeaveRecord(UUID.randomUUID(), maidA, date, LeaveType.UNPAID, "Duplicate entry");
        assertThatThrownBy(() -> leaveRecordRepository.saveAndFlush(leave2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Verify unique constraint on Holiday: same owner cannot have duplicate holiday on same date")
    void testHolidayUniqueConstraint() {
        LocalDate date = LocalDate.of(2026, 10, 2);
        Holiday holiday1 = new Holiday(UUID.randomUUID(), ownerA, date, "Gandhi Jayanti");
        holidayRepository.saveAndFlush(holiday1);

        Holiday holiday2 = new Holiday(UUID.randomUUID(), ownerA, date, "Duplicate Holiday");
        assertThatThrownBy(() -> holidayRepository.saveAndFlush(holiday2))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Verify payroll run lifecycle, adjustments and calculation snapshot")
    void testPayrollRunAndSnapshot() {
        LocalDate month = LocalDate.of(2026, 9, 1);
        PayrollRun run = new PayrollRun(UUID.randomUUID(), ownerA, maidA, month, PayrollStatus.DRAFT);
        run = payrollRunRepository.saveAndFlush(run);

        PayrollAdjustment adjustment = new PayrollAdjustment(
                UUID.randomUUID(), run, PayrollAdjustmentType.ADVANCE,
                new BigDecimal("1000.00"), LocalDate.of(2026, 9, 5), "Festival advance"
        );
        payrollAdjustmentRepository.saveAndFlush(adjustment);

        PayrollCalculationSnapshot snapshot = new PayrollCalculationSnapshot(
                UUID.randomUUID(), run, "{\"expectedDays\":26}", "{\"finalPayable\":14000.00}", "v1.0"
        );
        snapshotRepository.saveAndFlush(snapshot);

        Optional<PayrollRun> retrieved = payrollRunRepository.findByMaidIdAndMonth(maidA.getId(), ownerA.getId(), month);
        assertThat(retrieved).isPresent();

        List<PayrollAdjustment> adjustments = payrollAdjustmentRepository.findAllByPayrollRunIdAndOwnerId(run.getId(), ownerA.getId());
        assertThat(adjustments).hasSize(1);
        assertThat(adjustments.get(0).getAmount()).isEqualByComparingTo("1000.00");

        Optional<PayrollCalculationSnapshot> foundSnapshot = snapshotRepository.findByPayrollRunIdAndOwnerId(run.getId(), ownerA.getId());
        assertThat(foundSnapshot).isPresent();
        assertThat(foundSnapshot.get().getEngineVersion()).isEqualTo("v1.0");
    }

    @Test
    @DisplayName("Verify audit events persist correctly with JSON details")
    void testAuditEventPersistence() {
        AuditEvent event = new AuditEvent(
                UUID.randomUUID(), ownerA, ownerA, "MAID", maidA.getId(),
                "CREATE", null, "{\"name\":\"Rani\"}"
        );
        auditEventRepository.saveAndFlush(event);

        List<AuditEvent> events = auditEventRepository.findAllByEntity(ownerA.getId(), "MAID", maidA.getId());
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getAction()).isEqualTo("CREATE");
        assertThat(events.get(0).getAfterData()).isEqualTo("{\"name\":\"Rani\"}");
    }
}
