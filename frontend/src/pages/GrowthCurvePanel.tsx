import { useEffect, useState } from "react";
import { apiGet } from "../api/client";
import { BodyPoint, GrowthCurve, RecordCount } from "./GrowthCurve";

/**
 * 公开/资料页共用的曲线加载：空态中文，不画假线。
 */
export function GrowthCurvePanel({ emptyText = "还没有身体变化数据" }: { emptyText?: string }) {
  const [bodyHistory, setBodyHistory] = useState<BodyPoint[]>([]);
  const [recordCounts, setRecordCounts] = useState<RecordCount[]>([]);
  const [loadStatus, setLoadStatus] = useState<"loading" | "success" | "error">("loading");

  useEffect(() => {
    let cancelled = false;
    setLoadStatus("loading");
    apiGet<{ bodyHistory: BodyPoint[]; recordCounts: RecordCount[] }>("/api/v1/profile/trends")
      .then((data) => {
        if (cancelled) {
          return;
        }
        setBodyHistory(data.bodyHistory ?? []);
        setRecordCounts(data.recordCounts ?? []);
        setLoadStatus("success");
      })
      .catch(() => {
        if (cancelled) {
          return;
        }
        setBodyHistory([]);
        setRecordCounts([]);
        setLoadStatus("error");
      });
    return () => {
      cancelled = true;
    };
  }, []);

  const empty = loadStatus === "success" && bodyHistory.length === 0 && recordCounts.length === 0;

  return (
    <section className="stack">
      <h2 className="page__title" style={{ fontSize: "1.15rem" }}>
        成长曲线
      </h2>
      {loadStatus === "loading" ? <p className="empty-state">加载中…</p> : null}
      {loadStatus === "error" ? <p className="empty-state">曲线加载失败</p> : null}
      {empty ? <p className="empty-state">{emptyText}</p> : null}
      {loadStatus === "success" && !empty ? <GrowthCurve bodyHistory={bodyHistory} /> : null}
    </section>
  );
}
