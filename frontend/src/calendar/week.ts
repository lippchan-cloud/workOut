export function startOfDay(date: Date): Date {
  const copy = new Date(date);
  copy.setHours(0, 0, 0, 0);
  return copy;
}

/**
 * 返回包含给定日期的周一至周日（本地时区）。
 */
export function weekContaining(date: Date): Date[] {
  const start = startOfDay(date);
  const day = start.getDay(); // 0 Sun ... 6 Sat
  const offset = day === 0 ? 6 : day - 1;
  start.setDate(start.getDate() - offset);
  return Array.from({ length: 7 }, (_, i) => {
    const next = new Date(start);
    next.setDate(start.getDate() + i);
    return next;
  });
}

/**
 * 按周偏移（可为负）。
 */
export function addWeeks(date: Date, weeks: number): Date {
  const next = new Date(date);
  next.setDate(next.getDate() + weeks * 7);
  return next;
}

/**
 * 格式化为本地 YYYY-MM-DD。
 */
export function formatYmd(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

/**
 * 将 YYYY-MM-DD 解析为本地日期，避免 UTC 偏移。
 */
export function parseYmd(ymd: string): Date {
  const [year, month, day] = ymd.split("-").map(Number);
  return new Date(year, month - 1, day);
}

/**
 * 本地年月 YYYY-MM，供按月筛选默认值。
 */
export function formatYearMonth(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  return `${y}-${m}`;
}

/**
 * 按 Asia/Shanghai 自然日格式化时间戳，避免测试/浏览器时区把 +08:00 记到前一天。
 */
export function formatShanghaiYmd(isoOrDate: string | Date): string {
  const date = typeof isoOrDate === "string" ? new Date(isoOrDate) : isoOrDate;
  return date.toLocaleDateString("en-CA", { timeZone: "Asia/Shanghai" });
}

/**
 * 上海时区时分 HH:mm，供日列表展示。
 */
export function formatShanghaiHm(isoOrDate: string | Date): string {
  const date = typeof isoOrDate === "string" ? new Date(isoOrDate) : isoOrDate;
  return date.toLocaleTimeString("en-GB", {
    timeZone: "Asia/Shanghai",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
}

/**
 * 上海时区月日+时分，供月/区间列表展示。
 */
export function formatShanghaiMdHm(isoOrDate: string | Date): string {
  const date = typeof isoOrDate === "string" ? new Date(isoOrDate) : isoOrDate;
  return `${formatShanghaiYmd(date).slice(5)} ${formatShanghaiHm(date)}`;
}

/**
 * 将记录按上海时区自然日聚合条数，供周格子气泡一次计算。
 */
export function countByLocalYmd(records: { recordedAt: string }[]): Record<string, number> {
  const counts: Record<string, number> = {};
  for (const row of records) {
    const ymd = formatShanghaiYmd(row.recordedAt);
    counts[ymd] = (counts[ymd] ?? 0) + 1;
  }
  return counts;
}
