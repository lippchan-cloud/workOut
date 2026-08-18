import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { apiGet } from "../api/client";
import { addWeeks, formatYmd, weekContaining } from "../calendar/week";

type RecordItem = {
  id: number;
  type: "CONSUME" | "INTAKE";
  content: string;
  recordedAt: string;
};

/**
 * 日历页：周条、日列表、导出。
 */
export function CalendarPage() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const today = new Date();
  const [anchor, setAnchor] = useState(today);
  const [selected, setSelected] = useState(formatYmd(today));
  const [list, setList] = useState<RecordItem[]>([]);
  const week = weekContaining(anchor);

  useEffect(() => {
    if (!isAuthenticated) {
      return;
    }
    apiGet<{ date: string; list: RecordItem[] }>(`/api/v1/dailyRecords?date=${selected}`)
      .then((data) => setList(data.list))
      .catch(() => setList([]));
  }, [isAuthenticated, selected]);

  const onExport = async () => {
    if (!isAuthenticated) {
      navigate("/login?redirect=/calendar");
      return;
    }
    const token = localStorage.getItem("workout_token");
    const response = await fetch(`/api/v1/dailyRecords/exportCsv?date=${selected}`, {
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
    link.download = `workout-${selected}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div>
      <h1>日历</h1>
      <div>
        <button type="button" onClick={() => setAnchor(addWeeks(anchor, -1))}>
          上一周
        </button>
        <button type="button" onClick={() => setAnchor(addWeeks(anchor, 1))}>
          下一周
        </button>
      </div>
      <div role="list" aria-label="周条">
        {week.map((day) => {
          const ymd = formatYmd(day);
          const isToday = ymd === formatYmd(today);
          return (
            <button
              key={ymd}
              type="button"
              aria-pressed={selected === ymd}
              onClick={() => setSelected(ymd)}
            >
              {ymd.slice(5)}
              {isToday ? " 今" : ""}
            </button>
          );
        })}
      </div>
      {list.length === 0 ? (
        <p>这一天还没有记录</p>
      ) : (
        <ul>
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
      <button type="button" onClick={onExport}>
        导出 CSV
      </button>
    </div>
  );
}
