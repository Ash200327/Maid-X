import { apiRequest } from '../api/client';

export type PayrollStatus = 'DRAFT' | 'FINALIZED' | 'PAID';

export type PayrollAdjustmentType =
  | 'ADVANCE'
  | 'BONUS'
  | 'DEDUCTION'
  | 'OTHER_ADD'
  | 'OTHER_DEDUCTION';

export interface DailyCalculationResult {
  date: string;
  isExpectedWorkday: boolean;
  isHoliday: boolean;
  isLeave: boolean;
  leaveType?: 'PAID' | 'UNPAID';
  expectedMinutes: number;
  actualMinutes: number;
  shortfallMinutes: number;
  thresholdCrossed: boolean;
  overtimeMinutes: number;
  hasIncompleteSession: boolean;
}

export interface PayrollCalculationResult {
  month: string;
  salaryMode: 'MONTHLY' | 'DAILY' | 'HOURLY';
  currency: string;
  configuredSalary: number;
  fullMonthExpectedWorkingDays: number;
  eligibleWorkingDays: number;
  workedDays: number;
  paidLeaveDays: number;
  unpaidLeaveDays: number;
  expectedMinutes: number;
  actualWorkedMinutes: number;
  shortfallMinutes: number;
  thresholdCrossedDays: number;
  overtimeMinutes: number;
  baseEarnings: number;
  shortfallDeduction: number;
  overtimePay: number;
  additions: number;
  deductions: number;
  finalPayable: number;
  warnings: string[];
  dailyBreakdowns: DailyCalculationResult[];
}

export interface PayrollAdjustment {
  id: string;
  type: PayrollAdjustmentType;
  amount: number;
  adjustmentDate: string;
  reason?: string;
  createdAt: string;
}

export interface PayrollRun {
  id: string;
  maidId: string;
  maidName: string;
  payrollMonth: string;
  status: PayrollStatus;
  finalizedAt?: string;
  paidAt?: string;
  paymentMethod?: string;
  paymentNote?: string;
  baseEarnings: number;
  shortfallDeduction: number;
  overtimePay: number;
  additions: number;
  deductions: number;
  finalPayable: number;
  currency: string;
  adjustments: PayrollAdjustment[];
  calculation?: PayrollCalculationResult;
}

export interface MonthlyReportResponse {
  month: string;
  totalMaids: number;
  draftCount: number;
  finalizedCount: number;
  paidCount: number;
  totalPayable: number;
  totalPaid: number;
  currency: string;
  runs: PayrollRun[];
}

export interface AddAdjustmentPayload {
  type: PayrollAdjustmentType;
  amount: number;
  adjustmentDate: string;
  reason?: string;
}

export interface MarkPaidPayload {
  paidAt?: string;
  paymentMethod: string;
  paymentNote?: string;
}

export const payrollService = {
  async calculatePreview(maidId: string, month: string): Promise<PayrollCalculationResult> {
    return await apiRequest<PayrollCalculationResult>('/payroll/runs/calculate', {
      method: 'POST',
      body: JSON.stringify({ maidId, month }),
    });
  },

  async getPayrollRuns(params?: {
    month?: string;
    status?: PayrollStatus;
    maidId?: string;
  }): Promise<PayrollRun[]> {
    const searchParams = new URLSearchParams();
    if (params?.month) searchParams.append('month', params.month);
    if (params?.status) searchParams.append('status', params.status);
    if (params?.maidId) searchParams.append('maidId', params.maidId);
    const query = searchParams.toString() ? `?${searchParams.toString()}` : '';
    return await apiRequest<PayrollRun[]>(`/payroll/runs${query}`);
  },

  async createOrGetDraftRun(maidId: string, month: string): Promise<PayrollRun> {
    return await apiRequest<PayrollRun>('/payroll/runs', {
      method: 'POST',
      body: JSON.stringify({ maidId, month }),
    });
  },

  async getPayrollRunById(id: string): Promise<PayrollRun> {
    return await apiRequest<PayrollRun>(`/payroll/runs/${id}`);
  },

  async addAdjustment(runId: string, payload: AddAdjustmentPayload): Promise<PayrollAdjustment> {
    return await apiRequest<PayrollAdjustment>(`/payroll/runs/${runId}/adjustments`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  async deleteAdjustment(runId: string, adjustmentId: string): Promise<void> {
    await apiRequest<void>(`/payroll/runs/${runId}/adjustments/${adjustmentId}`, {
      method: 'DELETE',
    });
  },

  async finalizePayrollRun(runId: string): Promise<PayrollRun> {
    return await apiRequest<PayrollRun>(`/payroll/runs/${runId}/finalize`, {
      method: 'POST',
    });
  },

  async markPaid(runId: string, payload: MarkPaidPayload): Promise<PayrollRun> {
    return await apiRequest<PayrollRun>(`/payroll/runs/${runId}/mark-paid`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  async getMonthlyReport(month?: string): Promise<MonthlyReportResponse> {
    const query = month ? `?month=${encodeURIComponent(month)}` : '';
    return await apiRequest<MonthlyReportResponse>(`/reports/monthly${query}`);
  },

  async getMonthlyReportCsv(month?: string): Promise<string> {
    const query = month ? `?month=${encodeURIComponent(month)}` : '';
    return await apiRequest<string>(`/reports/monthly.csv${query}`);
  },
};
