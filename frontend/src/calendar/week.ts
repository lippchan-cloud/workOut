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
