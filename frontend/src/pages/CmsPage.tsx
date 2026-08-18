import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiGet } from "../api/client";

type AdminAccount = {
  userId: number;
  username: string;
  createdAt: string;
  role?: string;
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
 * CMS 账户列表。用户名链到详情；不含密码。
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
    <>
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
                <td>
                  <Link to={`/cms/users/${account.userId}`}>{account.username}</Link>
                </td>
                <td>{display(account.createdAt)}</td>
                <td>{display(account.nickname)}</td>
                <td>{display(account.heightCm)}</td>
                <td>{display(account.weightKg)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}
