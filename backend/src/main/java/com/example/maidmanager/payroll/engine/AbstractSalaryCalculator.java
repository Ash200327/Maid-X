package com.example.maidmanager.payroll.engine;

import com.example.maidmanager.attendance.entity.AttendanceSession;
import com.example.maidmanager.common.enums.AttendanceStatus;
import com.example.maidmanager.common.enums.LeaveType;
import com.example.maidmanager.common.enums.PayrollAdjustmentType;
import com.example.maidmanager.employment.entity.EmploymentConfig;
import com.example.maidmanager.employment.entity.MaidWorkingDay;
import com.example.maidmanager.holiday.entity.Holiday;
import com.example.maidmanager.leave.entity.LeaveRecord;
import com.example.maidmanager.payroll.entity.PayrollAdjustment;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class AbstractSalaryCalculator implements SalaryCalculator {

    protected static final MathContext MC = new MathContext(10, RoundingMode.HALF_UP);

    protected static class ProcessedMonthData {
        public int fullMonthExpectedDays = 0;
        public int eligibleWorkingDays = 0;
        public int workedDays = 0;
        public int workedExpectedDays = 0;
        public int paidLeaveDays = 0;
        public int unpaidLeaveDays = 0;
        public int totalExpectedMinutes = 0;
        public int totalActualWorkedMinutes = 0;
        public int totalShortfallMinutes = 0;
        public int thresholdCrossedDays = 0;
        public int totalOvertimeMinutes = 0;
        public List<DailyCalculationResult> dailyResults = new ArrayList<>();
        public List<String> warnings = new ArrayList<>();
        public EmploymentConfig primaryConfig;
    }

    protected ProcessedMonthData processMonthDays(PayrollCalculationContext context) {
        ProcessedMonthData data = new ProcessedMonthData();
        YearMonth ym = context.getPayrollMonth();
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();

        Map<LocalDate, Holiday> holidayMap = context.getHolidays().stream()
                .collect(Collectors.toMap(Holiday::getHolidayDate, h -> h, (h1, h2) -> h1));

        Map<LocalDate, LeaveRecord> leaveMap = context.getLeaveRecords().stream()
                .collect(Collectors.toMap(LeaveRecord::getLeaveDate, l -> l, (l1, l2) -> l1));

        Map<LocalDate, List<AttendanceSession>> sessionsByDate = context.getSessions().stream()
                .collect(Collectors.groupingBy(AttendanceSession::getBusinessDate));

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            final LocalDate currentDate = date;
            Optional<EmploymentConfig> configOpt = context.getConfigs().stream()
                    .filter(c -> !currentDate.isBefore(c.getEffectiveFrom())
                            && (c.getEffectiveTo() == null || !currentDate.isAfter(c.getEffectiveTo())))
                    .findFirst();

            if (configOpt.isEmpty()) {
                DailyCalculationResult emptyDay = new DailyCalculationResult();
                emptyDay.setDate(currentDate);
                data.dailyResults.add(emptyDay);
                continue;
            }

            EmploymentConfig config = configOpt.get();
            if (data.primaryConfig == null) {
                data.primaryConfig = config;
            }

            Set<Short> workingWeekdays = config.getWorkingDays().stream()
                    .map(MaidWorkingDay::getWeekday)
                    .collect(Collectors.toSet());

            short weekday = (short) currentDate.getDayOfWeek().getValue();
            boolean isConfiguredWeekday = workingWeekdays.contains(weekday);
            boolean isHoliday = holidayMap.containsKey(currentDate);
            boolean isEmployed = !currentDate.isBefore(context.getJoiningDate())
                    && (context.getLeavingDate() == null || !currentDate.isAfter(context.getLeavingDate()));

            boolean isFullMonthExpected = isConfiguredWeekday && !isHoliday;
            boolean isEligibleExpected = isFullMonthExpected && isEmployed;

            if (isFullMonthExpected) {
                data.fullMonthExpectedDays++;
            }

            DailyCalculationResult dayRes = new DailyCalculationResult();
            dayRes.setDate(currentDate);
            dayRes.setExpectedWorkday(isEligibleExpected);
            dayRes.setHoliday(isHoliday);

            if (leaveMap.containsKey(currentDate)) {
                LeaveRecord lr = leaveMap.get(currentDate);
                dayRes.setLeave(true);
                dayRes.setLeaveType(lr.getLeaveType());
            }

            List<AttendanceSession> daySessions = sessionsByDate.getOrDefault(currentDate, List.of());
            int actualMins = 0;
            boolean hasIncomplete = false;

            for (AttendanceSession session : daySessions) {
                if (session.getStatus() == AttendanceStatus.WORKING && session.getExitAt() == null) {
                    hasIncomplete = true;
                } else if (session.getStatus() == AttendanceStatus.INCOMPLETE) {
                    hasIncomplete = true;
                } else if (session.getStatus() == AttendanceStatus.COMPLETED && session.getEntryAt() != null && session.getExitAt() != null) {
                    actualMins += (int) Duration.between(session.getEntryAt(), session.getExitAt()).toMinutes();
                }
            }

            dayRes.setHasIncompleteSession(hasIncomplete);
            if (hasIncomplete) {
                data.warnings.add("Unresolved incomplete session on " + currentDate);
            }

            dayRes.setActualMinutes(actualMins);
            data.totalActualWorkedMinutes += actualMins;

            if (isEligibleExpected) {
                data.eligibleWorkingDays++;
                int expectedMins = config.getExpectedMinutesPerDay();
                dayRes.setExpectedMinutes(expectedMins);
                data.totalExpectedMinutes += expectedMins;

                if (dayRes.isLeave() && dayRes.getLeaveType() == LeaveType.PAID) {
                    data.paidLeaveDays++;
                    dayRes.setThresholdCrossed(false);
                    dayRes.setShortfallMinutes(0);
                } else if (dayRes.isLeave() && dayRes.getLeaveType() == LeaveType.UNPAID) {
                    data.unpaidLeaveDays++;
                    dayRes.setThresholdCrossed(false);
                    dayRes.setShortfallMinutes(0);
                } else {
                    if (actualMins > 0) {
                        data.workedDays++;
                        data.workedExpectedDays++;
                    }

                    // Shortfall threshold check (Grace floor)
                    int threshold = config.getShortfallThresholdMinutes();
                    if (actualMins >= threshold) {
                        dayRes.setThresholdCrossed(false);
                        dayRes.setShortfallMinutes(0);
                    } else {
                        dayRes.setThresholdCrossed(true);
                        int shortfall = Math.max(0, expectedMins - actualMins);
                        dayRes.setShortfallMinutes(shortfall);
                        data.totalShortfallMinutes += shortfall;
                        data.thresholdCrossedDays++;
                    }

                    // Overtime check
                    if (config.isOvertimeEnabled() && actualMins > expectedMins) {
                        int ot = actualMins - expectedMins;
                        dayRes.setOvertimeMinutes(ot);
                        data.totalOvertimeMinutes += ot;
                    }
                }
            } else if (actualMins > 0) {
                // Worked on a non-expected day (holiday or weekly off)
                data.workedDays++;
                if (config.isOvertimeEnabled()) {
                    dayRes.setOvertimeMinutes(actualMins);
                    data.totalOvertimeMinutes += actualMins;
                }
            }

            data.dailyResults.add(dayRes);
        }

        if (data.primaryConfig == null && !context.getConfigs().isEmpty()) {
            data.primaryConfig = context.getConfigs().get(0);
        }

        return data;
    }

    protected BigDecimal calculateAdditions(List<PayrollAdjustment> adjustments) {
        return adjustments.stream()
                .filter(a -> a.getType() == PayrollAdjustmentType.BONUS || a.getType() == PayrollAdjustmentType.OTHER_ADD)
                .map(PayrollAdjustment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    protected BigDecimal calculateDeductions(List<PayrollAdjustment> adjustments) {
        return adjustments.stream()
                .filter(a -> a.getType() == PayrollAdjustmentType.ADVANCE
                        || a.getType() == PayrollAdjustmentType.DEDUCTION
                        || a.getType() == PayrollAdjustmentType.OTHER_DEDUCTION)
                .map(PayrollAdjustment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }
}
