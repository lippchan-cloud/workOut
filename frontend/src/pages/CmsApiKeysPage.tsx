import { FormEvent, useEffect, useState } from "react";
import { apiGet, apiPost, apiPut } from "../api/client";

type ApiKeyRow = {
  userId: number;
  username: string;
  apiKeyId: number | null;
  keyMask: string | null;
  hasKey: boolean;
};

type PoolRow = {
  id: number;
  keyMask: string;
  enabled: boolean;
};

/**
 * CMS：密钥库列表/新增 + 单用户/批量改 DeepSeek API Key；只展示掩码。
 */
export function CmsApiKeysPage() {
  const [rows, setRows] = useState<ApiKeyRow[]>([]);
  const [poolRows, setPoolRows] = useState<PoolRow[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [userId, setUserId] = useState("");
  const [apiKey, setApiKey] = useState("");
  const [batchIds, setBatchIds] = useState("");
  const [batchKey, setBatchKey] = useState("");
  const [poolKey, setPoolKey] = useState("");
  const [message, setMessage] = useState("");

  const reload = () => {
    setLoading(true);
    Promise.all([
      apiGet<{ list: ApiKeyRow[] }>("/api/v1/admin/apiKeys"),
      apiGet<{ list: PoolRow[] }>("/api/v1/admin/apiKeys/pool"),
    ])
      .then(([userData, poolData]) => {
        setRows(userData.list ?? []);
        setPoolRows(poolData.list ?? []);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "加载失败"))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    reload();
  }, []);

  const onPoolCreate = async (event: FormEvent) => {
    event.preventDefault();
    setMessage("");
    setError("");
    try {
      await apiPost("/api/v1/admin/apiKeys/pool", { apiKey: poolKey });
      setMessage("已加入密钥库（仅掩码展示）");
      setPoolKey("");
      reload();
    } catch (err) {
      setError(err instanceof Error ? err.message : "密钥库新增失败");
    }
  };

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
      <p className="page__subtitle">密钥库存独立表；新注册用户默认分配；列表仅显示掩码。</p>
      {loading ? <p className="empty-state">加载中…</p> : null}
      {error ? <p role="alert">{error}</p> : null}
      {message ? <p>{message}</p> : null}

      <form className="card stack" onSubmit={onPoolCreate}>
        <h3>密钥库</h3>
        <label>
          新增 API Key
          <input
            type="password"
            value={poolKey}
            onChange={(e) => setPoolKey(e.target.value)}
            required
            autoComplete="off"
          />
        </label>
        <button type="submit" className="btn btn-secondary">
          加入密钥库
        </button>
      </form>
      <div className="cms-table-wrap">
        <table className="cms-table" aria-label="密钥库">
          <thead>
            <tr>
              <th>poolId</th>
              <th>掩码</th>
              <th>启用</th>
            </tr>
          </thead>
          <tbody>
            {poolRows.map((row) => (
              <tr key={row.id}>
                <td>{row.id}</td>
                <td>{row.keyMask}</td>
                <td>{row.enabled ? "是" : "否"}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

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
