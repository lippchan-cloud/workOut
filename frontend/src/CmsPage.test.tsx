import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import App from "./App";

function renderAt(path: string) {
  localStorage.clear();
  return render(
    <MemoryRouter initialEntries={[path]}>
      <App />
    </MemoryRouter>,
  );
}

describe("CmsPage", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({
          code: 200,
          data: {
            list: [
              {
                userId: 1,
                username: "cms_alice",
                createdAt: "2026-08-18T04:00:00Z",
                nickname: "阿丽",
                heightCm: 165,
                weightKg: 52,
              },
            ],
          },
        }),
      }),
    );
  });

  it("opens without token and shows temporary auth warning plus column labels", async () => {
    renderAt("/cms");
    expect(await screen.findByRole("status")).toHaveTextContent(/临时/);
    expect(screen.getByRole("status")).toHaveTextContent(/鉴权/);
    expect(screen.getByText("用户ID")).toBeInTheDocument();
    expect(screen.getByText("用户名")).toBeInTheDocument();
    expect(screen.getByText("创建时间")).toBeInTheDocument();
    expect(screen.getByText("昵称")).toBeInTheDocument();
    expect(screen.getByText("身高")).toBeInTheDocument();
    expect(screen.getByText("体重")).toBeInTheDocument();
    expect(document.body.textContent).not.toContain("passwordHash");
    expect(await screen.findByText("cms_alice")).toBeInTheDocument();
    expect(screen.getByText("阿丽")).toBeInTheDocument();
    expect(screen.getByText("cms_alice").closest("tr")).toHaveTextContent("1");
  });

  it("shows loading state before accounts arrive", async () => {
    let finish!: (value: unknown) => void;
    vi.stubGlobal(
      "fetch",
      vi.fn().mockReturnValue(
        new Promise((resolve) => {
          finish = resolve;
        }),
      ),
    );
    renderAt("/cms");
    expect(screen.getByText("加载中…")).toBeInTheDocument();
    finish({
      ok: true,
      status: 200,
      json: async () => ({
        code: 200,
        data: { list: [] },
      }),
    });
    expect(await screen.findByText("暂无账户")).toBeInTheDocument();
  });

  it("shows empty state when there are no accounts", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({ code: 200, data: { list: [] } }),
      }),
    );
    renderAt("/cms");
    expect(await screen.findByText("暂无账户")).toBeInTheDocument();
    expect(screen.queryByText("加载中…")).not.toBeInTheDocument();
  });

  it("shows error when the accounts request fails", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: false,
        status: 500,
        json: async () => ({ code: 500, msg: "服务不可用", data: null }),
      }),
    );
    renderAt("/cms");
    expect(await screen.findByRole("alert")).toHaveTextContent("服务不可用");
  });

  it("login page has visible CMS entry to /cms", () => {
    renderAt("/login");
    const link = screen.getByRole("link", { name: "后台管理" });
    expect(link).toHaveAttribute("href", "/cms");
  });
});
