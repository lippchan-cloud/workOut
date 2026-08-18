import { describe, expect, it } from "vitest";
import { weekContaining, addWeeks, formatYmd, countByLocalYmd, formatShanghaiHm, formatShanghaiMdHm } from "./week";

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

  it("counts records by local calendar day", () => {
    const counts = countByLocalYmd([
      { recordedAt: "2026-08-18T07:30:00+08:00" },
      { recordedAt: "2026-08-18T08:00:00+08:00" },
      { recordedAt: "2026-08-19T09:00:00+08:00" },
    ]);
    expect(counts["2026-08-18"]).toBe(2);
    expect(counts["2026-08-19"]).toBe(1);
    expect(counts["2026-08-17"]).toBeUndefined();
  });

  it("formats shanghai hours and minutes", () => {
    expect(formatShanghaiHm("2026-08-18T07:30:00+08:00")).toBe("07:30");
  });

  it("formats shanghai month-day and time", () => {
    expect(formatShanghaiMdHm("2026-08-01T07:30:00+08:00")).toBe("08-01 07:30");
  });
});
