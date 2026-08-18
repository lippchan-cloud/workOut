import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, useLocation } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import App from "./App";
import { formatYmd } from "./calendar/week";

function LocationProbe() {
  const location = useLocation();
  return <div data-testid="location">{`${location.pathname}${location.search}`}</div>;
}

function emptyListResponse() {
  return {
    ok: true,
    status: 200,
    json: async () => ({ code: 200, msg: "OK", data: { date: "2026-08-18", list: [] } }),
    blob: async () => new Blob(["csv"]),
  };
}

const sampleRun = {
  id: 9,
  type: "CONSUME" as const,
  content: "跑步",
  recordedAt: "2026-08-18T07:30:00+08:00",
};

function fetchListAndDetail(list: typeof sampleRun[]) {
  return vi.fn().mockImplementation(async (url: string) => {
    const path = String(url);
    if (/\/dailyRecords\/\d+$/.test(path)) {
      return {
        ok: true,
        status: 200,
        json: async () => ({ code: 200, msg: "OK", data: list[0] ?? sampleRun }),
      };
    }
    return {
      ok: true,
      status: 200,
      json: async () => ({ code: 200, msg: "OK", data: { list } }),
      blob: async () => new Blob(["csv"]),
    };
  });
}

function renderCalendar(path = "/calendar") {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <LocationProbe />
      <App />
    </MemoryRouter>,
  );
}

describe("CalendarPage", () => {
  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem("workout_token", "tok");
  });

  it("renders week strip and empty message", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(emptyListResponse()));
    renderCalendar();
    expect(screen.getByRole("list", { name: "周条" })).toBeInTheDocument();
    expect(await screen.findByText("这一天还没有记录")).toBeInTheDocument();
  });

  it("uses small week-nav controls that still change the week", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(emptyListResponse()));
    const user = userEvent.setup();
    renderCalendar();
    const prev = screen.getByRole("button", { name: "上一周" });
    const next = screen.getByRole("button", { name: "下一周" });
    expect(prev).toHaveClass("week-nav-btn");
    expect(next).toHaveClass("week-nav-btn");
    expect(prev).not.toHaveClass("btn-record-hero");
    expect(prev).not.toHaveClass("btn-block");
    const before = screen.getByRole("list", { name: "周条" }).textContent;
    await user.click(prev);
    const after = screen.getByRole("list", { name: "周条" }).textContent;
    expect(after).not.toEqual(before);
    const dayCell = screen.getByRole("list", { name: "周条" }).querySelector("button.week-day");
    expect(dayCell).not.toBeNull();
    (dayCell as HTMLButtonElement).focus();
    expect(dayCell).toHaveFocus();
  });

  it("loads the visible week with from and to in one request", async () => {
    const fetchMock = vi.fn().mockResolvedValue(emptyListResponse());
    vi.stubGlobal("fetch", fetchMock);
    renderCalendar("/calendar?date=2026-08-18");
    await waitFor(() => {
      expect(
        fetchMock.mock.calls.some((call) => {
          const url = String(call[0]);
          return url.includes("from=2026-08-17") && url.includes("to=2026-08-23");
        }),
      ).toBe(true);
    });
    expect(fetchMock.mock.calls.filter((call) => String(call[0]).includes("date=")).length).toBe(0);
  });

  it("shows a count badge on days that have records", async () => {
    vi.stubGlobal(
      "fetch",
      fetchListAndDetail([
        sampleRun,
        { id: 10, type: "INTAKE", content: "早餐", recordedAt: "2026-08-18T08:00:00+08:00" },
      ]),
    );
    renderCalendar("/calendar?date=2026-08-18");
    const badge = await screen.findByLabelText("2 条记录");
    expect(badge).toHaveTextContent("2");
    expect(screen.queryByLabelText("0 条记录")).not.toBeInTheDocument();
  });

  it("renders consume green and intake red", async () => {
    vi.stubGlobal(
      "fetch",
      fetchListAndDetail([
        { id: 1, type: "CONSUME", content: "跑步", recordedAt: "2026-08-18T07:30:00+08:00" },
        { id: 2, type: "INTAKE", content: "早餐", recordedAt: "2026-08-18T08:00:00+08:00" },
      ]),
    );
    renderCalendar("/calendar?date=2026-08-18");
    const consume = await screen.findByText("跑步");
    const intake = await screen.findByText("早餐");
    expect(consume.closest("li")).toHaveClass("record-consume");
    expect(intake.closest("li")).toHaveClass("record-intake");
    expect(consume.closest("li")).toHaveStyle({ color: "#16A34A" });
    expect(intake.closest("li")).toHaveStyle({ color: "#DC2626" });
    expect(screen.getByText("07:30")).toBeInTheDocument();
    expect(screen.getByText("08:00")).toBeInTheDocument();
  });

  it("switches to previous week", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(emptyListResponse()));
    const user = userEvent.setup();
    renderCalendar();
    const before = screen.getByRole("list", { name: "周条" }).textContent;
    await user.click(screen.getByRole("button", { name: "上一周" }));
    const after = screen.getByRole("list", { name: "周条" }).textContent;
    expect(after).not.toEqual(before);
  });

  it("switches to month mode and shows empty month message", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(emptyListResponse()));
    const user = userEvent.setup();
    renderCalendar();
    await user.click(screen.getByRole("button", { name: "按月" }));
    expect(screen.getByLabelText("选择月份")).toBeInTheDocument();
    expect(await screen.findByText("这个月还没有记录")).toBeInTheDocument();
  });

  it("renders consume green and intake red in month mode", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockImplementation(async (url: string) => ({
        ok: true,
        status: 200,
        json: async () =>
          String(url).includes("yearMonth=")
            ? {
                code: 200,
                msg: "OK",
                data: {
                  yearMonth: "2026-08",
                  list: [
                    { id: 1, type: "CONSUME", content: "月跑", recordedAt: "2026-08-01T07:30:00+08:00" },
                    { id: 2, type: "INTAKE", content: "月餐", recordedAt: "2026-08-31T08:00:00+08:00" },
                  ],
                },
              }
            : { code: 200, msg: "OK", data: { date: "x", list: [] } },
      })),
    );
    const user = userEvent.setup();
    renderCalendar();
    await user.click(screen.getByRole("button", { name: "按月" }));
    const consume = await screen.findByText("月跑");
    const intake = await screen.findByText("月餐");
    expect(consume.closest("li")).toHaveClass("record-consume");
    expect(intake.closest("li")).toHaveClass("record-intake");
    expect(consume.closest("li")).toHaveStyle({ color: "#16A34A" });
    expect(intake.closest("li")).toHaveStyle({ color: "#DC2626" });
    expect(screen.getByText("08-01 07:30")).toBeInTheDocument();
    expect(screen.getByText("08-31 08:00")).toBeInTheDocument();
  });

  it("jumps to a date outside the visible week", async () => {
    const fetchMock = vi.fn().mockResolvedValue(emptyListResponse());
    vi.stubGlobal("fetch", fetchMock);
    renderCalendar();
    const target = new Date();
    target.setDate(target.getDate() - 21);
    const ymd = formatYmd(target);
    fireEvent.change(screen.getByLabelText("跳转到"), { target: { value: ymd } });
    await waitFor(() => {
      expect(screen.getByRole("list", { name: "周条" }).textContent).toContain(ymd.slice(5));
    });
    expect(fetchMock.mock.calls.some((call) => String(call[0]).includes(`from=`))).toBe(true);
    expect(fetchMock.mock.calls.some((call) => String(call[0]).includes(`date=${ymd}`))).toBe(false);
  });

  it("requests from and to in custom mode", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ code: 200, msg: "OK", data: { from: "2026-08-01", to: "2026-08-18", list: [] } }),
    });
    vi.stubGlobal("fetch", fetchMock);
    const user = userEvent.setup();
    renderCalendar();
    await user.click(screen.getByRole("button", { name: "自定义" }));
    fireEvent.change(screen.getByLabelText("开始日期"), { target: { value: "2026-08-01" } });
    fireEvent.change(screen.getByLabelText("结束日期"), { target: { value: "2026-08-18" } });
    expect(
      fetchMock.mock.calls.some((call) => {
        const url = String(call[0]);
        return url.includes("from=2026-08-01") && url.includes("to=2026-08-18");
      }),
    ).toBe(true);
    expect(await screen.findByText("这段时间还没有记录")).toBeInTheDocument();
  });

  it("shows loading instead of empty while the list request is in flight", () => {
    vi.stubGlobal("fetch", vi.fn().mockReturnValue(new Promise(() => undefined)));
    renderCalendar();
    expect(screen.getByText("加载中…")).toBeInTheDocument();
    expect(screen.queryByText("这一天还没有记录")).not.toBeInTheDocument();
  });

  it("shows failure with retry instead of empty when list request fails", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce({
        ok: false,
        status: 500,
        json: async () => ({ code: 500, msg: "服务异常", data: null }),
      })
      .mockResolvedValue(emptyListResponse());
    vi.stubGlobal("fetch", fetchMock);
    const user = userEvent.setup();
    renderCalendar();
    expect(await screen.findByText("记录加载失败")).toBeInTheDocument();
    expect(screen.queryByText("这一天还没有记录")).not.toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "重试" }));
    expect(await screen.findByText("这一天还没有记录")).toBeInTheDocument();
  });

  it("opens detail from a list row and loads GET by id", async () => {
    const fetchMock = fetchListAndDetail([sampleRun]);
    vi.stubGlobal("fetch", fetchMock);
    const user = userEvent.setup();
    renderCalendar("/calendar?date=2026-08-18");
    await user.click(await screen.findByRole("button", { name: /跑步/ }));
    expect(await screen.findByTestId("location")).toHaveTextContent("/calendar/records/9");
    await waitFor(() => {
      expect(fetchMock.mock.calls.some((call) => String(call[0]).includes("/api/v1/dailyRecords/9"))).toBe(true);
    });
    expect(await screen.findByText("跑步")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "编辑" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "删除" })).toBeInTheDocument();
  });

  it("edits from the detail page into the consume form", async () => {
    vi.stubGlobal("fetch", fetchListAndDetail([sampleRun]));
    const user = userEvent.setup();
    renderCalendar("/calendar?date=2026-08-18");
    await user.click(await screen.findByRole("button", { name: /跑步/ }));
    await user.click(await screen.findByRole("button", { name: "编辑" }));
    expect(await screen.findByDisplayValue("跑步")).toBeInTheDocument();
  });

  it("asks confirmation before deleting on the detail page", async () => {
    const fetchMock = fetchListAndDetail([sampleRun]);
    vi.stubGlobal("fetch", fetchMock);
    const confirmSpy = vi.spyOn(window, "confirm").mockReturnValue(false);
    const user = userEvent.setup();
    renderCalendar("/calendar?date=2026-08-18");
    await user.click(await screen.findByRole("button", { name: /跑步/ }));
    await user.click(await screen.findByRole("button", { name: "删除" }));
    expect(confirmSpy).toHaveBeenCalled();
    expect(fetchMock.mock.calls.some((call) => call[1]?.method === "DELETE")).toBe(false);
    confirmSpy.mockReturnValue(true);
    await user.click(screen.getByRole("button", { name: "删除" }));
    await waitFor(() => {
      expect(
        fetchMock.mock.calls.some(
          (call) => String(call[0]).includes("/api/v1/dailyRecords/9") && call[1]?.method === "DELETE",
        ),
      ).toBe(true);
    });
    confirmSpy.mockRestore();
  });

  it("returns from detail to the record's calendar day", async () => {
    vi.stubGlobal("fetch", fetchListAndDetail([sampleRun]));
    const user = userEvent.setup();
    renderCalendar("/calendar/records/9");
    await user.click(await screen.findByRole("button", { name: "返回" }));
    expect(await screen.findByTestId("location")).toHaveTextContent("/calendar?date=2026-08-18");
  });

  it("backfills the selected day into the consume form datetime", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(emptyListResponse()));
    const user = userEvent.setup();
    renderCalendar();
    fireEvent.change(screen.getByLabelText("跳转到"), { target: { value: "2026-08-10" } });
    const backfill = screen.getByRole("button", { name: "补记" });
    expect(backfill).not.toHaveClass("btn-record-hero");
    expect(backfill).not.toHaveClass("btn-block");
    await user.click(backfill);
    await user.click(await screen.findByRole("button", { name: "消耗" }));
    const datetime = screen.getByLabelText("记录时间") as HTMLInputElement;
    expect(datetime.value.startsWith("2026-08-10")).toBe(true);
  });

  it("opens trends from calendar and back returns to calendar", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(emptyListResponse()));
    const user = userEvent.setup();
    renderCalendar();
    await user.click(screen.getByRole("button", { name: "变化曲线" }));
    expect(screen.getByTestId("location")).toHaveTextContent("/calendar/trends");
    await user.click(screen.getByRole("button", { name: "返回" }));
    expect(screen.getByTestId("location")).toHaveTextContent("/calendar");
  });

  it("shows empty state on trends when there is no body history", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockImplementation(async (url: string) => {
        if (String(url).includes("/profile/trends")) {
          return {
            ok: true,
            status: 200,
            json: async () => ({ code: 200, msg: "OK", data: { bodyHistory: [], recordCounts: [] } }),
          };
        }
        return emptyListResponse();
      }),
    );
    renderCalendar("/calendar/trends");
    expect(await screen.findByText("还没有身体变化数据")).toBeInTheDocument();
    expect(screen.queryByRole("img", { name: "变化曲线图" })).not.toBeInTheDocument();
  });
});
