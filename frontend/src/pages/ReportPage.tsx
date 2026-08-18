import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { GrowthCurve, BodyPoint } from "./GrowthCurve";
import { formatShanghaiMdHm } from "../calendar/week";

type ReportRecord = {
  recordedAt: string;
  type: "CONSUME" | "INTAKE";
  content: string;
};

type AdviceStatus = "NONE_KEY" | "PENDING" | "READY" | "FAILED" | string;

type ReportPayload = {
  from: string;
  to: string;
  displayName: string;
  records: ReportRecord[];
  bodyHistory: BodyPoint[];
  advice: string | null;
  adviceStatus?: AdviceStatus | null;
};

/**
 * 根据建议状态选择展示文案。
 */
function adviceDisplay(data: ReportPayload): string {
  const status = data.adviceStatus ?? (data.advice ? "READY" : "NONE_KEY");
  if (status === "PENDING") {
    return "生成中";
  }
  if (status === "NONE_KEY") {
    return data.advice?.trim() || "未配置 API Key";
  }
  if (status === "FAILED") {
    return data.advice?.trim() || "建议生成失败，请稍后重试（仅供参考）";
  }
  if (status === "READY" && data.advice?.trim()) {
    return data.advice;
  }
  return data.advice?.trim() || "建议分析（即将提供）";
}

/**
 * 公开报告页：不走三 Tab 壳。建议分析由异步 AI 填充。
 */
export function ReportPage() {
  const { id } = useParams();
  const [data, setData] = useState<ReportPayload | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!id) {
      setError("报告不存在");
      return;
    }
    let cancelled = false;
    let timer: number | undefined;
    const load = () => {
      fetch(`/api/v1/reports/${id}`)
        .then(async (response) => {
          const body = await response.json();
          if (!response.ok || body.code !== 200) {
            throw new Error(body.msg || "报告不存在");
          }
          if (cancelled) {
            return;
          }
          setData(body.data);
          if (body.data?.adviceStatus === "PENDING") {
            timer = window.setTimeout(load, 3000);
          }
        })
        .catch((err) => {
          if (!cancelled) {
            setError(err instanceof Error ? err.message : "报告不存在");
          }
        });
    };
    load();
    return () => {
      cancelled = true;
      if (timer !== undefined) {
        window.clearTimeout(timer);
      }
    };
  }, [id]);

  return (
    <div className="page report-page">
      <p className="page__eyebrow">Report</p>
      <h1 className="page__title">训练报告</h1>
      <p>
        <Link to="/" className="btn btn-text">
          回首页
        </Link>
      </p>
      {error ? <p className="empty-state">{error}</p> : null}
      {data ? (
        <div className="stack">
          <p className="page__subtitle">
            数据范围 {data.from} ～ {data.to}
          </p>
          <section className="card stack">
            <h2 className="page__title" style={{ fontSize: "1.15rem" }}>
              用户名称
            </h2>
            <p>{data.displayName || "—"}</p>
          </section>
          <section className="card stack">
            <h2 className="page__title" style={{ fontSize: "1.15rem" }}>
              事项列表
            </h2>
            {data.records.length === 0 ? (
              <p className="empty-state">这段时间还没有记录</p>
            ) : (
              <ul className="record-list record-list--stacked">
                {data.records.map((item, index) => (
                  <li
                    key={`${item.recordedAt}-${index}`}
                    className={item.type === "CONSUME" ? "record-consume" : "record-intake"}
                    style={{ color: item.type === "CONSUME" ? "#16A34A" : "#DC2626" }}
                  >
                    <span className="record-list__time">{formatShanghaiMdHm(item.recordedAt)}</span>
                    <span className="record-list__content">{item.content}</span>
                  </li>
                ))}
              </ul>
            )}
          </section>
          <section className="stack">
            <h2 className="page__title" style={{ fontSize: "1.15rem" }}>
              成长曲线
            </h2>
            {data.bodyHistory.length === 0 ? (
              <p className="empty-state">还没有身体变化数据</p>
            ) : (
              <GrowthCurve bodyHistory={data.bodyHistory} />
            )}
          </section>
          <section className="card stack">
            <h2 className="page__title" style={{ fontSize: "1.15rem" }}>
              建议分析
            </h2>
            <p className="empty-state" data-testid="advice-text">
              {adviceDisplay(data)}
            </p>
          </section>
        </div>
      ) : null}
    </div>
  );
}
