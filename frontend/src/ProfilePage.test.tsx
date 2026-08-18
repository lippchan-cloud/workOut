import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, useLocation } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import App from "./App";

function LocationProbe() {
  const location = useLocation();
  return <div data-testid="location">{`${location.pathname}${location.search}`}</div>;
}

function stubProfileFetch() {
  vi.stubGlobal(
    "fetch",
    vi.fn().mockImplementation(async (url: string, init?: RequestInit) => {
      if (String(url).includes("/profile/trends")) {
        return {
          ok: true,
          status: 200,
          json: async () => ({ code: 200, data: { bodyHistory: [], recordCounts: [] } }),
        };
      }
      if (!init || init.method === undefined || init.method === "GET") {
        return {
          ok: true,
          status: 200,
          json: async () => ({
            code: 200,
            data: { nickname: "小明", heightCm: 175, weightKg: 70 },
          }),
        };
      }
      return { ok: true, status: 200, json: async () => ({ code: 200, data: {} }) };
    }),
  );
}

function renderProfile(path = "/profile") {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <LocationProbe />
      <App />
    </MemoryRouter>,
  );
}

describe("ProfilePage", () => {
  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem("workout_token", "tok");
  });

  it("shows three options on /profile without body or password fields", async () => {
    stubProfileFetch();
    renderProfile("/profile");
    expect(await screen.findByRole("button", { name: "身体资料" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "账号安全" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "退出登录" })).toBeInTheDocument();
    expect(screen.queryByPlaceholderText("例如 175")).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "保存资料" })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "修改密码" })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "注销账号" })).not.toBeInTheDocument();
  });

  it("opens body page from the option list and can save", async () => {
    stubProfileFetch();
    const user = userEvent.setup();
    renderProfile("/profile");
    await user.click(await screen.findByRole("button", { name: "身体资料" }));
    expect(await screen.findByTestId("location")).toHaveTextContent("/profile/body");
    expect(await screen.findByDisplayValue("小明")).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "修改密码" })).not.toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "保存资料" }));
    expect(await screen.findByText("保存成功")).toBeInTheDocument();
  });

  it("returns from body page to the option list", async () => {
    stubProfileFetch();
    const user = userEvent.setup();
    renderProfile("/profile/body");
    await user.click(await screen.findByRole("button", { name: "返回" }));
    expect(await screen.findByTestId("location")).toHaveTextContent(/^\/profile$/);
    expect(screen.getByRole("button", { name: "身体资料" })).toBeInTheDocument();
  });

  it("opens account page with password change and delete confirmation", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ code: 200, data: { nickname: null, heightCm: null, weightKg: null } }),
    });
    vi.stubGlobal("fetch", fetchMock);
    const confirmSpy = vi.spyOn(window, "confirm").mockReturnValue(false);
    const user = userEvent.setup();
    renderProfile("/profile");
    await user.click(await screen.findByRole("button", { name: "账号安全" }));
    expect(await screen.findByTestId("location")).toHaveTextContent("/profile/account");
    expect(screen.getByRole("button", { name: "修改密码" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "注销账号" })).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "保存资料" })).not.toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "注销账号" }));
    expect(confirmSpy).toHaveBeenCalled();
    expect(fetchMock.mock.calls.some((call) => call[1]?.method === "DELETE")).toBe(false);
    confirmSpy.mockRestore();
  });

  it("clears token on logout from the option list", async () => {
    stubProfileFetch();
    const user = userEvent.setup();
    renderProfile("/profile");
    await user.click(await screen.findByRole("button", { name: "退出登录" }));
    expect(localStorage.getItem("workout_token")).toBeNull();
  });

  it("shows CMS entry on account page for ADMIN only", async () => {
    stubProfileFetch();
    localStorage.setItem("workout_role", "ADMIN");
    renderProfile("/profile/account");
    expect(await screen.findByRole("link", { name: "后台管理" })).toHaveAttribute("href", "/cms");
  });

  it("hides CMS entry on account page for USER", async () => {
    stubProfileFetch();
    localStorage.setItem("workout_role", "USER");
    renderProfile("/profile/account");
    await waitFor(() => expect(screen.getByRole("button", { name: "修改密码" })).toBeInTheDocument());
    expect(screen.queryByRole("link", { name: "后台管理" })).not.toBeInTheDocument();
  });

  it("shows datetime defaulting to today and curve below the body form", async () => {
    stubProfileFetch();
    renderProfile("/profile/body");
    const datetime = await screen.findByLabelText("资料真实日期");
    expect((datetime as HTMLInputElement).value.startsWith(new Date().toISOString().slice(0, 10))).toBe(true);
    expect(screen.getByRole("button", { name: "保存资料" })).toBeInTheDocument();
    expect(await screen.findByText("还没有身体变化数据")).toBeInTheDocument();
  });

  it("saves changedAt with the body form", async () => {
    const fetchMock = vi.fn().mockImplementation(async (url: string, init?: RequestInit) => {
      if (String(url).includes("/profile/trends")) {
        return {
          ok: true,
          status: 200,
          json: async () => ({ code: 200, data: { bodyHistory: [], recordCounts: [] } }),
        };
      }
      if (!init || init.method === undefined || init.method === "GET") {
        return {
          ok: true,
          status: 200,
          json: async () => ({ code: 200, data: { nickname: "小明", heightCm: 175, weightKg: 70 } }),
        };
      }
      return { ok: true, status: 200, json: async () => ({ code: 200, data: {} }) };
    });
    vi.stubGlobal("fetch", fetchMock);
    const user = userEvent.setup();
    renderProfile("/profile/body");
    await screen.findByDisplayValue("小明");
    await user.click(screen.getByRole("button", { name: "保存资料" }));
    expect(await screen.findByText("保存成功")).toBeInTheDocument();
    const putCall = fetchMock.mock.calls.find((call) => call[1]?.method === "PUT");
    expect(putCall).toBeTruthy();
    const payload = JSON.parse(String(putCall?.[1]?.body));
    expect(payload.request.changedAt).toBeTruthy();
  });

  it("shows cm unit time axis and zoom changes precision", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockImplementation(async (url: string, init?: RequestInit) => {
        if (String(url).includes("/profile/trends")) {
          return {
            ok: true,
            status: 200,
            json: async () => ({
              code: 200,
              data: {
                bodyHistory: [
                  { changedAt: "2026-08-01T08:00:00+08:00", nickname: "小明", heightCm: 175, weightKg: 70 },
                  { changedAt: "2026-08-10T08:00:00+08:00", nickname: "小明", heightCm: 176, weightKg: 69 },
                ],
                recordCounts: [],
              },
            }),
          };
        }
        if (!init || init.method === undefined || init.method === "GET") {
          return {
            ok: true,
            status: 200,
            json: async () => ({ code: 200, data: { nickname: "小明", heightCm: 175, weightKg: 70 } }),
          };
        }
        return { ok: true, status: 200, json: async () => ({ code: 200, data: {} }) };
      }),
    );
    const user = userEvent.setup();
    renderProfile("/profile/body");
    await user.click(await screen.findByRole("button", { name: "身高" }));
    const chart = await screen.findByRole("img", { name: /成长曲线/ });
    expect(chart).toHaveAttribute("data-unit", "cm");
    expect(chart).toHaveAttribute("data-precision", "day");
    expect(screen.getByTestId("time-axis")).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "放大" }));
    expect(chart).toHaveAttribute("data-precision", "hour");
    expect(chart).not.toHaveStyle({ transform: expect.stringContaining("scale") } as never);
    expect(screen.getByRole("button", { name: "缩小" })).toHaveTextContent("−");
    expect(screen.getByRole("button", { name: "放大" })).toHaveTextContent("+");
  });
});
