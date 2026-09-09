package com.example.maidmanager;

import com.example.maidmanager.attendance.entity.AttendanceSession;
import com.example.maidmanager.common.enums.AttendanceStatus;
import com.example.maidmanager.common.enums.LeaveType;
import com.example.maidmanager.common.enums.PayrollAdjustmentType;
import com.example.maidmanager.common.enums.SalaryMode;
import com.example.maidmanager.employment.entity.EmploymentConfig;
import com.example.maidmanager.employment.entity.MaidWorkingDay;
import com.example.maidmanager.holiday.entity.Holiday;
import com.example.maidmanager.leave.entity.LeaveRecord;
import com.example.maidmanager.maid.entity.Maid;
import com.example.maidmanager.owner.entity.Owner;
import com.example.maidmanager.payroll.engine.DailyCalculationResult;
import com.example.maidmanager.payroll.engine.DailySalaryCalculator;
import com.example.maidmanager.payroll.engine.HourlySalaryCalculator;
import com.example.maidmanager.payroll.engine.MonthlySalaryCalculator;
import com.example.maidmanager.payroll.engine.PayrollCalculationContext;
import com.example.maidmanager.payroll.engine.PayrollCalculationResult;
import com.example.maidmanager.payroll.engine.SalaryEngine;
import com.example.maidmanager.payroll.entity.PayrollAdjustment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class SalaryEngineTest {

    private SalaryEngine salaryEngine;
    private Owner owner;
    private Maid maid;
    private final ZoneId zoneId = ZoneId.of("Asia/Kolkata");

    @BeforeEach
    void setUp() {
        salaryEngine = new SalaryEngine(List.of(
                new MonthlySalaryCalculator(),
                new DailySalaryCalculator(),
                new HourlySalaryCalculator()
        ));

        owner = new Owner(UUID.randomUUID(), "Test Owner", "9876543210", "testowner@example.com", "hash", "Asia/Kolkata", "INR");
        maid = new Maid(UUID.randomUUID(), owner, "Kamla Bai", "9876501234", LocalDate.of(2026, 1, 1), null, null, true);
    }

    private EmploymentConfig createConfig(SalaryMode mode, BigDecimal salary, int expectedMinutes,
                                          int thresholdMinutes, boolean overtimeEnabled, BigDecimal overtimeMultiplier) {
        EmploymentConfig config = new EmploymentConfig();
        config.setId(UUID.randomUUID());
        config.setMaid(maid);
        config.setSalaryMode(mode);
        config.setSalaryAmount(salary);
        config.setExpectedMinutesPerDay(expectedMinutes);
        config.setShortfallThresholdMinutes(thresholdMinutes);
        config.setOvertimeEnabled(overtimeEnabled);
        config.setOvertimeMultiplier(overtimeMultiplier != null ? overtimeMultiplier : new BigDecimal("1.000"));
        config.setEffectiveFrom(LocalDate.of(2026, 1, 1));
        config.setEffectiveTo(null);

        // Monday (1) to Saturday (6)
        Set<MaidWorkingDay> workingDays = new HashSet<>();
        for (short d = 1; d <= 6; d++) {
            workingDays.add(new MaidWorkingDay(UUID.randomUUID(), config, d));
        }
        config.setWorkingDays(workingDays);
        return config;
    }

    private AttendanceSession createSession(LocalDate date, LocalTime start, LocalTime end) {
        Instant entryAt = date.atTime(start).atZone(zoneId).toInstant();
        Instant exitAt = date.atTime(end).atZone(zoneId).toInstant();
        return new AttendanceSession(UUID.randomUUID(), maid, date, entryAt, exitAt,
                AttendanceStatus.COMPLETED, "Regular shift", owner, owner);
    }

    @Test
    @DisplayName("1. Full monthly attendance: 100% pay, zero shortfall, zero overtime")
    void testFullMonthlyAttendance_100PercentPay() {
        // September 2026: 30 days. Mon-Sat = 26 expected days.
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.MONTHLY, new BigDecimal("15000.00"), 480, 450, false, null);

        List<AttendanceSession> sessions = new ArrayList<>();
        for (int day = 1; day <= 30; day++) {
            LocalDate date = ym.atDay(day);
            if (date.getDayOfWeek().getValue() <= 6) {
                // 8 hours: 09:00 to 17:00
                sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(17, 0)));
            }
        }

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, maid.getJoiningDate(), null, List.of(config),
                sessions, List.of(), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getFullMonthExpectedWorkingDays()).isEqualTo(26);
        assertThat(result.getEligibleWorkingDays()).isEqualTo(26);
        assertThat(result.getWorkedDays()).isEqualTo(26);
        assertThat(result.getExpectedMinutes()).isEqualTo(26 * 480);
        assertThat(result.getActualWorkedMinutes()).isEqualTo(26 * 480);
        assertThat(result.getShortfallMinutes()).isEqualTo(0);
        assertThat(result.getThresholdCrossedDays()).isEqualTo(0);
        assertThat(result.getBaseEarnings()).isEqualByComparingTo("15000.00");
        assertThat(result.getShortfallDeduction()).isEqualByComparingTo("0.00");
        assertThat(result.getOvertimePay()).isEqualByComparingTo("0.00");
        assertThat(result.getFinalPayable()).isEqualByComparingTo("15000.00");
    }

    @Test
    @DisplayName("2. Short day above threshold: Grace floor applies, 0 deduction")
    void testShortDayAboveThreshold_ZeroDeduction_GraceFloor() {
        // 8h expected (480m), threshold 7h30m (450m).
        // On one day, worker works 7h45m (465m).
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.MONTHLY, new BigDecimal("15000.00"), 480, 450, false, null);

        List<AttendanceSession> sessions = new ArrayList<>();
        for (int day = 1; day <= 30; day++) {
            LocalDate date = ym.atDay(day);
            if (date.getDayOfWeek().getValue() <= 6) {
                if (day == 1) {
                    // 7h 45m: 09:00 to 16:45
                    sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(16, 45)));
                } else {
                    sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(17, 0)));
                }
            }
        }

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, maid.getJoiningDate(), null, List.of(config),
                sessions, List.of(), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getThresholdCrossedDays()).isEqualTo(0);
        assertThat(result.getShortfallMinutes()).isEqualTo(0);
        assertThat(result.getShortfallDeduction()).isEqualByComparingTo("0.00");
        assertThat(result.getFinalPayable()).isEqualByComparingTo("15000.00");
    }

    @Test
    @DisplayName("3. Short day below threshold: Chargeable shortfall deduction applied")
    void testShortDayBelowThreshold_ChargeableShortfallDeduction() {
        // On Sept 1, worker works 6 hours (360m) instead of 8h (480m). Threshold is 7h30m (450m).
        // Shortfall = 480 - 360 = 120m (2 hours).
        // Effective hourly rate: 15,000 / (26 * 8) = 15,000 / 208 = ~72.11538...
        // Shortfall deduction = 2 * (15000 / 208) = 144.23
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.MONTHLY, new BigDecimal("15000.00"), 480, 450, false, null);

        List<AttendanceSession> sessions = new ArrayList<>();
        for (int day = 1; day <= 30; day++) {
            LocalDate date = ym.atDay(day);
            if (date.getDayOfWeek().getValue() <= 6) {
                if (day == 1) {
                    // 6h: 09:00 to 15:00
                    sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(15, 0)));
                } else {
                    sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(17, 0)));
                }
            }
        }

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, maid.getJoiningDate(), null, List.of(config),
                sessions, List.of(), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getThresholdCrossedDays()).isEqualTo(1);
        assertThat(result.getShortfallMinutes()).isEqualTo(120);
        assertThat(result.getShortfallDeduction()).isEqualByComparingTo("144.23");
        // Final payable = 15,000 - 144.23 = 14,855.77
        assertThat(result.getFinalPayable()).isEqualByComparingTo("14855.77");
    }

    @Test
    @DisplayName("4. Exact threshold: No shortfall deduction (grace floor boundary)")
    void testExactThreshold_ZeroDeduction() {
        // Worked exactly 7h 30m (450m) on Sept 1.
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.MONTHLY, new BigDecimal("15000.00"), 480, 450, false, null);

        List<AttendanceSession> sessions = new ArrayList<>();
        for (int day = 1; day <= 30; day++) {
            LocalDate date = ym.atDay(day);
            if (date.getDayOfWeek().getValue() <= 6) {
                if (day == 1) {
                    // 7h 30m: 09:00 to 16:30
                    sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(16, 30)));
                } else {
                    sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(17, 0)));
                }
            }
        }

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, maid.getJoiningDate(), null, List.of(config),
                sessions, List.of(), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getThresholdCrossedDays()).isEqualTo(0);
        assertThat(result.getShortfallMinutes()).isEqualTo(0);
        assertThat(result.getShortfallDeduction()).isEqualByComparingTo("0.00");
        assertThat(result.getFinalPayable()).isEqualByComparingTo("15000.00");
    }

    @Test
    @DisplayName("5. Zero attendance on expected day: Absence deducted in monthly mode")
    void testZeroAttendanceOnExpectedDay_AbsenceDeductionInMonthlyMode() {
        // Worker completely absent on Sept 1 (no attendance, not filed as leave).
        // Full month: 26 expected days. 25 worked days.
        // 1 absent day = 8h (480m) shortfall.
        // Hourly rate = 15,000 / 208 = ~72.11538...
        // Shortfall deduction = 8 * (15000 / 208) = 15,000 / 26 = 576.92
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.MONTHLY, new BigDecimal("15000.00"), 480, 450, false, null);

        List<AttendanceSession> sessions = new ArrayList<>();
        for (int day = 2; day <= 30; day++) {
            LocalDate date = ym.atDay(day);
            if (date.getDayOfWeek().getValue() <= 6) {
                sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(17, 0)));
            }
        }

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, maid.getJoiningDate(), null, List.of(config),
                sessions, List.of(), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getWorkedDays()).isEqualTo(25);
        assertThat(result.getThresholdCrossedDays()).isEqualTo(1);
        assertThat(result.getShortfallMinutes()).isEqualTo(480);
        assertThat(result.getShortfallDeduction()).isEqualByComparingTo("576.92");
        assertThat(result.getFinalPayable()).isEqualByComparingTo("14423.08");
    }

    @Test
    @DisplayName("6. Paid leave: Paid day, no shortfall, no overtime from leave")
    void testPaidLeave_PaidDay_NoShortfall_NoOvertime() {
        // Sept 1 is PAID leave. 25 other days worked 8 hours.
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.MONTHLY, new BigDecimal("15000.00"), 480, 450, true, new BigDecimal("1.500"));

        List<AttendanceSession> sessions = new ArrayList<>();
        for (int day = 2; day <= 30; day++) {
            LocalDate date = ym.atDay(day);
            if (date.getDayOfWeek().getValue() <= 6) {
                sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(17, 0)));
            }
        }

        LeaveRecord paidLeave = new LeaveRecord(UUID.randomUUID(), maid, LocalDate.of(2026, 9, 1), LeaveType.PAID, "Medical");

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, maid.getJoiningDate(), null, List.of(config),
                sessions, List.of(paidLeave), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getPaidLeaveDays()).isEqualTo(1);
        assertThat(result.getUnpaidLeaveDays()).isEqualTo(0);
        assertThat(result.getShortfallMinutes()).isEqualTo(0);
        assertThat(result.getShortfallDeduction()).isEqualByComparingTo("0.00");
        assertThat(result.getOvertimePay()).isEqualByComparingTo("0.00");
        assertThat(result.getFinalPayable()).isEqualByComparingTo("15000.00");
    }

    @Test
    @DisplayName("7. Unpaid leave: Proration reduces payable base, 0 shortfall deduction")
    void testUnpaidLeave_ReducesProratedBase_NoShortfallDeduction() {
        // Sept 1 is UNPAID leave. 25 days worked.
        // Base = 15000 * 25 / 26 = 14423.08
        // Shortfall = 0 (unpaid leave does not double-penalize).
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.MONTHLY, new BigDecimal("15000.00"), 480, 450, false, null);

        List<AttendanceSession> sessions = new ArrayList<>();
        for (int day = 2; day <= 30; day++) {
            LocalDate date = ym.atDay(day);
            if (date.getDayOfWeek().getValue() <= 6) {
                sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(17, 0)));
            }
        }

        LeaveRecord unpaidLeave = new LeaveRecord(UUID.randomUUID(), maid, LocalDate.of(2026, 9, 1), LeaveType.UNPAID, "Personal");

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, maid.getJoiningDate(), null, List.of(config),
                sessions, List.of(unpaidLeave), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getUnpaidLeaveDays()).isEqualTo(1);
        assertThat(result.getShortfallMinutes()).isEqualTo(0);
        assertThat(result.getShortfallDeduction()).isEqualByComparingTo("0.00");
        assertThat(result.getBaseEarnings()).isEqualByComparingTo("14423.08");
        assertThat(result.getFinalPayable()).isEqualByComparingTo("14423.08");
    }

    @Test
    @DisplayName("8. Weekly off: Non-working weekday does not generate absence deduction")
    void testWeeklyOff_NotCountedAsAbsence() {
        // Only Mon-Fri configured as working days (no Sat, no Sun).
        // September 2026: 22 weekdays (Mon-Fri).
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = new EmploymentConfig();
        config.setId(UUID.randomUUID());
        config.setMaid(maid);
        config.setSalaryMode(SalaryMode.MONTHLY);
        config.setSalaryAmount(new BigDecimal("22000.00"));
        config.setExpectedMinutesPerDay(480);
        config.setShortfallThresholdMinutes(450);
        config.setOvertimeEnabled(false);
        config.setOvertimeMultiplier(new BigDecimal("1.000"));
        config.setEffectiveFrom(LocalDate.of(2026, 1, 1));

        Set<MaidWorkingDay> workingDays = new HashSet<>();
        for (short d = 1; d <= 5; d++) { // Mon to Fri
            workingDays.add(new MaidWorkingDay(UUID.randomUUID(), config, d));
        }
        config.setWorkingDays(workingDays);

        List<AttendanceSession> sessions = new ArrayList<>();
        for (int day = 1; day <= 30; day++) {
            LocalDate date = ym.atDay(day);
            if (date.getDayOfWeek().getValue() <= 5) {
                sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(17, 0)));
            }
        }

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, maid.getJoiningDate(), null, List.of(config),
                sessions, List.of(), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getFullMonthExpectedWorkingDays()).isEqualTo(22);
        assertThat(result.getEligibleWorkingDays()).isEqualTo(22);
        assertThat(result.getWorkedDays()).isEqualTo(22);
        assertThat(result.getShortfallMinutes()).isEqualTo(0);
        assertThat(result.getFinalPayable()).isEqualByComparingTo("22000.00");
    }

    @Test
    @DisplayName("9. Manual holiday: Excluded from expected days, no absence deduction")
    void testManualHoliday_ExcludedFromExpectedDays() {
        // Sept 1 is a holiday. 25 expected days instead of 26.
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.MONTHLY, new BigDecimal("15000.00"), 480, 450, false, null);

        Holiday holiday = new Holiday(UUID.randomUUID(), owner, LocalDate.of(2026, 9, 1), "Festival");

        List<AttendanceSession> sessions = new ArrayList<>();
        for (int day = 2; day <= 30; day++) {
            LocalDate date = ym.atDay(day);
            if (date.getDayOfWeek().getValue() <= 6) {
                sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(17, 0)));
            }
        }

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, maid.getJoiningDate(), null, List.of(config),
                sessions, List.of(), List.of(holiday), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getFullMonthExpectedWorkingDays()).isEqualTo(25);
        assertThat(result.getEligibleWorkingDays()).isEqualTo(25);
        assertThat(result.getWorkedDays()).isEqualTo(25);
        assertThat(result.getShortfallMinutes()).isEqualTo(0);
        assertThat(result.getFinalPayable()).isEqualByComparingTo("15000.00");
    }

    @Test
    @DisplayName("10. Overtime OFF: Extra hours worked are not paid as overtime")
    void testOvertimeDisabled_ExtraHoursNotPaidAsOvertime() {
        // Worker works 10 hours (09:00 to 19:00 = 600m) on Sept 1, but overtimeEnabled = false.
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.MONTHLY, new BigDecimal("15000.00"), 480, 450, false, new BigDecimal("1.500"));

        List<AttendanceSession> sessions = new ArrayList<>();
        for (int day = 1; day <= 30; day++) {
            LocalDate date = ym.atDay(day);
            if (date.getDayOfWeek().getValue() <= 6) {
                if (day == 1) {
                    sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(19, 0))); // 10h
                } else {
                    sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(17, 0))); // 8h
                }
            }
        }

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, maid.getJoiningDate(), null, List.of(config),
                sessions, List.of(), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getOvertimeMinutes()).isEqualTo(0);
        assertThat(result.getOvertimePay()).isEqualByComparingTo("0.00");
        assertThat(result.getFinalPayable()).isEqualByComparingTo("15000.00");
    }

    @Test
    @DisplayName("11. Overtime ON: Calculates overtime pay using multiplier")
    void testOvertimeEnabled_CalculatesOvertimeWithMultiplier() {
        // 2 hours overtime on Sept 1 (600m worked, 480m expected = 120m OT).
        // Overtime multiplier = 1.500.
        // Hourly rate = 15,000 / (26 * 8) = 15,000 / 208 = ~72.11538...
        // OT Pay = 2 * (15000 / 208) * 1.5 = 216.35
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.MONTHLY, new BigDecimal("15000.00"), 480, 450, true, new BigDecimal("1.500"));

        List<AttendanceSession> sessions = new ArrayList<>();
        for (int day = 1; day <= 30; day++) {
            LocalDate date = ym.atDay(day);
            if (date.getDayOfWeek().getValue() <= 6) {
                if (day == 1) {
                    sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(19, 0))); // 10h
                } else {
                    sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(17, 0))); // 8h
                }
            }
        }

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, maid.getJoiningDate(), null, List.of(config),
                sessions, List.of(), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getOvertimeMinutes()).isEqualTo(120);
        assertThat(result.getOvertimePay()).isEqualByComparingTo("216.35");
        // Base 15000 + 216.35 = 15216.35
        assertThat(result.getFinalPayable()).isEqualByComparingTo("15216.35");
    }

    @Test
    @DisplayName("12. Daily salary mode: Full attendance, partial shortfall, and absent handling")
    void testDailySalaryMode_Calculations() {
        // Daily rate: ₹600. Expected: 8h (480m). Threshold: 7.5h (450m). Hourly = ₹75.
        // Day 1: 8h worked -> 600 base, 0 shortfall
        // Day 2: 6h worked (360m) -> 600 base, 2h shortfall = 2 * 75 = 150 deduction -> net 450
        // Day 3: 0h worked (absent) -> 0 base, 0 shortfall deduction -> net 0 (no double deduction!)
        // Total expected: 3 days in test period.
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.DAILY, new BigDecimal("600.00"), 480, 450, false, null);

        List<AttendanceSession> sessions = List.of(
                createSession(LocalDate.of(2026, 9, 1), LocalTime.of(9, 0), LocalTime.of(17, 0)), // 8h
                createSession(LocalDate.of(2026, 9, 2), LocalTime.of(9, 0), LocalTime.of(15, 0))  // 6h
                // Sept 3: absent
        );

        // Limit month context to Sept 1 - Sept 3 by setting joining/leaving
        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 3), List.of(config),
                sessions, List.of(), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getEligibleWorkingDays()).isEqualTo(3);
        assertThat(result.getWorkedDays()).isEqualTo(2);
        // Base pay for 2 worked days = 2 * 600 = 1200
        assertThat(result.getBaseEarnings()).isEqualByComparingTo("1200.00");
        // Shortfall deduction on Day 2: 2h * 75 = 150
        assertThat(result.getShortfallDeduction()).isEqualByComparingTo("150.00");
        // Final payable = 1200 - 150 = 1050
        assertThat(result.getFinalPayable()).isEqualByComparingTo("1050.00");
    }

    @Test
    @DisplayName("13. Daily salary mode: Paid leave and Overtime")
    void testDailySalaryMode_PaidLeaveAndOvertime() {
        // Daily rate: ₹800. Expected: 8h (480m). Hourly = ₹100. Overtime multiplier = 1.5.
        // Day 1: 10h worked (2h OT) -> 800 base + 2 * 100 * 1.5 = 300 OT = 1100
        // Day 2: Paid leave -> 800 base
        // Total = 1100 + 800 = 1900
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.DAILY, new BigDecimal("800.00"), 480, 450, true, new BigDecimal("1.500"));

        List<AttendanceSession> sessions = List.of(
                createSession(LocalDate.of(2026, 9, 1), LocalTime.of(9, 0), LocalTime.of(19, 0)) // 10h
        );
        LeaveRecord leave = new LeaveRecord(UUID.randomUUID(), maid, LocalDate.of(2026, 9, 2), LeaveType.PAID, "Festival");

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2), List.of(config),
                sessions, List.of(leave), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getPaidLeaveDays()).isEqualTo(1);
        // Base = (1 worked + 1 paid leave) * 800 = 1600
        assertThat(result.getBaseEarnings()).isEqualByComparingTo("1600.00");
        assertThat(result.getOvertimeMinutes()).isEqualTo(120);
        assertThat(result.getOvertimePay()).isEqualByComparingTo("300.00");
        assertThat(result.getFinalPayable()).isEqualByComparingTo("1900.00");
    }

    @Test
    @DisplayName("14. Hourly salary mode: Standard hours and overtime with multiplier")
    void testHourlySalaryMode_StandardHoursAndOvertime() {
        // Hourly rate: ₹100. Overtime multiplier: 1.5. Expected: 8h/day.
        // Day 1: 8 hours standard = 800
        // Day 2: 10 hours (8h standard + 2h OT) = 800 standard + 300 OT = 1100
        // Total = 1600 base + 300 OT = 1900
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.HOURLY, new BigDecimal("100.00"), 480, 450, true, new BigDecimal("1.500"));

        List<AttendanceSession> sessions = List.of(
                createSession(LocalDate.of(2026, 9, 1), LocalTime.of(9, 0), LocalTime.of(17, 0)), // 8h
                createSession(LocalDate.of(2026, 9, 2), LocalTime.of(9, 0), LocalTime.of(19, 0))  // 10h
        );

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2), List.of(config),
                sessions, List.of(), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getBaseEarnings()).isEqualByComparingTo("1600.00");
        assertThat(result.getOvertimeMinutes()).isEqualTo(120);
        assertThat(result.getOvertimePay()).isEqualByComparingTo("300.00");
        assertThat(result.getShortfallDeduction()).isEqualByComparingTo("0.00");
        assertThat(result.getFinalPayable()).isEqualByComparingTo("1900.00");
    }

    @Test
    @DisplayName("15. Hourly salary mode: Paid leave credited at expected hours")
    void testHourlySalaryMode_PaidLeave() {
        // Hourly rate: ₹100. Expected: 8h (480m).
        // Day 1: 5 hours worked = 500
        // Day 2: Paid leave = 8 hours * 100 = 800
        // Total = 1300
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.HOURLY, new BigDecimal("100.00"), 480, 450, false, null);

        List<AttendanceSession> sessions = List.of(
                createSession(LocalDate.of(2026, 9, 1), LocalTime.of(9, 0), LocalTime.of(14, 0)) // 5h
        );
        LeaveRecord leave = new LeaveRecord(UUID.randomUUID(), maid, LocalDate.of(2026, 9, 2), LeaveType.PAID, "Sick");

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 2), List.of(config),
                sessions, List.of(leave), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getBaseEarnings()).isEqualByComparingTo("1300.00");
        assertThat(result.getFinalPayable()).isEqualByComparingTo("1300.00");
    }

    @Test
    @DisplayName("16. Mid-month join: Expected working day proration")
    void testMidMonthJoin_ProrationByExpectedDays() {
        // September 2026: 26 full month expected days.
        // Joining date: Sept 15, 2026.
        // From Sept 15 to Sept 30, there are 14 expected working days (Mon-Sat).
        // Monthly salary: ₹15,000.
        // Prorated base = 15,000 * 14 / 26 = 8076.92
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.MONTHLY, new BigDecimal("15000.00"), 480, 450, false, null);

        List<AttendanceSession> sessions = new ArrayList<>();
        for (int day = 15; day <= 30; day++) {
            LocalDate date = ym.atDay(day);
            if (date.getDayOfWeek().getValue() <= 6) {
                sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(17, 0)));
            }
        }

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, LocalDate.of(2026, 9, 15), null, List.of(config),
                sessions, List.of(), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getFullMonthExpectedWorkingDays()).isEqualTo(26);
        assertThat(result.getEligibleWorkingDays()).isEqualTo(14);
        assertThat(result.getWorkedDays()).isEqualTo(14);
        assertThat(result.getShortfallMinutes()).isEqualTo(0);
        assertThat(result.getBaseEarnings()).isEqualByComparingTo("8076.92");
        assertThat(result.getFinalPayable()).isEqualByComparingTo("8076.92");
    }

    @Test
    @DisplayName("17. Mid-month leave: Expected working day proration")
    void testMidMonthLeave_ProrationByExpectedDays() {
        // Leaving date: Sept 14, 2026.
        // From Sept 1 to Sept 14: 12 expected working days out of 26.
        // Prorated base = 15,000 * 12 / 26 = 6923.08
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.MONTHLY, new BigDecimal("15000.00"), 480, 450, false, null);

        List<AttendanceSession> sessions = new ArrayList<>();
        for (int day = 1; day <= 14; day++) {
            LocalDate date = ym.atDay(day);
            if (date.getDayOfWeek().getValue() <= 6) {
                sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(17, 0)));
            }
        }

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 9, 14), List.of(config),
                sessions, List.of(), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getFullMonthExpectedWorkingDays()).isEqualTo(26);
        assertThat(result.getEligibleWorkingDays()).isEqualTo(12);
        assertThat(result.getWorkedDays()).isEqualTo(12);
        assertThat(result.getBaseEarnings()).isEqualByComparingTo("6923.08");
        assertThat(result.getFinalPayable()).isEqualByComparingTo("6923.08");
    }

    @Test
    @DisplayName("18. Multiple sessions per day: Correctly summed without premature rounding")
    void testMultipleSessionsPerDay_SummedCorrectly() {
        // Sept 1: 09:00 - 13:00 (240m) + 16:00 - 18:00 (120m) = 360m (6 hours)
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.MONTHLY, new BigDecimal("15000.00"), 480, 450, false, null);

        AttendanceSession s1 = createSession(LocalDate.of(2026, 9, 1), LocalTime.of(9, 0), LocalTime.of(13, 0));
        AttendanceSession s2 = createSession(LocalDate.of(2026, 9, 1), LocalTime.of(16, 0), LocalTime.of(18, 0));

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 1), List.of(config),
                List.of(s1, s2), List.of(), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        DailyCalculationResult day1 = result.getDailyBreakdowns().get(0);
        assertThat(day1.getActualMinutes()).isEqualTo(360);
        assertThat(day1.getShortfallMinutes()).isEqualTo(120);
        assertThat(day1.isThresholdCrossed()).isTrue();
    }

    @Test
    @DisplayName("19. Incomplete session: Surfaces warning in payroll result")
    void testIncompleteSession_ProducesWarning() {
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.MONTHLY, new BigDecimal("15000.00"), 480, 450, false, null);

        // Session with no exit time
        AttendanceSession incompleteSession = new AttendanceSession(
                UUID.randomUUID(), maid, LocalDate.of(2026, 9, 1),
                LocalDate.of(2026, 9, 1).atTime(9, 0).atZone(zoneId).toInstant(),
                null, AttendanceStatus.WORKING, "Started", owner, owner
        );

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 1), List.of(config),
                List.of(incompleteSession), List.of(), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getWarnings()).isNotEmpty();
        assertThat(result.getWarnings().get(0)).contains("Unresolved incomplete session");
        assertThat(result.getDailyBreakdowns().get(0).isHasIncompleteSession()).isTrue();
    }

    @Test
    @DisplayName("20. Adjustments: Advance, Bonus, Deduction applied accurately")
    void testAdjustments_AdvanceBonusDeduction() {
        // Base salary: ₹15,000. Full attendance.
        // Bonus: ₹1,000
        // Other Add: ₹500
        // Advance: ₹2,000
        // Deduction: ₹500
        // Final payable = 15,000 + 1,500 - 2,500 = 14,000
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.MONTHLY, new BigDecimal("15000.00"), 480, 450, false, null);

        List<AttendanceSession> sessions = new ArrayList<>();
        for (int day = 1; day <= 30; day++) {
            LocalDate date = ym.atDay(day);
            if (date.getDayOfWeek().getValue() <= 6) {
                sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(17, 0)));
            }
        }

        List<PayrollAdjustment> adjustments = List.of(
                new PayrollAdjustment(UUID.randomUUID(), null, PayrollAdjustmentType.BONUS, new BigDecimal("1000.00"), LocalDate.of(2026, 9, 15), "Diwali bonus"),
                new PayrollAdjustment(UUID.randomUUID(), null, PayrollAdjustmentType.OTHER_ADD, new BigDecimal("500.00"), LocalDate.of(2026, 9, 20), "Travel reimbursement"),
                new PayrollAdjustment(UUID.randomUUID(), null, PayrollAdjustmentType.ADVANCE, new BigDecimal("2000.00"), LocalDate.of(2026, 9, 10), "Emergency cash"),
                new PayrollAdjustment(UUID.randomUUID(), null, PayrollAdjustmentType.DEDUCTION, new BigDecimal("500.00"), LocalDate.of(2026, 9, 25), "Damage deduction")
        );

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, maid.getJoiningDate(), null, List.of(config),
                sessions, List.of(), List.of(), adjustments, "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getBaseEarnings()).isEqualByComparingTo("15000.00");
        assertThat(result.getAdditions()).isEqualByComparingTo("1500.00");
        assertThat(result.getDeductions()).isEqualByComparingTo("2500.00");
        assertThat(result.getFinalPayable()).isEqualByComparingTo("14000.00");
    }

    @Test
    @DisplayName("21. High precision and Half-Up 2-decimal currency rounding")
    void testDecimalPrecisionAndHalfUpRounding() {
        // Salary: ₹13,333.33. Full month expected days: 26.
        // Hourly rate: 13,333.33 / (26 * 8) = 13,333.33 / 208 = 64.102548...
        // 1 day worked 5h 25m (325m) instead of 8h (480m).
        // Shortfall = 155 minutes = 2.583333... hours.
        // Shortfall deduction = 2.583333... * 64.102548... = 165.5982... -> rounds to 165.60
        YearMonth ym = YearMonth.of(2026, 9);
        EmploymentConfig config = createConfig(SalaryMode.MONTHLY, new BigDecimal("13333.33"), 480, 450, false, null);

        List<AttendanceSession> sessions = new ArrayList<>();
        for (int day = 1; day <= 30; day++) {
            LocalDate date = ym.atDay(day);
            if (date.getDayOfWeek().getValue() <= 6) {
                if (day == 1) {
                    // 5h 25m: 09:00 to 14:25
                    sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(14, 25)));
                } else {
                    sessions.add(createSession(date, LocalTime.of(9, 0), LocalTime.of(17, 0)));
                }
            }
        }

        PayrollCalculationContext context = new PayrollCalculationContext(
                ym, maid.getJoiningDate(), null, List.of(config),
                sessions, List.of(), List.of(), List.of(), "INR"
        );

        PayrollCalculationResult result = salaryEngine.calculate(context);

        assertThat(result.getBaseEarnings()).isEqualByComparingTo("13333.33");
        assertThat(result.getShortfallDeduction()).isEqualByComparingTo("165.60");
        // 13,333.33 - 165.60 = 13,167.73
        assertThat(result.getFinalPayable()).isEqualByComparingTo("13167.73");
    }
}
