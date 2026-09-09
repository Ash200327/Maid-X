import { apiRequest } from '../api/client';

export type AttendanceStatus = 'WORKING' | 'COMPLETED' | 'INCOMPLETE' | 'ABSENT';

export interface AttendanceSession {
  id: string;
  maidId: string;
  businessDate: string;
  entryAt?: string;
  exitAt?: string;
  durationMinutes?: number;
  status: AttendanceStatus;
  note?: string;
  createdAt: string;
  updatedAt: string;
}

export interface EntryPayload {
  businessDate?: string;
  clientRequestId?: string;
  note?: string;
}

export interface ExitPayload {
  clientRequestId?: string;
  note?: string;
}

export interface UpdateSessionPayload {
  entryAt?: string;
  exitAt?: string;
  status?: AttendanceStatus;
  note?: string;
}

export interface ManualStatusPayload {
  businessDate: string;
  status: AttendanceStatus;
  note?: string;
}

export interface DashboardMaidEntry {
  maidId: string;
  maidName: string;
  state: 'NOT_STARTED' | 'WORKING' | 'COMPLETED' | 'INCOMPLETE' | 'ABSENT' | 'ON_LEAVE';
  activeSessionId?: string;
  entryAt?: string;
  exitAt?: string;
  completedSessionsCount: number;
  totalWorkedMinutes: number;
}

export interface DashboardResponse {
  date: string;
  activeMaidCount: number;
  entries: DashboardMaidEntry[];
  currentMonth: {
    salaryPayable: number;
    currency: string;
  };
}

export const attendanceService = {
  async getDashboard(date?: string): Promise<DashboardResponse> {
    const query = date ? `?date=${date}` : '';
    return await apiRequest<DashboardResponse>(`/dashboard${query}`);
  },

  async recordEntry(maidId: string, payload?: EntryPayload): Promise<AttendanceSession> {
    return await apiRequest<AttendanceSession>(`/maids/${maidId}/attendance/sessions/entry`, {
      method: 'POST',
      body: payload ? JSON.stringify(payload) : undefined,
    });
  },

  async recordExit(
    maidId: string,
    sessionId: string,
    payload?: ExitPayload
  ): Promise<AttendanceSession> {
    return await apiRequest<AttendanceSession>(
      `/maids/${maidId}/attendance/sessions/${sessionId}/exit`,
      {
        method: 'POST',
        body: payload ? JSON.stringify(payload) : undefined,
      }
    );
  },

  async updateSession(
    maidId: string,
    sessionId: string,
    payload: UpdateSessionPayload
  ): Promise<AttendanceSession> {
    return await apiRequest<AttendanceSession>(
      `/maids/${maidId}/attendance/sessions/${sessionId}`,
      {
        method: 'PATCH',
        body: JSON.stringify(payload),
      }
    );
  },

  async deleteSession(maidId: string, sessionId: string): Promise<void> {
    await apiRequest<void>(`/maids/${maidId}/attendance/sessions/${sessionId}`, {
      method: 'DELETE',
    });
  },

  async recordManualStatus(
    maidId: string,
    payload: ManualStatusPayload
  ): Promise<AttendanceSession> {
    return await apiRequest<AttendanceSession>(`/maids/${maidId}/attendance/manual-status`, {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  async getAttendance(
    maidId: string,
    from?: string,
    to?: string
  ): Promise<AttendanceSession[]> {
    const params = new URLSearchParams();
    if (from) params.append('from', from);
    if (to) params.append('to', to);
    const query = params.toString() ? `?${params.toString()}` : '';
    return await apiRequest<AttendanceSession[]>(`/maids/${maidId}/attendance${query}`);
  },
};
