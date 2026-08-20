import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { AuthProvider } from "./auth/AuthContext";
import { LoginPage } from "./pages/LoginPage";

describe("loginFormValidation", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("shows Chinese prompt when username is empty", async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </MemoryRouter>,
    );

    await user.click(screen.getByRole("button", { name: "登录" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("请填写用户名");
  });

  it("can switch to email login and send a 4-digit code request", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ code: 200, data: {} }),
    });
    vi.stubGlobal("fetch", fetchMock);
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </MemoryRouter>,
    );

    await user.click(screen.getByRole("button", { name: "邮箱登录" }));
    expect(screen.queryByLabelText("验证码")).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "登录" })).not.toBeInTheDocument();
    await user.type(screen.getByLabelText("邮箱"), "alice@example.com");
    await user.click(screen.getByRole("button", { name: "发送验证码" }));
    expect(await screen.findByText("验证码已发送，请查收邮箱")).toBeInTheDocument();
    expect(screen.getByLabelText("验证码")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "登录" })).toBeInTheDocument();
    const sendCall = fetchMock.mock.calls.find((call) => String(call[0]).includes("/email/sendCode"));
    expect(sendCall).toBeTruthy();
    expect(JSON.parse(String(sendCall?.[1]?.body)).request).toEqual({
      email: "alice@example.com",
      purpose: "LOGIN",
    });
  });

  it("shows Chinese prompt when email login email is empty", async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </MemoryRouter>,
    );

    await user.click(screen.getByRole("button", { name: "邮箱登录" }));
    await user.click(screen.getByRole("button", { name: "发送验证码" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("请填写邮箱");
  });

  it("shows Chinese prompt when email login code is empty", async () => {
    const fetchMock = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      json: async () => ({ code: 200, data: {} }),
    });
    vi.stubGlobal("fetch", fetchMock);
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <AuthProvider>
          <LoginPage />
        </AuthProvider>
      </MemoryRouter>,
    );

    await user.click(screen.getByRole("button", { name: "邮箱登录" }));
    await user.type(screen.getByLabelText("邮箱"), "alice@example.com");
    await user.click(screen.getByRole("button", { name: "发送验证码" }));
    await screen.findByLabelText("验证码");
    await user.click(screen.getByRole("button", { name: "登录" }));
    expect(await screen.findByRole("alert")).toHaveTextContent("请填写4位验证码");
  });
});
