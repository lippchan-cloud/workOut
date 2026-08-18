import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { apiGet } from "../api/client";

type RecentRecord = {
  id: number;
  type: string;
  content: string;
  recordedAt: string;
};

type ShareItem = {
  id: string;
  from: string;
  to: string;
  createdAt: string;
};

type UserDetail = {
  userId: number;
  username: string;
  createdAt: string;
  role: string;
  nickname: string | null;
  heightCm: number | null;
  weightKg: number | null;
  recordCount: number;
  recentRecords: RecentRecord[];
  shares: ShareItem[];
};

function display(value: string | number | null | undefined): string {
  if (value === null || value === undefined || value === "") {
    return "—";
  }
  return String(value);
}

/**
 * CMS 用户详情空态：提示从账户列表选择。
 */
export function CmsUserPickPage() {
  return (
    <section className="stack">
      <h2>用户详情</h2>
      <p className="empty-state">
        请从账户列表选择用户。 <Link to="/cms/accounts">去账户列表</Link>
      </p>
    </section>
  );
}

/**
 * CMS 用户详情：资料、最近记录与已有分享链接；不代用户生成报告。
 */
export function CmsUserDetailPage() {
  const { userId } = useParams();
  const [detail, setDetail] = useState<UserDetail | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!userId) {
      setLoading(false);
      return;
    }
    apiGet<UserDetail>(`/api/v1/admin/accounts/${userId}`)
      .then((data) => setDetail(data))
      .catch((err) => setError(err instanceof Error ? err.message : "加载失败"))
      .finally(() => setLoading(false));
  }, [userId]);

  if (loading) {
    return <p className="empty-state">加载中…</p>;
  }
  if (error) {
    return <p role="alert">{error}</p>;
  }
  if (!detail) {
    return <p className="empty-state">用户不存在</p>;
  }

  return (
    <section className="stack">
      <h2>用户详情</h2>
      <dl className="cms-dl">
        <dt>用户名</dt>
        <dd>{detail.username}</dd>
        <dt>角色</dt>
        <dd>{detail.role}</dd>
        <dt>创建时间</dt>
        <dd>{display(detail.createdAt)}</dd>
        <dt>昵称</dt>
        <dd>{display(detail.nickname)}</dd>
        <dt>身高</dt>
        <dd>{display(detail.heightCm)}</dd>
        <dt>体重</dt>
        <dd>{display(detail.weightKg)}</dd>
      </dl>
      <h3>最近记录（{detail.recordCount}）</h3>
      {detail.recentRecords.length === 0 ? (
        <p className="empty-state">暂无记录</p>
      ) : (
        <ul className="record-list record-list--stacked">
          {detail.recentRecords.map((row) => (
            <li key={row.id} className={row.type === "CONSUME" ? "record-consume" : "record-intake"}>
              <span>{row.recordedAt}</span>
              <span>{row.content}</span>
            </li>
          ))}
        </ul>
      )}
      <h3>已有分享</h3>
      {detail.shares.length === 0 ? (
        <p className="empty-state">该用户暂无分享报告</p>
      ) : (
        <ul className="stack">
          {detail.shares.map((share) => (
            <li key={share.id}>
              {share.from} ~ {share.to}{" "}
              <a href={`/report/${share.id}`} target="_blank" rel="noreferrer">
                查看报告
              </a>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
