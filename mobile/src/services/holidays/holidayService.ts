import { apiRequest } from '../api/client';

export interface Holiday {
  id: string;
  holidayDate: string;
  name: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateHolidayPayload {
  holidayDate: string;
  name: string;
}

export interface UpdateHolidayPayload {
  holidayDate?: string;
  name?: string;
}

export const holidayService = {
  async listHolidays(
    year?: number,
    month?: number,
    from?: string,
    to?: string
  ): Promise<Holiday[]> {
    const params = new URLSearchParams();
    if (year !== undefined) params.append('year', String(year));
    if (month !== undefined) params.append('month', String(month));
    if (from) params.append('from', from);
    if (to) params.append('to', to);

    const query = params.toString() ? `?${params.toString()}` : '';
    return await apiRequest<Holiday[]>(`/holidays${query}`);
  },

  async createHoliday(payload: CreateHolidayPayload): Promise<Holiday> {
    return await apiRequest<Holiday>('/holidays', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
  },

  async updateHoliday(
    holidayId: string,
    payload: UpdateHolidayPayload
  ): Promise<Holiday> {
    return await apiRequest<Holiday>(`/holidays/${holidayId}`, {
      method: 'PATCH',
      body: JSON.stringify(payload),
    });
  },

  async deleteHoliday(holidayId: string): Promise<void> {
    await apiRequest<void>(`/holidays/${holidayId}`, {
      method: 'DELETE',
    });
  },
};
