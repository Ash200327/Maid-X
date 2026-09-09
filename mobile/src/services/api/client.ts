import { authStorage } from '../auth/authStorage';

export const API_BASE_URL = process.env.EXPO_PUBLIC_API_URL || 'http://localhost:8080/api/v1';

export interface ApiErrorResponse {
  code: string;
  message: string;
  fieldErrors?: Record<string, string>;
  requestId?: string;
}

export class ApiError extends Error {
  code: string;
  status: number;
  fieldErrors?: Record<string, string>;
  requestId?: string;

  constructor(status: number, errorData: ApiErrorResponse) {
    super(errorData.message || 'An API error occurred');
    this.name = 'ApiError';
    this.status = status;
    this.code = errorData.code || 'UNKNOWN_ERROR';
    this.fieldErrors = errorData.fieldErrors;
    this.requestId = errorData.requestId;
  }
}

interface RequestOptions extends RequestInit {
  requiresAuth?: boolean;
}

let isRefreshing = false;
let refreshSubscribers: ((token: string) => void)[] = [];

function subscribeTokenRefresh(cb: (token: string) => void) {
  refreshSubscribers.push(cb);
}

function onRefreshed(token: string) {
  refreshSubscribers.forEach((cb) => cb(token));
  refreshSubscribers = [];
}

export async function apiRequest<T>(
  endpoint: string,
  options: RequestOptions = {}
): Promise<T> {
  const { requiresAuth = true, headers: customHeaders, ...restOptions } = options;

  const url = `${API_BASE_URL}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`;
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    Accept: 'application/json',
    ...(customHeaders as Record<string, string>),
  };

  if (requiresAuth) {
    const accessToken = await authStorage.getAccessToken();
    if (accessToken) {
      headers['Authorization'] = `Bearer ${accessToken}`;
    }
  }

  const response = await fetch(url, {
    headers,
    ...restOptions,
  });

  // Handle 401 and token refresh
  if (response.status === 401 && requiresAuth) {
    const refreshToken = await authStorage.getRefreshToken();
    if (refreshToken) {
      if (!isRefreshing) {
        isRefreshing = true;
        try {
          const refreshRes = await fetch(`${API_BASE_URL}/auth/refresh`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ refreshToken }),
          });

          if (refreshRes.ok) {
            const data = await refreshRes.json();
            await authStorage.setTokens(data.accessToken, data.refreshToken);
            onRefreshed(data.accessToken);
            isRefreshing = false;

            // Retry original request with new token
            headers['Authorization'] = `Bearer ${data.accessToken}`;
            const retryRes = await fetch(url, { headers, ...restOptions });
            if (!retryRes.ok) {
              const err = await parseError(retryRes);
              throw new ApiError(retryRes.status, err);
            }
            if (retryRes.status === 204) return null as T;
            return await retryRes.json();
          } else {
            await authStorage.clearTokens();
            isRefreshing = false;
          }
        } catch {
          await authStorage.clearTokens();
          isRefreshing = false;
        }
      } else {
        // Wait for active refresh
        return new Promise<T>((resolve, reject) => {
          subscribeTokenRefresh(async (newToken: string) => {
            try {
              headers['Authorization'] = `Bearer ${newToken}`;
              const retryRes = await fetch(url, { headers, ...restOptions });
              if (!retryRes.ok) {
                const err = await parseError(retryRes);
                reject(new ApiError(retryRes.status, err));
              } else {
                if (retryRes.status === 204) resolve(null as T);
                else resolve(await retryRes.json());
              }
            } catch (err) {
              reject(err);
            }
          });
        });
      }
    }
  }

  if (!response.ok) {
    const err = await parseError(response);
    throw new ApiError(response.status, err);
  }

  if (response.status === 204) {
    return null as T;
  }

  return await response.json();
}

async function parseError(response: Response): Promise<ApiErrorResponse> {
  try {
    return await response.json();
  } catch {
    return {
      code: 'HTTP_' + response.status,
      message: response.statusText || 'Request failed',
    };
  }
}
