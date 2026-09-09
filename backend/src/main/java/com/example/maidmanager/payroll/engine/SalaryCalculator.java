package com.example.maidmanager.payroll.engine;

import com.example.maidmanager.common.enums.SalaryMode;

public interface SalaryCalculator {
    boolean supports(SalaryMode mode);
    PayrollCalculationResult calculate(PayrollCalculationContext context);
}
