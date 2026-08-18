import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import App from "./App";

describe("ProfilePage", () => {
  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem("workout_token", "tok");
  });

  it("fills saved profile and shows success after save", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockImplementation(async (url: string, init?: RequestInit) => {
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
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/profile"]}>
        <App />
      </MemoryRouter>,
    );
    expect(await screen.findByDisplayValue("小明")).toBeInTheDocument();
    await user.click(screen.getByRole("button", { name: "保存资料" }));
    expect(await screen.findByText("保存成功")).toBeInTheDocument();
  });

  it("clears token on logout", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({ code: 200, data: { nickname: null, heightCm: null, weightKg: null } }),
      }),
    );
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/profile"]}>
        <App />
      </MemoryRouter>,
    );
    await waitFor(() => expect(screen.getByRole("button", { name: "退出登录" })).toBeInTheDocument());
    await user.click(screen.getByRole("button", { name: "退出登录" }));
    expect(localStorage.getItem("workout_token")).toBeNull();
  });
});
