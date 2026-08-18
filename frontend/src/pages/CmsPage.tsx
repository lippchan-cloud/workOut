import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiGet } from "../api/client";

type AdminAccount = {
  userId: number;
  username: string;
  createdAt: string;
  nickname: string | null;
  heightCm: number | null;
  weightKg: number | null;
};

function display(value: string | number | null | undefined): string {
  if (value === null || value === undefined || value === "") {
    return "—";
  }
  return String(value);
}

/**
 * 后台 CMS 账户列表页。
 * TEMPORARY：第一阶段无需登录即可访问，后续将加鉴权；不展示密码哈希。
 */
export function CmsPage() {
  const [accounts, setAccounts] = useState<AdminAccount[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    apiGet<{ list: AdminAccount[] }>("/api/v1/admin/accounts")
      .then((data) => setAccounts(data.list ?? []))
      .catch((err) => setError(err instanceof Error ? err.message : "加载失败"))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="cms-page">
      <header className="cms-page__header">
        <div>
          <p className="page__eyebrow">CMS</p>
          <h1>后台管理</h1>
          <p className="page__subtitle">查看全部注册账户（不含密码）。</p>
        </div>
        <Link to="/login">返回登录</Link>
      </header>
      <p role="status" className="cms-page__banner">
        临时开放：第一阶段无需登录即可访问，后续将加鉴权。请勿对公网暴露本页。
      </p>
      {loading ? <p className="empty-state">加载中…</p> : null}
      {!loading && error ? <p role="alert">{error}</p> : null}
      {!loading && !error && accounts.length === 0 ? <p className="empty-state">暂无账户</p> : null}
      <div className="cms-table-wrap">
        <table className="cms-table">
          <thead>
            <tr>
              <th>用户ID</th>
              <th>用户名</th>
              <th>创建时间</th>
              <th>昵称</th>
              <th>身高</th>
              <th>体重</th>
            </tr>
          </thead>
          <tbody>
            {accounts.map((account) => (
              <tr key={account.userId}>
                <td>{account.userId}</td>
                <td>{account.username}</td>
                <td>{display(account.createdAt)}</td>
                <td>{display(account.nickname)}</td>
                <td>{display(account.heightCm)}</td>
                <td>{display(account.weightKg)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
