import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { apiGet } from "../api/client";
import { addWeeks, countByLocalYmd, formatShanghaiHm, formatShanghaiMdHm, formatShanghaiYmd, formatYearMonth, formatYmd, parseYmd, weekContaining } from "../calendar/week";

type RecordItem = {
  id: number;
  type: "CONSUME" | "INTAKE";
  content: string;
  recordedAt: string;
};

type FilterMode = "day" | "month" | "range";
type LoadStatus = "loading" | "success" | "error";

/**
 * 日历页：周条为主体（小周切换 + hover + 数量气泡）；列表点进详情。
 */
export function CalendarPage() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const today = new Date();
  const todayYmd = formatYmd(today);
  const dateParam = searchParams.get("date");
  const initialSelected = dateParam && /^\d{4}-\d{2}-\d{2}$/.test(dateParam) ? dateParam : todayYmd;
  const [mode, setMode] = useState<FilterMode>("day");
  const [anchor, setAnchor] = useState(() => parseYmd(initialSelected));
  const [selected, setSelected] = useState(initialSelected);
  const [yearMonth, setYearMonth] = useState(formatYearMonth(today));
  const [from, setFrom] = useState(todayYmd);
  const [to, setTo] = useState(todayYmd);
  const [list, setList] = useState<RecordItem[]>([]);
  const [loadStatus, setLoadStatus] = useState<LoadStatus>("loading");
  const [reloadKey, setReloadKey] = useState(0);
  const [actionMessage, setActionMessage] = useState("");
  const [shareUrl, setShareUrl] = useState("");
  const week = weekContaining(anchor);
  const weekStart = formatYmd(week[0]);
  const weekEnd = formatYmd(week[6]);

  const periodQuery = () => {
    if (mode === "month") {
      return `yearMonth=${yearMonth}`;
    }
    if (mode === "range") {
      return `from=${from}&to=${to}`;
    }
    return `from=${weekStart}&to=${weekEnd}`;
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
  }, [isAuthenticated, mode, selected, yearMonth, from, to, reloadKey, weekStart, weekEnd]);

  const counts = useMemo(() => countByLocalYmd(list), [list]);
  const visibleList =
    mode === "day" ? list.filter((item) => formatShanghaiYmd(item.recordedAt) === selected) : list;

  const selectDay = (ymd: string) => {
    setSelected(ymd);
    setAnchor(parseYmd(ymd));
    setSearchParams({ date: ymd }, { replace: true });
  };

  const shiftWeek = (delta: number) => {
    const nextAnchor = addWeeks(anchor, delta);
    const nextWeek = weekContaining(nextAnchor);
    const idx = week.findIndex((day) => formatYmd(day) === selected);
    const picked = nextWeek[idx >= 0 ? idx : 0];
    const ymd = formatYmd(picked);
    setAnchor(nextAnchor);
    setSelected(ymd);
    setSearchParams({ date: ymd }, { replace: true });
  };

  const onJump = (ymd: string) => {
    if (!ymd) {
      return;
    }
    selectDay(ymd);
  };

  const csvFilename = () => {
    if (mode === "month") {
      return `workout-${yearMonth}.xlsx`;
    }
    if (mode === "range") {
      return from === to ? `workout-${from}.xlsx` : `workout-${from}_${to}.xlsx`;
    }
    return `workout-${selected}.xlsx`;
  };

  const emptyMessage =
    mode === "month" ? "这个月还没有记录" : mode === "range" ? "这段时间还没有记录" : "这一天还没有记录";

  const requireBodyOrRedirect = async (): Promise<boolean> => {
    const profile = await apiGet<{ heightCm: number | null; weightKg: number | null }>("/api/v1/profile");
    if (profile.heightCm == null || profile.weightKg == null) {
      setActionMessage("请先填写身高和体重");
      navigate("/profile/body");
      return false;
    }
    return true;
  };

  const onExport = async () => {
    if (!isAuthenticated) {
      navigate("/login?redirect=/calendar");
      return;
    }
    if (!(await requireBodyOrRedirect())) {
      return;
    }
    const token = localStorage.getItem("workout_token");
    const exportQuery = mode === "day" ? `date=${selected}` : periodQuery();
    const response = await fetch(`/api/v1/dailyRecords/exportCsv?${exportQuery}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    if (response.status === 401) {
      localStorage.removeItem("workout_token");
      navigate("/login?redirect=/calendar");
      return;
    }
    if (response.status === 400) {
      setActionMessage("请先填写身高和体重");
      navigate("/profile/body");
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

  const onShare = async () => {
    if (!isAuthenticated) {
      navigate("/login?redirect=/calendar");
      return;
    }
    if (!(await requireBodyOrRedirect())) {
      return;
    }
    const token = localStorage.getItem("workout_token");
    const exportQuery = mode === "day" ? `date=${selected}` : periodQuery();
    const response = await fetch(`/api/v1/shareReports?${exportQuery}`, {
      method: "POST",
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    if (response.status === 401) {
      localStorage.removeItem("workout_token");
      navigate("/login?redirect=/calendar");
      return;
    }
    const body = await response.json();
    if (!response.ok || body.code !== 200) {
      setActionMessage(body.msg || "分享失败");
      if (body.msg === "请先填写身高和体重") {
        navigate("/profile/body");
      }
      return;
    }
    setShareUrl(body.data.url);
    setActionMessage("");
  };

  const openDetail = (item: RecordItem) => {
    navigate(`/calendar/records/${item.id}?date=${formatShanghaiYmd(item.recordedAt)}`);
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
              <div className="week-nav">
                <button type="button" className="week-nav-btn" onClick={() => shiftWeek(-1)}>
                  上一周
                </button>
                <button type="button" className="week-nav-btn" onClick={() => shiftWeek(1)}>
                  下一周
                </button>
              </div>
              <button type="button" className="btn btn-text" onClick={() => navigate(`/record?date=${selected}`)}>
                补记
              </button>
            </div>
            <div className="week-strip" role="list" aria-label="周条">
              {week.map((day) => {
                const ymd = formatYmd(day);
                const isToday = ymd === todayYmd;
                const count = counts[ymd] ?? 0;
                return (
                  <button
                    key={ymd}
                    type="button"
                    className={`week-day${isToday ? " week-day--today" : ""}`}
                    aria-pressed={selected === ymd}
                    onClick={() => selectDay(ymd)}
                  >
                    {count > 0 ? (
                      <span className="week-day__badge" aria-label={`${count} 条记录`}>
                        {count}
                      </span>
                    ) : null}
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
        {loadStatus === "success" && visibleList.length === 0 ? <p className="empty-state">{emptyMessage}</p> : null}
        {loadStatus === "success" && visibleList.length > 0 ? (
          <ul className="record-list">
            {visibleList.map((item) => (
              <li
                key={item.id}
                className={item.type === "CONSUME" ? "record-consume" : "record-intake"}
                style={{ color: item.type === "CONSUME" ? "#16A34A" : "#DC2626" }}
              >
                <button type="button" className="record-list__open" onClick={() => openDetail(item)}>
                  <span className="record-list__time">
                    {mode === "day" ? formatShanghaiHm(item.recordedAt) : formatShanghaiMdHm(item.recordedAt)}
                  </span>
                  <span className="record-list__content">{item.content}</span>
                </button>
              </li>
            ))}
          </ul>
        ) : null}

        <div className="row">
          <button type="button" className="btn btn-ghost" onClick={onShare}>
            分享
          </button>
          <button type="button" className="btn btn-primary btn-block" onClick={onExport}>
            导出
          </button>
        </div>
        {actionMessage ? <p className="flash">{actionMessage}</p> : null}
        {shareUrl ? (
          <p className="flash">
            分享链接：{shareUrl}
          </p>
        ) : null}
      </div>
    </div>
  );
}
