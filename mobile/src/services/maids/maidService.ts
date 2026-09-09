import { apiRequest } from '../api/client';

export type SalaryMode = 'MONTHLY' | 'DAILY' | 'HOURLY';

export interface EmploymentConfig {
  id: string;
  maidId: string;
  effectiveFrom: string;
  effectiveTo?: string;
  salaryMode: SalaryMode;
  salaryAmount: number;
  expectedMinutesPerDay: number;
  shortfallThresholdMinutes: number;
  overtimeEnabled: boolean;
  overtimeMultiplier: number;
  workingDays: number[];
  createdAt: string;
}

export interface CreateEmploymentConfigPayload {
  effectiveFrom?: string;
  effectiveTo?: string;
  salaryMode: SalaryMode;
  salaryAmount: number;
  expectedMinutesPerDay: number;
  shortfallThresholdMinutes: number;
  overtimeEnabled?: boolean;
  overtimeMultiplier?: number;
  workingDays: number[];
}

export interface MaidSummary {
  id: string;
  name: string;
  phone?: string;
  joiningDate: string;
  leavingDate?: string;
  active: boolean;
  activeSalaryMode?: SalaryMode;
  activeSalaryAmount?: number;
}

export interface MaidDetail {
  id: string;
  name: string;
  phone?: string;
  joiningDate: string;
  leavingDate?: string;
  notes?: string;
  active: boolean;
  currentConfig?: EmploymentConfig;
  createdAt: string;
}

export interface CreateMaidPayload {
  name: string;
  phone?: string;
  joiningDate: string;
  leavingDate?: string;
  notes?: string;
  employmentConfig: CreateEmploymentConfigPayload;
}

export interface UpdateMaidPayload {
  name?: string;
  phone?: string;
  joiningDate?: string;
  leavingDate?: string;
  notes?: string;
  isActive?: boolean;
}

export const maidService = {
  async listMaids(active?: boolean, search?: string): Promise<MaidSummary[]> {
    const params = new URLSearchParams();
    if (active !== undefined) params.append('active', String(active));
    if (search) params.append('search', search);

    const query = params.toString() ? `?${params.toString()}` : '';
    return await apiRequest<MaidSummary[]>(`/maids${query}`);
  },

  async createMaid(payload: CreateMaidPayload): Promise<MaidDetail> {
    return await apiRequest<MaidDetail>('/maids', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  async getMaid(maidId: string): Promise<MaidDetail> {
    return await apiRequest<MaidDetail>(`/maids/${maidId}`);
  },

  async updateMaid(maidId: string, payload: UpdateMaidPayload): Promise<MaidDetail> {
    return await apiRequest<MaidDetail>(`/maids/${maidId}`, {
      method: 'PATCH',
      body: JSON.stringify(payload),
    });
  },

  async archiveMaid(maidId: string): Promise<MaidDetail> {
    return await apiRequest<MaidDetail>(`/maids/${maidId}/archive`, {
      method: 'POST',
    });
  },

  async createEmploymentConfig(
    maidId: string,
    payload: CreateEmploymentConfigPayload
  ): Promise<EmploymentConfig> {
    return await apiRequest<EmploymentConfig>(`/maids/${maidId}/employment-configs`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  async getEmploymentConfigs(maidId: string): Promise<EmploymentConfig[]> {
    return await apiRequest<EmploymentConfig[]>(`/maids/${maidId}/employment-configs`);
  },
};
