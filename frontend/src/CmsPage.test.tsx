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

const account = {
  userId: 1,
  username: "cms_alice",
  createdAt: "2026-08-18T04:00:00Z",
  role: "USER",
  nickname: "阿丽",
  heightCm: 165,
  weightKg: 52,
};

const detail = {
  ...account,
  recordCount: 1,
  recentRecords: [{ id: 9, type: "CONSUME", content: "跑步", recordedAt: "2026-08-18T07:30:00Z" }],
  shares: [
    {
      id: "abcToken",
      userId: 1,
      username: "cms_alice",
      from: "2026-08-18",
      to: "2026-08-18",
      createdAt: "2026-08-18T04:00:00Z",
    },
  ],
};

function jsonOk(data: unknown) {
  return {
    ok: true,
    status: 200,
    json: async () => ({ code: 200, data }),
  };
}

function stubAdminApis() {
  vi.stubGlobal(
    "fetch",
    vi.fn(async (input: RequestInfo) => {
      const url = String(input);
      if (url.includes("/api/v1/admin/reports")) {
        return jsonOk({
          list: [
            {
              id: "abcToken",
              userId: 1,
              username: "cms_alice",
              from: "2026-08-18",
              to: "2026-08-18",
              createdAt: "2026-08-18T04:00:00Z",
            },
          ],
        });
      }
      if (/\/api\/v1\/admin\/accounts\/\d+/.test(url)) {
        return jsonOk(detail);
      }
      if (url.includes("/api/v1/admin/accounts")) {
        return jsonOk({ list: [account] });
      }
      return jsonOk({ list: [] });
    }),
  );
}

describe("CmsPage", () => {
  beforeEach(() => {
    localStorage.clear();
    stubAdminApis();
  });

  it("redirects anonymous visitors to login with redirect=/cms", async () => {
    renderAt("/cms");
    expect(await screen.findByTestId("location")).toHaveTextContent("/login?redirect=/cms");
  });

  it("redirects anonymous CMS reports visitors to login", async () => {
    renderAt("/cms/reports");
    expect(await screen.findByTestId("location")).toHaveTextContent("/login?redirect=/cms/reports");
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
    renderAt("/cms/accounts");
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

  it("admin sees function nav with accounts highlighted", async () => {
    localStorage.setItem("workout_token", "admin-tok");
    localStorage.setItem("workout_role", "ADMIN");
    renderAt("/cms/accounts");
    expect(await screen.findByRole("link", { name: "概览" })).toHaveAttribute("href", "/cms");
    expect(screen.getByRole("link", { name: "账户列表" })).toHaveAttribute("href", "/cms/accounts");
    expect(screen.getByRole("link", { name: "用户详情" })).toHaveAttribute("href", "/cms/users");
    expect(screen.getByRole("link", { name: "报告" })).toHaveAttribute("href", "/cms/reports");
    expect(screen.getByRole("link", { name: "账户列表" })).toHaveAttribute("aria-current", "page");
    expect(screen.queryByRole("navigation", { name: "主导航" })).not.toBeInTheDocument();
  });

  it("admin username links to user detail", async () => {
    localStorage.setItem("workout_token", "admin-tok");
    localStorage.setItem("workout_role", "ADMIN");
    renderAt("/cms/accounts");
    expect(await screen.findByRole("link", { name: "cms_alice" })).toHaveAttribute("href", "/cms/users/1");
  });

  it("admin user detail shows profile records and existing shares", async () => {
    localStorage.setItem("workout_token", "admin-tok");
    localStorage.setItem("workout_role", "ADMIN");
    renderAt("/cms/users/1");
    expect(await screen.findByText("cms_alice")).toBeInTheDocument();
    expect(screen.getByText("USER")).toBeInTheDocument();
    expect(screen.getByText("阿丽")).toBeInTheDocument();
    expect(screen.getByText("165")).toBeInTheDocument();
    expect(screen.getByText("52")).toBeInTheDocument();
    expect(screen.getByText("最近记录（1）")).toBeInTheDocument();
    expect(screen.getByText("跑步")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /查看报告/ })).toHaveAttribute("href", "/report/abcToken");
    expect(screen.getByRole("link", { name: "用户详情" })).toHaveAttribute("aria-current", "page");
  });

  it("admin reports page lists shares and public report links", async () => {
    localStorage.setItem("workout_token", "admin-tok");
    localStorage.setItem("workout_role", "ADMIN");
    renderAt("/cms/reports");
    expect(await screen.findByText("cms_alice")).toBeInTheDocument();
    expect(screen.getByRole("link", { name: /打开报告/ })).toHaveAttribute("href", "/report/abcToken");
    expect(screen.getByRole("link", { name: "报告" })).toHaveAttribute("aria-current", "page");
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
    renderAt("/cms/accounts");
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
    renderAt("/cms/accounts");
    expect(await screen.findByRole("alert")).toHaveTextContent("服务不可用");
  });
});
