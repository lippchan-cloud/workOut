import { PointerEvent, useMemo, useRef, useState } from "react";
import { formatShanghaiMdHm, formatShanghaiYmd } from "../calendar/week";

export type BodyPoint = {
  changedAt: string;
  nickname: string | null;
  heightCm: number | null;
  weightKg: number | null;
};

export type RecordCount = {
  date: string;
  count: number;
};

type SeriesKey = "heightCm" | "weightKg";
export type TimePrecision = "hour" | "day" | "week" | "month";

const PRECISIONS: TimePrecision[] = ["hour", "day", "week", "month"];

/**
 * 成长曲线：时间轴 + 标准单位；放大改粒度，拖动平移窗口。
 */
export function GrowthCurve({ bodyHistory }: { bodyHistory: BodyPoint[] }) {
  const [series, setSeries] = useState<SeriesKey>("weightKg");
  const [precision, setPrecision] = useState<TimePrecision>("day");
  const [panShiftMs, setPanShiftMs] = useState(0);
  const drag = useRef<{ x: number; shift: number } | null>(null);

  const points = useMemo(
    () =>
      bodyHistory
        .filter((row) => row[series] != null)
        .map((row) => ({ at: row.changedAt, ms: new Date(row.changedAt).getTime(), value: Number(row[series]) })),
    [bodyHistory, series],
  );

  const unit = series === "heightCm" ? "cm" : "kg";
  const seriesLabel = series === "heightCm" ? "身高" : "体重";

  if (points.length === 0) {
    return (
      <div className="card stack">
        <div className="mode-switch" role="group" aria-label="曲线系列">
          <button type="button" className="btn btn-ghost" aria-pressed={series === "heightCm"} onClick={() => setSeries("heightCm")}>
            身高
          </button>
          <button type="button" className="btn btn-ghost" aria-pressed={series === "weightKg"} onClick={() => setSeries("weightKg")}>
            体重
          </button>
        </div>
        <p className="empty-state">这一项还没有数据</p>
      </div>
    );
  }

  const zoomIn = () => {
    const index = PRECISIONS.indexOf(precision);
    setPrecision(PRECISIONS[Math.max(0, index - 1)]);
  };

  const zoomOut = () => {
    const index = PRECISIONS.indexOf(precision);
    setPrecision(PRECISIONS[Math.min(PRECISIONS.length - 1, index + 1)]);
  };

  const onPointerDown = (event: PointerEvent<SVGSVGElement>) => {
    drag.current = { x: event.clientX, shift: panShiftMs };
    event.currentTarget.setPointerCapture(event.pointerId);
  };

  const onPointerMove = (event: PointerEvent<SVGSVGElement>) => {
    if (!drag.current) {
      return;
    }
    const span = Math.max(timespan(points, precision), 1);
    const dx = event.clientX - drag.current.x;
    const deltaMs = (-dx / 280) * span;
    setPanShiftMs(drag.current.shift + deltaMs);
  };

  const onPointerUp = () => {
    drag.current = null;
  };

  return (
    <div className="card stack">
      <div className="mode-switch" role="group" aria-label="曲线系列">
        <button type="button" className="btn btn-ghost" aria-pressed={series === "heightCm"} onClick={() => setSeries("heightCm")}>
          身高
        </button>
        <button type="button" className="btn btn-ghost" aria-pressed={series === "weightKg"} onClick={() => setSeries("weightKg")}>
          体重
        </button>
      </div>
      <div className="trend-toolbar">
        <button type="button" className="btn btn-ghost" onClick={zoomOut}>
          缩小
        </button>
        <button type="button" className="btn btn-ghost" onClick={zoomIn}>
          放大
        </button>
        <span className="trend-unit">
          {seriesLabel}（{unit}）
        </span>
      </div>
      <TrendSvg
        points={points}
        series={series}
        precision={precision}
        panShiftMs={panShiftMs}
        onPointerDown={onPointerDown}
        onPointerMove={onPointerMove}
        onPointerUp={onPointerUp}
      />
    </div>
  );
}

function timespan(points: { ms: number }[], precision: TimePrecision): number {
  const min = Math.min(...points.map((point) => point.ms));
  const max = Math.max(...points.map((point) => point.ms));
  const raw = Math.max(max - min, 1);
  if (precision === "hour") {
    return Math.max(raw, 12 * 3600_000);
  }
  if (precision === "day") {
    return Math.max(raw, 7 * 86400_000);
  }
  if (precision === "week") {
    return Math.max(raw, 28 * 86400_000);
  }
  return Math.max(raw, 180 * 86400_000);
}

function TrendSvg({
  points,
  series,
  precision,
  panShiftMs,
  onPointerDown,
  onPointerMove,
  onPointerUp,
}: {
  points: { at: string; ms: number; value: number }[];
  series: SeriesKey;
  precision: TimePrecision;
  panShiftMs: number;
  onPointerDown: (event: PointerEvent<SVGSVGElement>) => void;
  onPointerMove: (event: PointerEvent<SVGSVGElement>) => void;
  onPointerUp: () => void;
}) {
  const width = 320;
  const height = 200;
  const padLeft = 42;
  const padRight = 16;
  const padTop = 16;
  const padBottom = 36;
  const values = points.map((point) => point.value);
  const min = Math.min(...values);
  const max = Math.max(...values);
  const span = max - min || 1;
  const minMs = Math.min(...points.map((point) => point.ms));
  const maxMs = Math.max(...points.map((point) => point.ms));
  const windowMs = timespan(points, precision);
  const mid = (minMs + maxMs) / 2 + panShiftMs;
  const viewStart = mid - windowMs / 2;
  const viewEnd = mid + windowMs / 2;
  const xSpan = viewEnd - viewStart || 1;
  const innerW = width - padLeft - padRight;
  const innerH = height - padTop - padBottom;
  const coords = points.map((point) => {
    const x = padLeft + ((point.ms - viewStart) / xSpan) * innerW;
    const y = padTop + innerH - ((point.value - min) / span) * innerH;
    return { x, y, point };
  });
  const visible = coords.filter((coord) => coord.x >= padLeft - 4 && coord.x <= width - padRight + 4);
  const polyline = visible.map((coord) => `${coord.x},${coord.y}`).join(" ");
  const unit = series === "heightCm" ? "cm" : "kg";
  const ticks = axisTicks(viewStart, viewEnd, precision);

  return (
    <svg
      role="img"
      aria-label={`成长曲线 ${unit}`}
      data-unit={unit}
      data-precision={precision}
      className="trend-chart"
      viewBox={`0 0 ${width} ${height}`}
      onPointerDown={onPointerDown}
      onPointerMove={onPointerMove}
      onPointerUp={onPointerUp}
      onPointerCancel={onPointerUp}
    >
      <line x1={padLeft} y1={padTop} x2={padLeft} y2={height - padBottom} stroke="currentColor" strokeOpacity="0.35" />
      <line
        x1={padLeft}
        y1={height - padBottom}
        x2={width - padRight}
        y2={height - padBottom}
        stroke="currentColor"
        strokeOpacity="0.35"
      />
      <text x={8} y={padTop + 8} className="trend-axis-label">
        {unit}
      </text>
      <text x={8} y={height - padBottom} className="trend-axis-label">
        {min}
      </text>
      <text x={8} y={padTop + 20} className="trend-axis-label">
        {max}
      </text>
      <g data-testid="time-axis">
        {ticks.map((tick) => {
          const x = padLeft + ((tick.ms - viewStart) / xSpan) * innerW;
          return (
            <text key={tick.ms} x={x} y={height - 8} textAnchor="middle" className="trend-axis-label">
              {tick.label}
            </text>
          );
        })}
      </g>
      {polyline ? <polyline fill="none" stroke="#16A34A" strokeWidth="2.5" points={polyline} /> : null}
      {visible.map((coord) => (
        <circle key={coord.point.at} cx={coord.x} cy={coord.y} r="3.5" fill="#16A34A">
          <title>
            {formatShanghaiMdHm(coord.point.at)} {coord.point.value}
            {unit}
          </title>
        </circle>
      ))}
    </svg>
  );
}

function axisTicks(viewStart: number, viewEnd: number, precision: TimePrecision): { ms: number; label: string }[] {
  const count = 4;
  const step = (viewEnd - viewStart) / (count - 1);
  return Array.from({ length: count }, (_, index) => {
    const ms = viewStart + step * index;
    return { ms, label: formatTick(ms, precision) };
  });
}

function formatTick(ms: number, precision: TimePrecision): string {
  const iso = new Date(ms).toISOString();
  if (precision === "hour") {
    return formatShanghaiMdHm(iso);
  }
  if (precision === "month") {
    return formatShanghaiYmd(iso).slice(0, 7);
  }
  return formatShanghaiYmd(iso).slice(5);
}
