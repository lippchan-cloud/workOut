import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter } from "react-router-dom";
import { beforeEach, describe, expect, it } from "vitest";
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
});
