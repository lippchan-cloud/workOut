import { describe, expect, it } from "vitest";
import { weekContaining, addWeeks, formatYmd } from "./week";

describe("week utils", () => {
  it("returns Monday through Sunday for a Wednesday", () => {
    const days = weekContaining(new Date("2026-08-19T12:00:00+08:00"));
    expect(days.map(formatYmd)).toEqual([
      "2026-08-17",
      "2026-08-18",
      "2026-08-19",
      "2026-08-20",
      "2026-08-21",
      "2026-08-22",
      "2026-08-23",
    ]);
  });

  it("addWeeks moves by seven days", () => {
    const next = addWeeks(new Date("2026-08-17T00:00:00+08:00"), 1);
    expect(formatYmd(next)).toBe("2026-08-24");
  });
});
