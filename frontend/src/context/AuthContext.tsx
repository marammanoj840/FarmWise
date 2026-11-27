import { createContext, useCallback, useContext, useEffect, useState, type ReactNode } from "react";
import { authApi } from "@/api/services";
import { tokenStorage } from "@/api/client";
import type { AuthResponse, UserRole, UserSummary } from "@/types/api";

interface AuthContextValue {
  user: UserSummary | null;
  isLoading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<AuthResponse>;
  register: (payload: Record<string, unknown>) => Promise<AuthResponse>;
  logout: () => Promise<void>;
  refreshUser: () => Promise<void>;
  hasRole: (...roles: UserRole[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function getHomePathForRole(role?: string): string {
  switch (role?.toUpperCase()) {
    case "ADMIN":
      return "/app/admin";
    case "EXPERT":
      return "/app/expert";
    case "SUPPLIER":
      return "/app/supplier";
    case "BUYER":
      return "/app/buyer";
    case "FARMER":
    default:
      return "/app/farmer";
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserSummary | null>(() => tokenStorage.getCachedUser<UserSummary>());
  const [isLoading, setIsLoading] = useState<boolean>(() => Boolean(tokenStorage.getAccessToken()));

  const refreshUser = useCallback(async () => {
    if (!tokenStorage.getAccessToken()) {
      setUser(null);
      setIsLoading(false);
      return;
    }
    try {
      const me = await authApi.me();
      setUser(me);
      localStorage.setItem("fw_user", JSON.stringify(me));
    } catch {
      tokenStorage.clearSession();
      setUser(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    let active = true;
    const bootstrap = async () => {
      if (!tokenStorage.getAccessToken()) {
        if (active) {
          setUser(null);
          setIsLoading(false);
        }
        return;
      }
      try {
        const me = await authApi.me();
        if (active) {
          setUser(me);
          localStorage.setItem("fw_user", JSON.stringify(me));
        }
      } catch {
        tokenStorage.clearSession();
        if (active) setUser(null);
      } finally {
        if (active) setIsLoading(false);
      }
    };
    void bootstrap();
    return () => {
      active = false;
    };
  }, []);

  const login = async (email: string, password: string) => {
    const res = await authApi.login({ email, password });
    tokenStorage.setSession(res);
    setUser(res.user);
    return res;
  };

  const register = async (payload: Record<string, unknown>) => {
    const res = await authApi.register(payload);
    tokenStorage.setSession(res);
    setUser(res.user);
    return res;
  };

  const logout = async () => {
    const refresh = tokenStorage.getRefreshToken();
    try {
      await authApi.logout(refresh);
    } catch {
      // ignore network failures during logout
    } finally {
      tokenStorage.clearSession();
      setUser(null);
    }
  };

  const hasRole = (...roles: UserRole[]) => {
    if (!user) return false;
    return roles.some((r) => user.primaryRole === r || user.roles?.includes(r));
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: Boolean(user),
        login,
        register,
        logout,
        refreshUser,
        hasRole,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside AuthProvider");
  return ctx;
}
