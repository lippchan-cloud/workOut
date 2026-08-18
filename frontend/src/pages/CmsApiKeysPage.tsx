import { FormEvent, useEffect, useState } from "react";
import { apiGet, apiPut } from "../api/client";

type ApiKeyRow = {
  userId: number;
  username: string;
  apiKeyId: number | null;
  keyMask: string | null;
  hasKey: boolean;
};

/**
 * CMS：单用户 / 批量改 DeepSeek API Key；只展示掩码。
 */
export function CmsApiKeysPage() {
  const [rows, setRows] = useState<ApiKeyRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [userId, setUserId] = useState("");
  const [apiKey, setApiKey] = useState("");
  const [batchIds, setBatchIds] = useState("");
  const [batchKey, setBatchKey] = useState("");
  const [message, setMessage] = useState("");

  const reload = () => {
    setLoading(true);
    apiGet<{ list: ApiKeyRow[] }>("/api/v1/admin/apiKeys")
      .then((data) => setRows(data.list ?? []))
      .catch((err) => setError(err instanceof Error ? err.message : "加载失败"))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    reload();
  }, []);

  const onSingle = async (event: FormEvent) => {
    event.preventDefault();
    setMessage("");
    setError("");
    try {
      await apiPut(`/api/v1/admin/apiKeys/${userId}`, { apiKey });
      setMessage("已更新该用户 API Key（掩码展示）");
      setApiKey("");
      reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "更新失败");
    }
  };

  const onBatch = async (event: FormEvent) => {
    event.preventDefault();
    setMessage("");
    setError("");
    const userIds = batchIds
      .split(/[,，\s]+/)
      .map((s) => s.trim())
      .filter(Boolean)
      .map((s) => Number(s));
    try {
      await apiPut("/api/v1/admin/apiKeys/batch", { userIds, apiKey: batchKey });
      setMessage("已批量更新 API Key");
      setBatchKey("");
      reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "批量更新失败");
    }
  };

  return (
    <section className="stack">
      <h2>API Key</h2>
      <p className="page__subtitle">为用户绑定 DeepSeek Key；列表仅显示掩码。</p>
      {loading ? <p className="empty-state">加载中…</p> : null}
      {error ? <p role="alert">{error}</p> : null}
      {message ? <p>{message}</p> : null}
      <form className="card stack" onSubmit={onSingle}>
        <h3>单用户改 Key</h3>
        <label>
          用户 ID
          <input value={userId} onChange={(e) => setUserId(e.target.value)} required />
        </label>
        <label>
          API Key
          <input
            type="password"
            value={apiKey}
            onChange={(e) => setApiKey(e.target.value)}
            required
            autoComplete="off"
          />
        </label>
        <button type="submit" className="btn btn-secondary">
          保存
        </button>
      </form>
      <form className="card stack" onSubmit={onBatch}>
        <h3>批量改 Key</h3>
        <label>
          用户 ID 列表（逗号分隔）
          <input value={batchIds} onChange={(e) => setBatchIds(e.target.value)} required />
        </label>
        <label>
          API Key
          <input
            type="password"
            value={batchKey}
            onChange={(e) => setBatchKey(e.target.value)}
            required
            autoComplete="off"
          />
        </label>
        <button type="submit" className="btn btn-secondary">
          批量保存
        </button>
      </form>
      <div className="cms-table-wrap">
        <table className="cms-table">
          <thead>
            <tr>
              <th>用户名</th>
              <th>用户ID</th>
              <th>apiKeyId</th>
              <th>掩码</th>
              <th>已绑定</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => (
              <tr key={row.userId}>
                <td>{row.username}</td>
                <td>{row.userId}</td>
                <td>{row.apiKeyId ?? "—"}</td>
                <td>{row.keyMask ?? "—"}</td>
                <td>{row.hasKey ? "是" : "否"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
