import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';

const ACCESS_TOKEN_KEY = 'maid_x_access_token';
const REFRESH_TOKEN_KEY = 'maid_x_refresh_token';

// In-memory fallback for web/unsupported environments
const memoryStorage: Record<string, string> = {};

async function isSecureStoreAvailable(): Promise<boolean> {
  if (Platform.OS === 'web') return false;
  return await SecureStore.isAvailableAsync();
}

export const authStorage = {
  async setTokens(accessToken: string, refreshToken: string): Promise<void> {
    const available = await isSecureStoreAvailable();
    if (available) {
      await SecureStore.setItemAsync(ACCESS_TOKEN_KEY, accessToken);
      await SecureStore.setItemAsync(REFRESH_TOKEN_KEY, refreshToken);
    } else {
      memoryStorage[ACCESS_TOKEN_KEY] = accessToken;
      memoryStorage[REFRESH_TOKEN_KEY] = refreshToken;
    }
  },

  async getAccessToken(): Promise<string | null> {
    const available = await isSecureStoreAvailable();
    if (available) {
      return await SecureStore.getItemAsync(ACCESS_TOKEN_KEY);
    }
    return memoryStorage[ACCESS_TOKEN_KEY] || null;
  },

  async getRefreshToken(): Promise<string | null> {
    const available = await isSecureStoreAvailable();
    if (available) {
      return await SecureStore.getItemAsync(REFRESH_TOKEN_KEY);
    }
    return memoryStorage[REFRESH_TOKEN_KEY] || null;
  },

  async clearTokens(): Promise<void> {
    const available = await isSecureStoreAvailable();
    if (available) {
      await SecureStore.deleteItemAsync(ACCESS_TOKEN_KEY);
      await SecureStore.deleteItemAsync(REFRESH_TOKEN_KEY);
    } else {
      delete memoryStorage[ACCESS_TOKEN_KEY];
      delete memoryStorage[REFRESH_TOKEN_KEY];
    }
  },
};
