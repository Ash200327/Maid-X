import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService, OwnerProfile, LoginPayload, RegisterPayload } from '../services/auth/authService';

interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  owner: OwnerProfile | null;
  login: (payload: LoginPayload) => Promise<void>;
  register: (payload: RegisterPayload) => Promise<void>;
  logout: () => Promise<void>;
  refreshProfile: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(false);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [owner, setOwner] = useState<OwnerProfile | null>(null);

  useEffect(() => {
    checkAuth();
  }, []);

  const checkAuth = async () => {
    try {
      const authenticated = await authService.isAuthenticated();
      if (authenticated) {
        const profile = await authService.getProfile();
        setOwner(profile);
        setIsAuthenticated(true);
      } else {
        setOwner(null);
        setIsAuthenticated(false);
      }
    } catch {
      setOwner(null);
      setIsAuthenticated(false);
    } finally {
      setIsLoading(false);
    }
  };

  const login = async (payload: LoginPayload) => {
    const res = await authService.login(payload);
    setOwner(res.owner);
    setIsAuthenticated(true);
  };

  const register = async (payload: RegisterPayload) => {
    const res = await authService.register(payload);
    setOwner(res.owner);
    setIsAuthenticated(true);
  };

  const logout = async () => {
    await authService.logout();
    setOwner(null);
    setIsAuthenticated(false);
  };

  const refreshProfile = async () => {
    try {
      const profile = await authService.getProfile();
      setOwner(profile);
    } catch {
      // Ignored
    }
  };

  return (
    <AuthContext.Provider
      value={{
        isAuthenticated,
        isLoading,
        owner,
        login,
        register,
        logout,
        refreshProfile,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = (): AuthContextType => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
