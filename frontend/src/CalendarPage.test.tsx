import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import App from "./App";
import { formatYmd } from "./calendar/week";

function emptyListResponse() {
  return {
    ok: true,
    status: 200,
    json: async () => ({ code: 200, msg: "OK", data: { date: "2026-08-18", list: [] } }),
    blob: async () => new Blob(["csv"]),
  };
}

describe("CalendarPage", () => {
  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem("workout_token", "tok");
  });

  it("renders week strip and empty message", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(emptyListResponse()));
    render(
      <MemoryRouter initialEntries={["/calendar"]}>
        <App />
      </MemoryRouter>,
    );
    expect(screen.getByRole("list", { name: "周条" })).toBeInTheDocument();
    expect(await screen.findByText("这一天还没有记录")).toBeInTheDocument();
  });

  it("renders consume green and intake red", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({
          code: 200,
          msg: "OK",
          data: {
            date: "2026-08-18",
            list: [
              { id: 1, type: "CONSUME", content: "跑步", recordedAt: "2026-08-18T07:30:00+08:00" },
              { id: 2, type: "INTAKE", content: "早餐", recordedAt: "2026-08-18T08:00:00+08:00" },
            ],
          },
        }),
      }),
    );
    render(
      <MemoryRouter initialEntries={["/calendar"]}>
        <App />
      </MemoryRouter>,
    );
    const consume = await screen.findByText("跑步");
    const intake = await screen.findByText("早餐");
    expect(consume).toHaveClass("record-consume");
    expect(intake).toHaveClass("record-intake");
    expect(consume).toHaveStyle({ color: "#16A34A" });
    expect(intake).toHaveStyle({ color: "#DC2626" });
  });

  it("switches to previous week", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(emptyListResponse()));
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/calendar"]}>
        <App />
      </MemoryRouter>,
    );
    const before = screen.getByRole("list", { name: "周条" }).textContent;
    await user.click(screen.getByRole("button", { name: "上一周" }));
    const after = screen.getByRole("list", { name: "周条" }).textContent;
    expect(after).not.toEqual(before);
  });

  it("switches to month mode and shows empty month message", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(emptyListResponse()));
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/calendar"]}>
        <App />
      </MemoryRouter>,
    );
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
    render(
      <MemoryRouter initialEntries={["/calendar"]}>
        <App />
      </MemoryRouter>,
    );
    await user.click(screen.getByRole("button", { name: "按月" }));
    const consume = await screen.findByText("月跑");
    const intake = await screen.findByText("月餐");
    expect(consume).toHaveClass("record-consume");
    expect(intake).toHaveClass("record-intake");
    expect(consume).toHaveStyle({ color: "#16A34A" });
    expect(intake).toHaveStyle({ color: "#DC2626" });
  });

  it("jumps to a date outside the visible week", async () => {
    const fetchMock = vi.fn().mockResolvedValue(emptyListResponse());
    vi.stubGlobal("fetch", fetchMock);
    render(
      <MemoryRouter initialEntries={["/calendar"]}>
        <App />
      </MemoryRouter>,
    );
    const target = new Date();
    target.setDate(target.getDate() - 21);
    const ymd = formatYmd(target);
    fireEvent.change(screen.getByLabelText("跳转到"), { target: { value: ymd } });
    await waitFor(() => {
      expect(screen.getByRole("list", { name: "周条" }).textContent).toContain(ymd.slice(5));
    });
    expect(fetchMock.mock.calls.some((call) => String(call[0]).includes(`date=${ymd}`))).toBe(true);
  });

  it("requests from and to in custom mode", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ code: 200, msg: "OK", data: { from: "2026-08-01", to: "2026-08-18", list: [] } }),
    });
    vi.stubGlobal("fetch", fetchMock);
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/calendar"]}>
        <App />
      </MemoryRouter>,
    );
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
    vi.stubGlobal(
      "fetch",
      vi.fn().mockReturnValue(new Promise(() => undefined)),
    );
    render(
      <MemoryRouter initialEntries={["/calendar"]}>
        <App />
      </MemoryRouter>,
    );
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
    render(
      <MemoryRouter initialEntries={["/calendar"]}>
        <App />
      </MemoryRouter>,
    );
    expect(await screen.findByText("记录加载失败")).toBeInTheDocument();
    expect(screen.queryByText("这一天还没有记录")).not.toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "重试" }));
    expect(await screen.findByText("这一天还没有记录")).toBeInTheDocument();
  });

  it("edits a calendar row into the consume form", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({
          code: 200,
          msg: "OK",
          data: {
            date: "2026-08-18",
            list: [{ id: 9, type: "CONSUME", content: "跑步", recordedAt: "2026-08-18T07:30:00+08:00" }],
          },
        }),
      }),
    );
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/calendar"]}>
        <App />
      </MemoryRouter>,
    );
    await user.click(await screen.findByRole("button", { name: "编辑" }));
    expect(await screen.findByDisplayValue("跑步")).toBeInTheDocument();
  });

  it("asks confirmation before deleting a calendar row", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({
        code: 200,
        msg: "OK",
        data: {
          date: "2026-08-18",
          list: [{ id: 9, type: "CONSUME", content: "跑步", recordedAt: "2026-08-18T07:30:00+08:00" }],
        },
      }),
    });
    vi.stubGlobal("fetch", fetchMock);
    const confirmSpy = vi.spyOn(window, "confirm").mockReturnValue(false);
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/calendar"]}>
        <App />
      </MemoryRouter>,
    );
    await user.click(await screen.findByRole("button", { name: "删除" }));
    expect(confirmSpy).toHaveBeenCalled();
    expect(fetchMock.mock.calls.some((call) => call[1]?.method === "DELETE")).toBe(false);
    confirmSpy.mockReturnValue(true);
    await user.click(screen.getByRole("button", { name: "删除" }));
    await waitFor(() => {
      expect(fetchMock.mock.calls.some((call) => String(call[0]).includes("/api/v1/dailyRecords/9") && call[1]?.method === "DELETE")).toBe(true);
    });
    confirmSpy.mockRestore();
  });

  it("backfills the selected day into the consume form datetime", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue(emptyListResponse()));
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/calendar"]}>
        <App />
      </MemoryRouter>,
    );
    fireEvent.change(screen.getByLabelText("跳转到"), { target: { value: "2026-08-10" } });
    await user.click(screen.getByRole("button", { name: "补记" }));
    await user.click(await screen.findByRole("button", { name: "消耗" }));
    const datetime = screen.getByLabelText("记录时间") as HTMLInputElement;
    expect(datetime.value.startsWith("2026-08-10")).toBe(true);
  });
});
