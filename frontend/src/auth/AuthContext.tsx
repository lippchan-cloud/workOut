import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { apiGet } from "../api/client";

const TOKEN_KEY = "workout_token";
const ROLE_KEY = "workout_role";
const USERNAME_KEY = "workout_username";

type AuthContextValue = {
  token: string | null;
  role: string | null;
  username: string | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  setToken: (token: string | null) => void;
  setSession: (token: string, role?: string | null, username?: string | null) => void;
  clearToken: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

function persistSession(nextToken: string, nextRole?: string | null, nextUsername?: string | null) {
  localStorage.setItem(TOKEN_KEY, nextToken);
  if (nextRole) {
    localStorage.setItem(ROLE_KEY, nextRole);
  } else {
    localStorage.removeItem(ROLE_KEY);
  }
  if (nextUsername) {
    localStorage.setItem(USERNAME_KEY, nextUsername);
  } else {
    localStorage.removeItem(USERNAME_KEY);
  }
}

function clearStorage() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(ROLE_KEY);
  localStorage.removeItem(USERNAME_KEY);
}

/**
 * 提供 JWT 本地会话状态（localStorage），含用户名与最小角色。
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setTokenState] = useState<string | null>(() => localStorage.getItem(TOKEN_KEY));
  const [role, setRoleState] = useState<string | null>(() => localStorage.getItem(ROLE_KEY));
  const [username, setUsernameState] = useState<string | null>(() => localStorage.getItem(USERNAME_KEY));

  useEffect(() => {
    if (!token || username) {
      return;
    }
    apiGet<{ username?: string; role?: string }>("/api/v1/auth/me")
      .then((data) => {
        if (data.username) {
          localStorage.setItem(USERNAME_KEY, data.username);
          setUsernameState(data.username);
        }
        if (data.role) {
          localStorage.setItem(ROLE_KEY, data.role);
          setRoleState(data.role);
        }
      })
      .catch(() => {
        /* 401 由 client 跳转登录 */
      });
  }, [token, username]);

  const value = useMemo<AuthContextValue>(
    () => ({
      token,
      role,
      username,
      isAuthenticated: Boolean(token),
      isAdmin: role === "ADMIN",
      setToken: (next) => {
        if (next) {
          localStorage.setItem(TOKEN_KEY, next);
        } else {
          clearStorage();
          setRoleState(null);
          setUsernameState(null);
        }
        setTokenState(next);
      },
      setSession: (nextToken, nextRole, nextUsername) => {
        persistSession(nextToken, nextRole, nextUsername);
        setTokenState(nextToken);
        setRoleState(nextRole ?? null);
        setUsernameState(nextUsername ?? localStorage.getItem(USERNAME_KEY));
      },
      clearToken: () => {
        clearStorage();
        setTokenState(null);
        setRoleState(null);
        setUsernameState(null);
      },
    }),
    [token, role, username],
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
