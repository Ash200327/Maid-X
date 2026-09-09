import { apiRequest } from '../api/client';
import { authStorage } from './authStorage';

export interface OwnerProfile {
  id: string;
  name: string;
  email: string;
  phone?: string;
  timezone: string;
  currencyCode: string;
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  owner: OwnerProfile;
}

export interface RegisterPayload {
  name: string;
  email: string;
  password: string;
  phone?: string;
  timezone?: string;
  currencyCode?: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface UpdateProfilePayload {
  name?: string;
  phone?: string;
  timezone?: string;
  currencyCode?: string;
}

export const authService = {
  async register(payload: RegisterPayload): Promise<AuthResponse> {
    const data = await apiRequest<AuthResponse>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload),
      requiresAuth: false,
    });
    await authStorage.setTokens(data.accessToken, data.refreshToken);
    return data;
  },

  async login(payload: LoginPayload): Promise<AuthResponse> {
    const data = await apiRequest<AuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify(payload),
      requiresAuth: false,
    });
    await authStorage.setTokens(data.accessToken, data.refreshToken);
    return data;
  },

  async logout(): Promise<void> {
    const refreshToken = await authStorage.getRefreshToken();
    try {
      await apiRequest<void>('/auth/logout', {
        method: 'POST',
        body: JSON.stringify({ refreshToken }),
        requiresAuth: true,
      });
    } catch {
      // Clean local tokens even if server logout call fails
    } finally {
      await authStorage.clearTokens();
    }
  },

  async getProfile(): Promise<OwnerProfile> {
    return await apiRequest<OwnerProfile>('/me', {
      method: 'GET',
      requiresAuth: true,
    });
  },

  async updateProfile(payload: UpdateProfilePayload): Promise<OwnerProfile> {
    return await apiRequest<OwnerProfile>('/me', {
      method: 'PATCH',
      body: JSON.stringify(payload),
      requiresAuth: true,
    });
  },

  async isAuthenticated(): Promise<boolean> {
    const token = await authStorage.getAccessToken();
    return !!token;
  },
};
