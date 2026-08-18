---
# ReportPage advice status tests (update existing)
---
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import App from "./App";

function stubReport(data: Record<string, unknown>) {
  vi.stubGlobal(
    "fetch",
    vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ code: 200, data }),
    }),
  );
}

const base = {
  from: "2026-08-01",
  to: "2026-08-18",
  displayName: "小明",
  records: [{ recordedAt: "2026-08-18T07:30:00+08:00", type: "CONSUME", content: "跑步" }],
  bodyHistory: [{ changedAt: "2026-08-01T08:00:00+08:00", heightCm: 175, weightKg: 70 }],
};

describe("public report page", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("renders stacked sections without the three tabs", async () => {
    stubReport({ ...base, advice: null, adviceStatus: "NONE_KEY" });
    render(
      <MemoryRouter initialEntries={["/report/abc123"]}>
        <App />
      </MemoryRouter>,
    );
    expect(await screen.findByRole("heading", { name: "用户名称" })).toBeInTheDocument();
    expect(screen.getByText("小明")).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "建议分析" })).toBeInTheDocument();
    expect(screen.getByText("未配置 API Key")).toBeInTheDocument();
    expect(screen.queryByRole("navigation", { name: "主导航" })).not.toBeInTheDocument();
    expect(screen.getByRole("link", { name: "回首页" })).toHaveAttribute("href", "/");
  });

  it("shows generating when pending", async () => {
    stubReport({ ...base, advice: null, adviceStatus: "PENDING" });
    render(
      <MemoryRouter initialEntries={["/report/pending1"]}>
        <App />
      </MemoryRouter>,
    );
    expect(await screen.findByText("生成中")).toBeInTheDocument();
  });

  it("shows ready advice text", async () => {
    stubReport({ ...base, advice: "多喝水，仅供参考", adviceStatus: "READY" });
    render(
      <MemoryRouter initialEntries={["/report/ready1"]}>
        <App />
      </MemoryRouter>,
    );
    expect(await screen.findByText("多喝水，仅供参考")).toBeInTheDocument();
  });
});
