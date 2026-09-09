package com.example.maidmanager.payroll.engine;

import com.example.maidmanager.attendance.entity.AttendanceSession;
import com.example.maidmanager.employment.entity.EmploymentConfig;
import com.example.maidmanager.holiday.entity.Holiday;
import com.example.maidmanager.leave.entity.LeaveRecord;
import com.example.maidmanager.payroll.entity.PayrollAdjustment;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

public class PayrollCalculationContext {

    private final UUID maidId;
    private final UUID ownerId;
    private final YearMonth payrollMonth;
    private final LocalDate joiningDate;
    private final LocalDate leavingDate;
    private final List<EmploymentConfig> configs;
    private final List<AttendanceSession> sessions;
    private final List<LeaveRecord> leaveRecords;
    private final List<Holiday> holidays;
    private final List<PayrollAdjustment> adjustments;
    private final String currency;

    public PayrollCalculationContext(
            YearMonth payrollMonth,
            LocalDate joiningDate,
            LocalDate leavingDate,
            List<EmploymentConfig> configs,
            List<AttendanceSession> sessions,
            List<LeaveRecord> leaveRecords,
            List<Holiday> holidays,
            List<PayrollAdjustment> adjustments,
            String currency) {
        this(null, null, payrollMonth, joiningDate, leavingDate, configs, sessions, leaveRecords, holidays, adjustments, currency);
    }

    public PayrollCalculationContext(
            UUID maidId,
            UUID ownerId,
            YearMonth payrollMonth,
            LocalDate joiningDate,
            LocalDate leavingDate,
            List<EmploymentConfig> configs,
            List<AttendanceSession> sessions,
            List<LeaveRecord> leaveRecords,
            List<Holiday> holidays,
            List<PayrollAdjustment> adjustments,
            String currency) {
        this.maidId = maidId;
        this.ownerId = ownerId;
        this.payrollMonth = payrollMonth;
        this.joiningDate = joiningDate;
        this.leavingDate = leavingDate;
        this.configs = configs;
        this.sessions = sessions;
        this.leaveRecords = leaveRecords;
        this.holidays = holidays;
        this.adjustments = adjustments;
        this.currency = currency != null ? currency : "INR";
    }

    public UUID getMaidId() {
        return maidId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public YearMonth getPayrollMonth() {
        return payrollMonth;
    }

    public LocalDate getJoiningDate() {
        return joiningDate;
    }

    public LocalDate getLeavingDate() {
        return leavingDate;
    }

    public List<EmploymentConfig> getConfigs() {
        return configs;
    }

    public List<AttendanceSession> getSessions() {
        return sessions;
    }

    public List<LeaveRecord> getLeaveRecords() {
        return leaveRecords;
    }

    public List<Holiday> getHolidays() {
        return holidays;
    }

    public List<PayrollAdjustment> getAdjustments() {
        return adjustments;
    }

    public String getCurrency() {
        return currency;
    }
}
