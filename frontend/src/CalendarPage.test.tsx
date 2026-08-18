import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import App from "./App";

describe("CalendarPage", () => {
  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem("workout_token", "tok");
  });

  it("renders week strip and empty message", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({ code: 200, msg: "OK", data: { date: "2026-08-18", list: [] } }),
      }),
    );
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
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({ code: 200, msg: "OK", data: { date: "x", list: [] } }),
      }),
    );
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
});
