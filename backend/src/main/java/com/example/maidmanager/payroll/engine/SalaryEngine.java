package com.example.maidmanager.payroll.engine;

import com.example.maidmanager.common.enums.SalaryMode;
import com.example.maidmanager.common.exception.BadRequestException;
import com.example.maidmanager.employment.entity.EmploymentConfig;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalaryEngine {

    private final List<SalaryCalculator> calculators;

    public SalaryEngine(List<SalaryCalculator> calculators) {
        this.calculators = calculators;
    }

    public PayrollCalculationResult calculate(PayrollCalculationContext context) {
        if (context.getConfigs().isEmpty()) {
            throw new BadRequestException("No employment configuration found for payroll period.");
        }

        EmploymentConfig primaryConfig = context.getConfigs().get(0);
        SalaryMode mode = primaryConfig.getSalaryMode();

        SalaryCalculator calculator = calculators.stream()
                .filter(c -> c.supports(mode))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Unsupported salary mode: " + mode));

        return calculator.calculate(context);
    }
}
