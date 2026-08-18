import { createContext, useContext, useMemo, useState, type ReactNode } from "react";

const TOKEN_KEY = "workout_token";

type AuthContextValue = {
  token: string | null;
  isAuthenticated: boolean;
  setToken: (token: string | null) => void;
  clearToken: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

/**
 * 提供 JWT 本地会话状态（localStorage）。
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY));

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      isAuthenticated: Boolean(token),
      setToken: (next) => {
        if (next) {
          localStorage.setItem(TOKEN_KEY, next);
        } else {
          localStorage.removeItem(TOKEN_KEY);
        }
        setTokenState(next);
      },
      clearToken: () => {
        localStorage.removeItem(TOKEN_KEY);
        setTokenState(null);
      },
    }),
    [token],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

/**
 * 读取鉴权上下文；必须在 AuthProvider 内使用。
 */
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return ctx;
}
