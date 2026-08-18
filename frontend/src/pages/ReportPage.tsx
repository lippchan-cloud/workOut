import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { GrowthCurve, BodyPoint } from "./GrowthCurve";
import { formatShanghaiMdHm } from "../calendar/week";

type ReportRecord = {
  recordedAt: string;
  type: "CONSUME" | "INTAKE";
  content: string;
};

type ReportPayload = {
  from: string;
  to: string;
  displayName: string;
  records: ReportRecord[];
  bodyHistory: BodyPoint[];
  advice: string | null;
};

/**
 * 公开报告页：不走三 Tab 壳。上下为用户名称、事项、曲线、建议分析占位。
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
    fetch(`/api/v1/reports/${id}`)
      .then(async (response) => {
        const body = await response.json();
        if (!response.ok || body.code !== 200) {
          throw new Error(body.msg || "报告不存在");
        }
        setData(body.data);
      })
      .catch((err) => setError(err instanceof Error ? err.message : "报告不存在"));
  }, [id]);

  return (
    <div className="page report-page">
      <p className="page__eyebrow">Report</p>
      <h1 className="page__title">训练报告</h1>
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
              <ul className="record-list">
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
            <p className="empty-state">建议分析（即将提供）</p>
          </section>
        </div>
      ) : null}
    </div>
  );
}
