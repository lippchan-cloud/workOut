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

export function formatYmd(date: Date): string {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}
