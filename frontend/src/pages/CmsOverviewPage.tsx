import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiGet } from "../api/client";

/**
 * CMS 概览：复用账户与分享列表接口展示数量，不新开 API。
 */
export function CmsOverviewPage() {
  const [accountCount, setAccountCount] = useState<number | null>(null);
  const [reportCount, setReportCount] = useState<number | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    Promise.all([
      apiGet<{ list: unknown[] }>("/api/v1/admin/accounts"),
      apiGet<{ list: unknown[] }>("/api/v1/admin/reports"),
    ])
      .then(([accounts, reports]) => {
        setAccountCount(accounts.list?.length ?? 0);
        setReportCount(reports.list?.length ?? 0);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "加载失败"));
  }, []);

  return (
    <section className="stack">
      <h2>概览</h2>
      {error ? <p role="alert">{error}</p> : null}
      <p>
        已注册账户 {accountCount ?? "…"} · <Link to="/cms/accounts">账户列表</Link>
      </p>
      <p>
        已有分享 {reportCount ?? "…"} · <Link to="/cms/reports">报告</Link>
      </p>
    </section>
  );
}
