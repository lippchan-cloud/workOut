import { useEffect, useState } from "react";
import { apiGet } from "../api/client";

type ReportItem = {
  id: string;
  userId: number;
  username: string | null;
  from: string;
  to: string;
  createdAt: string;
};

function display(value: string | number | null | undefined): string {
  if (value === null || value === undefined || value === "") {
    return "—";
  }
  return String(value);
}

/**
 * CMS 已有分享报告列表。打开公开 /report/:id，不代用户生成。
 */
export function CmsReportsPage() {
  const [reports, setReports] = useState<ReportItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    apiGet<{ list: ReportItem[] }>("/api/v1/admin/reports")
      .then((data) => setReports(data.list ?? []))
      .catch((err) => setError(err instanceof Error ? err.message : "加载失败"))
      .finally(() => setLoading(false));
  }, []);

  return (
    <section>
      <h2>报告</h2>
      {loading ? <p className="empty-state">加载中…</p> : null}
      {!loading && error ? <p role="alert">{error}</p> : null}
      {!loading && !error && reports.length === 0 ? <p className="empty-state">暂无分享报告</p> : null}
      <div className="cms-table-wrap">
        <table className="cms-table">
          <thead>
            <tr>
              <th>用户名</th>
              <th>用户ID</th>
              <th>范围</th>
              <th>创建时间</th>
              <th>报告</th>
            </tr>
          </thead>
          <tbody>
            {reports.map((row) => (
              <tr key={row.id}>
                <td>{display(row.username)}</td>
                <td>{row.userId}</td>
                <td>
                  {row.from} ~ {row.to}
                </td>
                <td>{display(row.createdAt)}</td>
                <td>
                  <a href={`/report/${row.id}`} target="_blank" rel="noreferrer">
                    打开报告
                  </a>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
