import { Link, NavLink, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import type { MouseEvent, ReactNode } from "react";

const TABS = [
  { path: "/", label: "记录" },
  { path: "/calendar", label: "日历" },
  { path: "/profile", label: "我的" },
] as const;

/**
 * 底部三 Tab 壳：未登录点击跳转登录并带 redirect；顶栏展示账号或登录入口。
 */
export function AppShell({ children }: { children: ReactNode }) {
  const { isAuthenticated, username } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const onTabClick = (path: string) => (event: MouseEvent) => {
    if (!isAuthenticated) {
      event.preventDefault();
      navigate(`/login?redirect=${path}`);
    }
  };

  const loginHref = `/login?redirect=${location.pathname || "/"}`;

  return (
    <div className="app-shell">
      <header className="app-shell__brand">
        <div className="app-shell__logo">
          <span className="app-shell__logo-mark" aria-hidden />
          workOut
        </div>
        {isAuthenticated ? (
          <Link to="/profile" className="app-shell__account" title={username || "账号"}>
            {username || "账号"}
          </Link>
        ) : (
          <Link to={loginHref} className="app-shell__account app-shell__account--login">
            登录
          </Link>
        )}
      </header>
      <main className="app-shell__content">{children}</main>
      <nav className="app-shell__nav" aria-label="主导航">
        {TABS.map((tab) => (
          <NavLink
            key={tab.path}
            to={tab.path}
            end={tab.path === "/"}
            className="app-shell__tab"
            onClick={onTabClick(tab.path)}
          >
            {({ isActive }) => {
              const active =
                tab.path === "/"
                  ? location.pathname === "/" || location.pathname.startsWith("/record")
                  : tab.path === "/calendar"
                    ? location.pathname.startsWith("/calendar")
                    : tab.path === "/profile"
                      ? location.pathname.startsWith("/profile")
                      : isActive;
              return (
                <button type="button" className={active ? "active" : undefined}>
                  {tab.label}
                </button>
              );
            }}
          </NavLink>
        ))}
      </nav>
    </div>
  );
}
