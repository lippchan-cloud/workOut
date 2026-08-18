import { render, screen } from "@testing-library/react";
import { MemoryRouter, useLocation } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import App from "./App";

function LocationProbe() {
  const location = useLocation();
  return <div data-testid="location">{`${location.pathname}${location.search}`}</div>;
}

function renderAt(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <LocationProbe />
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
                role: "USER",
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

  it("redirects anonymous visitors to login with redirect=/cms", async () => {
    renderAt("/cms");
    expect(await screen.findByTestId("location")).toHaveTextContent("/login?redirect=/cms");
  });

  it("login page has no anonymous CMS entry", () => {
    renderAt("/login");
    expect(screen.queryByRole("link", { name: "后台管理" })).not.toBeInTheDocument();
  });

  it("regular user sees denial and no account table", async () => {
    localStorage.setItem("workout_token", "user-tok");
    localStorage.setItem("workout_role", "USER");
    renderAt("/cms");
    expect(await screen.findByText(/不是管理员/)).toBeInTheDocument();
    expect(screen.queryByText("cms_alice")).not.toBeInTheDocument();
  });

  it("admin can open CMS columns without passwordHash", async () => {
    localStorage.setItem("workout_token", "admin-tok");
    localStorage.setItem("workout_role", "ADMIN");
    renderAt("/cms");
    expect(await screen.findByText("用户ID")).toBeInTheDocument();
    expect(screen.getByText("用户名")).toBeInTheDocument();
    expect(screen.getByText("创建时间")).toBeInTheDocument();
    expect(screen.getByText("昵称")).toBeInTheDocument();
    expect(screen.getByText("身高")).toBeInTheDocument();
    expect(screen.getByText("体重")).toBeInTheDocument();
    expect(document.body.textContent).not.toContain("passwordHash");
    expect(await screen.findByText("cms_alice")).toBeInTheDocument();
    expect(screen.getByText("阿丽")).toBeInTheDocument();
  });

  it("admin sees loading then empty state", async () => {
    localStorage.setItem("workout_token", "admin-tok");
    localStorage.setItem("workout_role", "ADMIN");
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
      json: async () => ({ code: 200, data: { list: [] } }),
    });
    expect(await screen.findByText("暂无账户")).toBeInTheDocument();
  });

  it("admin sees error when the accounts request fails", async () => {
    localStorage.setItem("workout_token", "admin-tok");
    localStorage.setItem("workout_role", "ADMIN");
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
});
