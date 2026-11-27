import axios, { AxiosError, InternalAxiosRequestConfig } from "axios";
import type { AuthResponse } from "@/types/api";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "/api/v1";

const ACCESS_KEY = "fw_access_token";
const REFRESH_KEY = "fw_refresh_token";
const USER_KEY = "fw_user";

export const tokenStorage = {
  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_KEY);
  },
  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_KEY);
  },
  setSession(auth: AuthResponse): void {
    localStorage.setItem(ACCESS_KEY, auth.accessToken);
    localStorage.setItem(REFRESH_KEY, auth.refreshToken);
    localStorage.setItem(USER_KEY, JSON.stringify(auth.user));
  },
  clearSession(): void {
    localStorage.removeItem(ACCESS_KEY);
    localStorage.removeItem(REFRESH_KEY);
    localStorage.removeItem(USER_KEY);
  },
  getCachedUser<T>(): T | null {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw) as T;
    } catch {
      return null;
    }
  },
};

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  timeout: 15000,
});

let refreshPromise: Promise<string | null> | null = null;

async function performRefresh(): Promise<string | null> {
  const refreshToken = tokenStorage.getRefreshToken();
  if (!refreshToken) {
    tokenStorage.clearSession();
    return null;
  }
  try {
    const res = await axios.post<AuthResponse>(`${API_BASE_URL}/auth/refresh`, { refreshToken });
    tokenStorage.setSession(res.data);
    return res.data.accessToken;
  } catch {
    tokenStorage.clearSession();
    return null;
  }
}

apiClient.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = tokenStorage.getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as (InternalAxiosRequestConfig & { _retry?: boolean }) | undefined;
    const isAuthRoute = original?.url?.includes("/auth/login") || original?.url?.includes("/auth/refresh");

    if (error.response?.status === 401 && original && !original._retry && !isAuthRoute) {
      original._retry = true;
      if (!refreshPromise) {
        refreshPromise = performRefresh().finally(() => {
          refreshPromise = null;
        });
      }
      const nextToken = await refreshPromise;
      if (nextToken) {
        original.headers.Authorization = `Bearer ${nextToken}`;
        return apiClient(original);
      }
    }
    return Promise.reject(error);
  },
);
