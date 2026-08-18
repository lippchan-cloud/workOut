import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { apiGet } from "../api/client";
import { addWeeks, formatYearMonth, formatYmd, parseYmd, weekContaining } from "../calendar/week";

type RecordItem = {
  id: number;
  type: "CONSUME" | "INTAKE";
  content: string;
  recordedAt: string;
};

type FilterMode = "day" | "month" | "range";

/**
 * 日历页：按日（周条 + 跳转）、按月、自定义区间；列表与导出共用筛选参数。
 */
export function CalendarPage() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const today = new Date();
  const todayYmd = formatYmd(today);
  const [mode, setMode] = useState<FilterMode>("day");
  const [anchor, setAnchor] = useState(today);
  const [selected, setSelected] = useState(todayYmd);
  const [yearMonth, setYearMonth] = useState(formatYearMonth(today));
  const [from, setFrom] = useState(todayYmd);
  const [to, setTo] = useState(todayYmd);
  const [list, setList] = useState<RecordItem[]>([]);
  const week = weekContaining(anchor);

  const periodQuery = () => {
    if (mode === "month") {
      return `yearMonth=${yearMonth}`;
    }
    if (mode === "range") {
      return `from=${from}&to=${to}`;
    }
    return `date=${selected}`;
  };

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }
    if (mode === "range" && from > to) {
      setList([]);
      return;
    }
    apiGet<{ list: RecordItem[] }>(`/api/v1/dailyRecords?${periodQuery()}`)
      .then((data) => setList(data.list))
      .catch(() => setList([]));
  }, [isAuthenticated, mode, selected, yearMonth, from, to]);

  const onJump = (ymd: string) => {
    if (!ymd) {
      return;
    }
    setSelected(ymd);
    setAnchor(parseYmd(ymd));
  };

  const csvFilename = () => {
    if (mode === "month") {
      return `workout-${yearMonth}.csv`;
    }
    if (mode === "range") {
      return from === to ? `workout-${from}.csv` : `workout-${from}_${to}.csv`;
    }
    return `workout-${selected}.csv`;
  };

  const emptyMessage =
    mode === "month" ? "这个月还没有记录" : mode === "range" ? "这段时间还没有记录" : "这一天还没有记录";

  const onExport = async () => {
    if (!isAuthenticated) {
      navigate("/login?redirect=/calendar");
      return;
    }
    const token = localStorage.getItem("workout_token");
    const response = await fetch(`/api/v1/dailyRecords/exportCsv?${periodQuery()}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    if (response.status === 401) {
      localStorage.removeItem("workout_token");
      navigate("/login?redirect=/calendar");
      return;
    }
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = csvFilename();
    link.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="page">
      <p className="page__eyebrow">Week</p>
      <h1 className="page__title">日历</h1>
      <p className="page__subtitle">按日回看 · 按月带走</p>

      <div className="card stack">
        <div className="mode-switch" role="group" aria-label="筛选模式">
          <button type="button" className="btn btn-ghost" aria-pressed={mode === "day"} onClick={() => setMode("day")}>
            按日
          </button>
          <button type="button" className="btn btn-ghost" aria-pressed={mode === "month"} onClick={() => setMode("month")}>
            按月
          </button>
          <button type="button" className="btn btn-ghost" aria-pressed={mode === "range"} onClick={() => setMode("range")}>
            自定义
          </button>
        </div>

        {mode === "day" ? (
          <>
            <label className="filter-field">
              跳转到
              <input type="date" aria-label="跳转到" value={selected} onChange={(event) => onJump(event.target.value)} />
            </label>
            <div className="week-toolbar">
              <button type="button" className="btn btn-ghost" onClick={() => setAnchor(addWeeks(anchor, -1))}>
                上一周
              </button>
              <button type="button" className="btn btn-ghost" onClick={() => setAnchor(addWeeks(anchor, 1))}>
                下一周
              </button>
            </div>
            <div className="week-strip" role="list" aria-label="周条">
              {week.map((day) => {
                const ymd = formatYmd(day);
                const isToday = ymd === todayYmd;
                return (
                  <button
                    key={ymd}
                    type="button"
                    className={`week-day${isToday ? " week-day--today" : ""}`}
                    aria-pressed={selected === ymd}
                    onClick={() => setSelected(ymd)}
                  >
                    {ymd.slice(5)}
                    {isToday ? " 今" : ""}
                  </button>
                );
              })}
            </div>
          </>
        ) : null}

        {mode === "month" ? (
          <label className="filter-field">
            选择月份
            <input
              type="month"
              aria-label="选择月份"
              value={yearMonth}
              onChange={(event) => setYearMonth(event.target.value)}
            />
          </label>
        ) : null}

        {mode === "range" ? (
          <div className="row">
            <label className="filter-field">
              开始日期
              <input type="date" aria-label="开始日期" value={from} onChange={(event) => setFrom(event.target.value)} />
            </label>
            <label className="filter-field">
              结束日期
              <input type="date" aria-label="结束日期" value={to} onChange={(event) => setTo(event.target.value)} />
            </label>
          </div>
        ) : null}

        {list.length === 0 ? (
          <p className="empty-state">{emptyMessage}</p>
        ) : (
          <ul className="record-list">
            {list.map((item) => (
              <li
                key={item.id}
                className={item.type === "CONSUME" ? "record-consume" : "record-intake"}
                style={{ color: item.type === "CONSUME" ? "#16A34A" : "#DC2626" }}
              >
                {item.content}
              </li>
            ))}
          </ul>
        )}

        <button type="button" className="btn btn-primary btn-block" onClick={onExport}>
          导出 CSV
        </button>
      </div>
    </div>
  );
}
