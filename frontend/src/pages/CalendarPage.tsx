import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { apiDelete, apiGet } from "../api/client";
import { addWeeks, formatYearMonth, formatYmd, parseYmd, weekContaining } from "../calendar/week";

type RecordItem = {
  id: number;
  type: "CONSUME" | "INTAKE";
  content: string;
  recordedAt: string;
};

type FilterMode = "day" | "month" | "range";
type LoadStatus = "loading" | "success" | "error";

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
  const [loadStatus, setLoadStatus] = useState<LoadStatus>("loading");
  const [reloadKey, setReloadKey] = useState(0);
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
      setLoadStatus("success");
      return;
    }
    let cancelled = false;
    setLoadStatus("loading");
    apiGet<{ list: RecordItem[] }>(`/api/v1/dailyRecords?${periodQuery()}`)
      .then((data) => {
        if (cancelled) {
          return;
        }
        setList(data.list);
        setLoadStatus("success");
      })
      .catch(() => {
        if (cancelled) {
          return;
        }
        setList([]);
        setLoadStatus("error");
      });
    return () => {
      cancelled = true;
    };
  }, [isAuthenticated, mode, selected, yearMonth, from, to, reloadKey]);

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

  const onEdit = (item: RecordItem) => {
    const path = item.type === "CONSUME" ? "/record/consume" : "/record/intake";
    navigate(`${path}?edit=${item.id}`, { state: item });
  };

  const onDelete = async (item: RecordItem) => {
    if (!window.confirm("确认删除这条记录？")) {
      return;
    }
    await apiDelete(`/api/v1/dailyRecords/${item.id}`);
    setReloadKey((value) => value + 1);
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
              <button type="button" className="btn btn-primary" onClick={() => navigate(`/record?date=${selected}`)}>
                补记
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

        {loadStatus === "loading" ? <p className="empty-state">加载中…</p> : null}
        {loadStatus === "error" ? (
          <div className="empty-state">
            <p>记录加载失败</p>
            <button type="button" className="btn btn-ghost" onClick={() => setReloadKey((value) => value + 1)}>
              重试
            </button>
          </div>
        ) : null}
        {loadStatus === "success" && list.length === 0 ? <p className="empty-state">{emptyMessage}</p> : null}
        {loadStatus === "success" && list.length > 0 ? (
          <ul className="record-list">
            {list.map((item) => (
              <li
                key={item.id}
                className={item.type === "CONSUME" ? "record-consume" : "record-intake"}
                style={{ color: item.type === "CONSUME" ? "#16A34A" : "#DC2626" }}
              >
                {item.content}
                <span className="record-list__actions">
                  <button type="button" className="btn btn-ghost" onClick={() => onEdit(item)}>
                    编辑
                  </button>
                  <button type="button" className="btn btn-ghost" onClick={() => onDelete(item)}>
                    删除
                  </button>
                </span>
              </li>
            ))}
          </ul>
        ) : null}

        <button type="button" className="btn btn-primary btn-block" onClick={onExport}>
          导出 CSV
        </button>
      </div>
    </div>
  );
}
