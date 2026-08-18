import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import App from "./App";

describe("public report page", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("renders stacked sections without the three tabs", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({
          code: 200,
          data: {
            from: "2026-08-01",
            to: "2026-08-18",
            displayName: "小明",
            records: [{ recordedAt: "2026-08-18T07:30:00+08:00", type: "CONSUME", content: "跑步" }],
            bodyHistory: [{ changedAt: "2026-08-01T08:00:00+08:00", heightCm: 175, weightKg: 70 }],
            advice: null,
          },
        }),
      }),
    );
    render(
      <MemoryRouter initialEntries={["/report/abc123"]}>
        <App />
      </MemoryRouter>,
    );
    expect(await screen.findByRole("heading", { name: "用户名称" })).toBeInTheDocument();
    expect(screen.getByText("小明")).toBeInTheDocument();
    expect(screen.getByText(/2026-08-01/)).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "事项列表" })).toBeInTheDocument();
    expect(screen.getByText("跑步")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "成长曲线" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "建议分析" })).toBeInTheDocument();
    expect(screen.getByText("建议分析（即将提供）")).toBeInTheDocument();
    expect(screen.queryByRole("navigation", { name: "主导航" })).not.toBeInTheDocument();
    expect(screen.getByRole("link", { name: "回首页" })).toHaveAttribute("href", "/");
    const row = screen.getByText("跑步").closest("li");
    expect(row).toHaveClass("record-consume");
    expect(row?.closest("ul")).toHaveClass("record-list--stacked");
  });
});
