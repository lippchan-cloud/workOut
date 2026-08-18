import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { apiGet } from "../api/client";
import { formatShanghaiMdHm } from "../calendar/week";

type BodyPoint = {
  changedAt: string;
  nickname: string | null;
  heightCm: number | null;
  weightKg: number | null;
};

type RecordCount = {
  date: string;
  count: number;
};

type SeriesKey = "heightCm" | "weightKg";

/**
 * 日历二级：身体资料随时间变化曲线。返回回到日历，不堆在月网格同屏。
 */
export function TrendsPage() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [bodyHistory, setBodyHistory] = useState<BodyPoint[]>([]);
  const [recordCounts, setRecordCounts] = useState<RecordCount[]>([]);
  const [series, setSeries] = useState<SeriesKey>("weightKg");
  const [loadStatus, setLoadStatus] = useState<"loading" | "success" | "error">("loading");

  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login?redirect=/calendar/trends");
      return;
    }
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
  }, [isAuthenticated, navigate]);

  const points = useMemo(
    () =>
      bodyHistory
        .filter((row) => row[series] != null)
        .map((row) => ({ at: row.changedAt, value: Number(row[series]) })),
    [bodyHistory, series],
  );

  const empty = loadStatus === "success" && bodyHistory.length === 0 && recordCounts.length === 0;

  return (
    <div className="page">
      <button type="button" className="btn btn-ghost page-back" onClick={() => navigate("/calendar")}>
        返回
      </button>
      <p className="page__eyebrow">Trends</p>
      <h1 className="page__title">变化曲线</h1>
      <p className="page__subtitle">身高与体重随时间</p>

      {loadStatus === "loading" ? <p className="empty-state">加载中…</p> : null}
      {loadStatus === "error" ? <p className="empty-state">曲线加载失败</p> : null}
      {empty ? <p className="empty-state">还没有身体变化数据</p> : null}

      {loadStatus === "success" && !empty ? (
        <div className="card stack">
          <div className="mode-switch" role="group" aria-label="曲线系列">
            <button type="button" className="btn btn-ghost" aria-pressed={series === "heightCm"} onClick={() => setSeries("heightCm")}>
              身高
            </button>
            <button type="button" className="btn btn-ghost" aria-pressed={series === "weightKg"} onClick={() => setSeries("weightKg")}>
              体重
            </button>
          </div>
          {points.length === 0 ? (
            <p className="empty-state">这一项还没有数据</p>
          ) : (
            <TrendSvg points={points} series={series} />
          )}
          {recordCounts.length > 0 ? (
            <p className="page__subtitle">
              共 {recordCounts.reduce((sum, row) => sum + row.count, 0)} 条记录
            </p>
          ) : null}
        </div>
      ) : null}
    </div>
  );
}

function TrendSvg({ points, series }: { points: { at: string; value: number }[]; series: SeriesKey }) {
  const width = 320;
  const height = 180;
  const pad = 28;
  const values = points.map((point) => point.value);
  const min = Math.min(...values);
  const max = Math.max(...values);
  const span = max - min || 1;
  const coords = points.map((point, index) => {
    const x = pad + (index / Math.max(points.length - 1, 1)) * (width - pad * 2);
    const y = height - pad - ((point.value - min) / span) * (height - pad * 2);
    return { x, y, point };
  });
  const polyline = coords.map((coord) => `${coord.x},${coord.y}`).join(" ");
  const unit = series === "heightCm" ? "cm" : "kg";

  return (
    <svg role="img" aria-label="变化曲线图" className="trend-chart" viewBox={`0 0 ${width} ${height}`}>
      <polyline fill="none" stroke="#60A5FA" strokeWidth="2.5" points={polyline} />
      {coords.map((coord) => (
        <circle key={coord.point.at} cx={coord.x} cy={coord.y} r="3.5" fill="#93C5FD">
          <title>
            {formatShanghaiMdHm(coord.point.at)} {coord.point.value}
            {unit}
          </title>
        </circle>
      ))}
    </svg>
  );
}
