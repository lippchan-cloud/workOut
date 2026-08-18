import { FormEvent, useEffect, useState } from "react";
import { apiGet } from "../api/client";

type AiCallRow = {
  id: number;
  userId: number;
  apiKeyId: number | null;
  keyMask: string | null;
  purpose: string;
  status: string;
  shareToken: string | null;
  createdAt: string;
};

/**
 * CMS：AI 调用情况，可按 userId / apiKeyId 筛选。
 */
export function CmsAiCallsPage() {
  const [rows, setRows] = useState<AiCallRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [userId, setUserId] = useState("");
  const [apiKeyId, setApiKeyId] = useState("");

  const load = (uid?: string, kid?: string) => {
    setLoading(true);
    setError("");
    const params = new URLSearchParams();
    if (uid) params.set("userId", uid);
    if (kid) params.set("apiKeyId", kid);
    const q = params.toString();
    apiGet<{ list: AiCallRow[] }>(`/api/v1/admin/aiCalls${q ? `?${q}` : ""}`)
      .then((data) => setRows(data.list ?? []))
      .catch((err) => setError(err instanceof Error ? err.message : "加载失败"))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, []);

  const onFilter = (event: FormEvent) => {
    event.preventDefault();
    load(userId.trim(), apiKeyId.trim());
  };

  return (
    <section className="stack">
      <h2>AI 调用</h2>
      <form className="card stack" onSubmit={onFilter}>
        <label>
          userId
          <input value={userId} onChange={(e) => setUserId(e.target.value)} />
        </label>
        <label>
          apiKeyId
          <input value={apiKeyId} onChange={(e) => setApiKeyId(e.target.value)} />
        </label>
        <button type="submit" className="btn btn-secondary">
          筛选
        </button>
      </form>
      {loading ? <p className="empty-state">加载中…</p> : null}
      {error ? <p role="alert">{error}</p> : null}
      {!loading && !error && rows.length === 0 ? <p className="empty-state">暂无调用记录</p> : null}
      <div className="cms-table-wrap">
        <table className="cms-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>userId</th>
              <th>apiKeyId</th>
              <th>掩码</th>
              <th>用途</th>
              <th>状态</th>
              <th>时间</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.id}>
                <td>{row.id}</td>
                <td>{row.userId}</td>
                <td>{row.apiKeyId ?? "—"}</td>
                <td>{row.keyMask ?? "—"}</td>
                <td>{row.purpose}</td>
                <td>{row.status}</td>
                <td>{row.createdAt}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
