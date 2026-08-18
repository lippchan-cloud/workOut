import { useEffect, useState } from "react";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { apiDelete, apiGet } from "../api/client";
import { formatShanghaiYmd } from "../calendar/week";

type RecordItem = {
  id: number;
  type: "CONSUME" | "INTAKE";
  content: string;
  recordedAt: string;
};

/**
 * 事项只读详情：刷新 URL 走 GET by id；编辑/删除复用二期接口。
 */
export function RecordDetailPage() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const [item, setItem] = useState<RecordItem | null>(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate(`/login?redirect=/calendar/records/${id ?? ""}`);
      return;
    }
    if (!id) {
      setError("记录不存在");
      setLoading(false);
      return;
    }
    let cancelled = false;
    setLoading(true);
    apiGet<RecordItem>(`/api/v1/dailyRecords/${id}`)
      .then((data) => {
        if (cancelled) {
          return;
        }
        setItem(data);
        setError("");
        setLoading(false);
      })
      .catch((err: Error) => {
        if (cancelled) {
          return;
        }
        setItem(null);
        setError(err.message || "记录不存在");
        setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, [id, isAuthenticated, navigate]);

  const backToCalendar = (ymd?: string) => {
    const date = ymd || searchParams.get("date");
    navigate(date ? `/calendar?date=${date}` : "/calendar");
  };

  const onEdit = () => {
    if (!item) {
      return;
    }
    const path = item.type === "CONSUME" ? "/record/consume" : "/record/intake";
    navigate(`${path}?edit=${item.id}`, { state: item });
  };

  const onDelete = async () => {
    if (!item) {
      return;
    }
    if (!window.confirm("确认删除这条记录？")) {
      return;
    }
    await apiDelete(`/api/v1/dailyRecords/${item.id}`);
    backToCalendar(formatShanghaiYmd(item.recordedAt));
  };

  const isConsume = item?.type === "CONSUME";

  return (
    <div className="page">
      <button type="button" className="btn btn-ghost page-back" onClick={() => backToCalendar(item ? formatShanghaiYmd(item.recordedAt) : undefined)}>
        返回
      </button>
      <p className="page__eyebrow">Detail</p>
      <h1 className="page__title">事项详情</h1>
      {loading ? <p className="empty-state">加载中…</p> : null}
      {!loading && error ? (
        <div className="empty-state">
          <p>{error}</p>
          <button type="button" className="btn btn-ghost" onClick={() => backToCalendar()}>
            回日历
          </button>
        </div>
      ) : null}
      {!loading && item ? (
        <section className={`card stack ${isConsume ? "card--consume" : "card--intake"}`}>
          <span className={`card__badge ${isConsume ? "card__badge--consume" : "card__badge--intake"}`}>
            {isConsume ? "消耗" : "摄入"}
          </span>
          <p className="detail-content" style={{ color: isConsume ? "#16A34A" : "#DC2626" }}>
            {item.content}
          </p>
          <p className="page__subtitle">{new Date(item.recordedAt).toLocaleString("zh-CN", { timeZone: "Asia/Shanghai" })}</p>
          <div className="row">
            <button type="button" className="btn btn-primary" onClick={onEdit}>
              编辑
            </button>
            <button type="button" className="btn btn-ghost" onClick={onDelete}>
              删除
            </button>
          </div>
        </section>
      ) : null}
    </div>
  );
}
