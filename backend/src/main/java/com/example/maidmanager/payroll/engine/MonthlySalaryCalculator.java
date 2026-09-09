package com.example.maidmanager.payroll.engine;

import com.example.maidmanager.common.enums.SalaryMode;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class MonthlySalaryCalculator extends AbstractSalaryCalculator {

    @Override
    public boolean supports(SalaryMode mode) {
        return mode == SalaryMode.MONTHLY;
    }

    @Override
    public PayrollCalculationResult calculate(PayrollCalculationContext context) {
        ProcessedMonthData data = processMonthDays(context);
        PayrollCalculationResult result = new PayrollCalculationResult();

        result.setMonth(context.getPayrollMonth().toString());
        result.setSalaryMode(SalaryMode.MONTHLY);
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

        BigDecimal monthlySalary = data.primaryConfig != null ? data.primaryConfig.getSalaryAmount() : BigDecimal.ZERO;
        result.setConfiguredSalary(monthlySalary.setScale(2, RoundingMode.HALF_UP));

        if (data.fullMonthExpectedDays == 0 || monthlySalary.compareTo(BigDecimal.ZERO) == 0) {
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

        int payableDays = Math.max(0, data.eligibleWorkingDays - data.unpaidLeaveDays);
        BigDecimal baseEarnings = monthlySalary
                .multiply(BigDecimal.valueOf(payableDays))
                .divide(BigDecimal.valueOf(data.fullMonthExpectedDays), MC);

        // Effective hourly rate based on full-month expected hours
        int expectedMinutesPerDay = data.primaryConfig.getExpectedMinutesPerDay();
        BigDecimal fullMonthExpectedHours = BigDecimal.valueOf(data.fullMonthExpectedDays)
                .multiply(BigDecimal.valueOf(expectedMinutesPerDay))
                .divide(BigDecimal.valueOf(60), MC);

        BigDecimal hourlyRate = monthlySalary.divide(fullMonthExpectedHours, MC);

        // Shortfall deduction
        BigDecimal shortfallHours = BigDecimal.valueOf(data.totalShortfallMinutes).divide(BigDecimal.valueOf(60), MC);
        BigDecimal shortfallDeduction = shortfallHours.multiply(hourlyRate);

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
                .subtract(shortfallDeduction)
                .add(overtimePay)
                .add(additions)
                .subtract(deductions);

        result.setBaseEarnings(baseEarnings.setScale(2, RoundingMode.HALF_UP));
        result.setShortfallDeduction(shortfallDeduction.setScale(2, RoundingMode.HALF_UP));
        result.setOvertimePay(overtimePay.setScale(2, RoundingMode.HALF_UP));
        result.setAdditions(additions);
        result.setDeductions(deductions);
        result.setFinalPayable(finalPayable.setScale(2, RoundingMode.HALF_UP));

        return result;
    }
}
