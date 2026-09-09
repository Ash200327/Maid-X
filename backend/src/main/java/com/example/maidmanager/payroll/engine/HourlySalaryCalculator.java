package com.example.maidmanager.payroll.engine;

import com.example.maidmanager.common.enums.SalaryMode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class HourlySalaryCalculator extends AbstractSalaryCalculator {

    @Override
    public boolean supports(SalaryMode mode) {
        return mode == SalaryMode.HOURLY;
    }

    @Override
    public PayrollCalculationResult calculate(PayrollCalculationContext context) {
        ProcessedMonthData data = processMonthDays(context);
        PayrollCalculationResult result = new PayrollCalculationResult();

        result.setMonth(context.getPayrollMonth().toString());
        result.setSalaryMode(SalaryMode.HOURLY);
        result.setCurrency(context.getCurrency());
        result.setFullMonthExpectedWorkingDays(data.fullMonthExpectedDays);
        result.setEligibleWorkingDays(data.eligibleWorkingDays);
        result.setWorkedDays(data.workedDays);
        result.setPaidLeaveDays(data.paidLeaveDays);
        result.setUnpaidLeaveDays(data.unpaidLeaveDays);
        result.setExpectedMinutes(data.totalExpectedMinutes);
        result.setActualWorkedMinutes(data.totalActualWorkedMinutes);
        result.setShortfallMinutes(data.totalShortfallMinutes);
        result.setThresholdCrossedDays(data.thresholdCrossedDays);
        result.setOvertimeMinutes(data.totalOvertimeMinutes);
        result.setDailyBreakdowns(data.dailyResults);
        result.setWarnings(data.warnings);

        BigDecimal hourlyRate = data.primaryConfig != null ? data.primaryConfig.getSalaryAmount() : BigDecimal.ZERO;
        result.setConfiguredSalary(hourlyRate.setScale(2, RoundingMode.HALF_UP));

        if (hourlyRate.compareTo(BigDecimal.ZERO) == 0 || data.primaryConfig == null) {
            result.setBaseEarnings(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            result.setShortfallDeduction(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            result.setOvertimePay(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
            BigDecimal additions = calculateAdditions(context.getAdjustments());
            BigDecimal deductions = calculateDeductions(context.getAdjustments());
            result.setAdditions(additions);
            result.setDeductions(deductions);
            result.setFinalPayable(additions.subtract(deductions).setScale(2, RoundingMode.HALF_UP));
            return result;
        }

        // Standard worked minutes (excluding overtime portion to avoid double counting)
        int standardWorkedMinutes = Math.max(0, data.totalActualWorkedMinutes - data.totalOvertimeMinutes);
        int paidLeaveMinutes = data.paidLeaveDays * data.primaryConfig.getExpectedMinutesPerDay();
        int totalPayableStandardMinutes = standardWorkedMinutes + paidLeaveMinutes;

        BigDecimal payableHours = BigDecimal.valueOf(totalPayableStandardMinutes).divide(BigDecimal.valueOf(60), MC);
        BigDecimal baseEarnings = payableHours.multiply(hourlyRate);

        // Overtime pay
        BigDecimal overtimePay = BigDecimal.ZERO;
        if (data.primaryConfig.isOvertimeEnabled() && data.totalOvertimeMinutes > 0) {
            BigDecimal overtimeHours = BigDecimal.valueOf(data.totalOvertimeMinutes).divide(BigDecimal.valueOf(60), MC);
            BigDecimal multiplier = data.primaryConfig.getOvertimeMultiplier();
            overtimePay = overtimeHours.multiply(hourlyRate).multiply(multiplier);
        }

        BigDecimal additions = calculateAdditions(context.getAdjustments());
        BigDecimal deductions = calculateDeductions(context.getAdjustments());

        BigDecimal finalPayable = baseEarnings
                .add(overtimePay)
                .add(additions)
                .subtract(deductions);

        result.setBaseEarnings(baseEarnings.setScale(2, RoundingMode.HALF_UP));
        result.setShortfallDeduction(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        result.setOvertimePay(overtimePay.setScale(2, RoundingMode.HALF_UP));
        result.setAdditions(additions);
        result.setDeductions(deductions);
        result.setFinalPayable(finalPayable.setScale(2, RoundingMode.HALF_UP));

        return result;
    }
}
