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
    await user.type(screen.getByLabelText("邮箱"), "alice@example.com");
    await user.click(screen.getByRole("button", { name: "发送验证码" }));
    expect(await screen.findByText("验证码已发送")).toBeInTheDocument();
    const sendCall = fetchMock.mock.calls.find((call) => String(call[0]).includes("/email/sendCode"));
    expect(sendCall).toBeTruthy();
    expect(JSON.parse(String(sendCall?.[1]?.body)).request).toEqual({
      email: "alice@example.com",
      purpose: "LOGIN",
    });
  });
});
