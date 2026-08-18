import { createContext, useContext, useMemo, useState, type ReactNode } from "react";

const TOKEN_KEY = "workout_token";
const ROLE_KEY = "workout_role";

type AuthContextValue = {
  token: string | null;
  role: string | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  setToken: (token: string | null) => void;
  setSession: (token: string, role?: string | null) => void;
  clearToken: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

/**
 * 提供 JWT 本地会话状态（localStorage），含最小角色。
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY));
  const [role, setRoleState] = useState<string | null>(() => localStorage.getItem(ROLE_KEY));

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      role,
      isAuthenticated: Boolean(token),
      isAdmin: role === "ADMIN",
      setToken: (next) => {
        if (next) {
          localStorage.setItem(TOKEN_KEY, next);
        } else {
          localStorage.removeItem(TOKEN_KEY);
          localStorage.removeItem(ROLE_KEY);
          setRoleState(null);
        }
        setTokenState(next);
      },
      setSession: (nextToken, nextRole) => {
        localStorage.setItem(TOKEN_KEY, nextToken);
        if (nextRole) {
          localStorage.setItem(ROLE_KEY, nextRole);
          setRoleState(nextRole);
        } else {
          localStorage.removeItem(ROLE_KEY);
          setRoleState(null);
        }
        setTokenState(nextToken);
      },
      clearToken: () => {
        localStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(ROLE_KEY);
        setTokenState(null);
        setRoleState(null);
      },
    }),
    [token, role],
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
