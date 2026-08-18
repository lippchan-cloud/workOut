import { NavLink, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import type { MouseEvent, ReactNode } from "react";

const TABS = [
  { path: "/", label: "记录" },
  { path: "/calendar", label: "日历" },
  { path: "/profile", label: "我的" },
] as const;

/**
 * 底部三 Tab 壳：未登录点击跳转登录并带 redirect。
 */
export function AppShell({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const onTabClick = (path: string) => (event: MouseEvent) => {
    if (!isAuthenticated) {
      event.preventDefault();
      navigate(`/login?redirect=${path}`);
    }
  };

  return (
    <div className="app-shell">
      <header className="app-shell__brand">
        <div className="app-shell__logo">
          <span className="app-shell__logo-mark" aria-hidden />
          workOut
        </div>
        <span className="app-shell__tag">Train Log</span>
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
