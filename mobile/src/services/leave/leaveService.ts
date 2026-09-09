import { apiRequest } from '../api/client';

export type LeaveType = 'PAID' | 'UNPAID';

export interface LeaveRecord {
  id: string;
  maidId: string;
  leaveDate: string;
  leaveType: LeaveType;
  note?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateLeavePayload {
  leaveDate: string;
  leaveType: LeaveType;
  note?: string;
}

export interface UpdateLeavePayload {
  leaveType?: LeaveType;
  note?: string;
}

export const leaveService = {
  async getLeaveRecords(
    maidId: string,
    from?: string,
    to?: string
  ): Promise<LeaveRecord[]> {
    const params = new URLSearchParams();
    if (from) params.append('from', from);
    if (to) params.append('to', to);
    const query = params.toString() ? `?${params.toString()}` : '';
    return await apiRequest<LeaveRecord[]>(`/maids/${maidId}/leave${query}`);
  },

  async createLeave(
    maidId: string,
    payload: CreateLeavePayload
  ): Promise<LeaveRecord> {
    return await apiRequest<LeaveRecord>(`/maids/${maidId}/leave`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  async updateLeave(
    maidId: string,
    leaveId: string,
    payload: UpdateLeavePayload
  ): Promise<LeaveRecord> {
    return await apiRequest<LeaveRecord>(`/maids/${maidId}/leave/${leaveId}`, {
      method: 'PATCH',
      body: JSON.stringify(payload),
    });
  },

  async deleteLeave(maidId: string, leaveId: string): Promise<void> {
    await apiRequest<void>(`/maids/${maidId}/leave/${leaveId}`, {
      method: 'DELETE',
    });
  },
};
