import { Link, NavLink, Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

const NAV = [
  { to: "/cms", label: "概览", end: true },
  { to: "/cms/accounts", label: "账户列表", end: true },
  { to: "/cms/users", label: "用户详情", end: false },
  { to: "/cms/reports", label: "报告", end: true },
] as const;

/**
 * CMS 壳：独立于底部三 Tab；未登录跳转登录；非管理员只显示拒绝文案。
 */
export function CmsLayout() {
  const { isAuthenticated, isAdmin } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to={`/login?redirect=${location.pathname}`} replace />;
  }

  if (!isAdmin) {
    return (
      <div className="cms-page">
        <header className="cms-page__header">
          <div>
            <p className="page__eyebrow">CMS</p>
            <h1>后台管理</h1>
          </div>
          <Link to="/">返回</Link>
        </header>
        <p role="alert">你不是管理员，无法查看全站账户。</p>
      </div>
    );
  }

  return (
    <div className="cms-page">
      <header className="cms-page__header">
        <div>
          <p className="page__eyebrow">CMS</p>
          <h1>后台管理</h1>
          <p className="page__subtitle">运营只读：账户、用户详情与已有分享报告。</p>
        </div>
        <Link to="/">返回</Link>
      </header>
      <nav className="cms-nav" aria-label="CMS功能栏">
        {NAV.map((item) => (
          <NavLink key={item.to} to={item.to} end={item.end} className="cms-nav__link">
            {item.label}
          </NavLink>
        ))}
      </nav>
      <Outlet />
    </div>
  );
}
