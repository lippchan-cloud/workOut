import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, useLocation } from "react-router-dom";
import { beforeEach, describe, expect, it } from "vitest";
import App from "./App";

function LocationProbe() {
  const location = useLocation();
  return <div data-testid="location">{`${location.pathname}${location.search}`}</div>;
}

function renderApp(initialPath = "/", withToken = false) {
  if (withToken) {
    localStorage.setItem("workout_token", "test-token");
  } else {
    localStorage.clear();
  }
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <LocationProbe />
      <App />
    </MemoryRouter>,
  );
}

describe("AppShell", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("renders three tab labels 记录 日历 我的", () => {
    renderApp("/");
    expect(screen.getByRole("button", { name: "记录" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "日历" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "我的" })).toBeInTheDocument();
  });

  it("allows authenticated tab switching without login redirect", async () => {
    const user = userEvent.setup();
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({ code: 200, data: { list: [], nickname: null, heightCm: null, weightKg: null } }),
      }),
    );
    renderApp("/", true);

    await user.click(screen.getByRole("button", { name: "日历" }));
    expect(await screen.findByTestId("location")).toHaveTextContent("/calendar");
    expect(screen.getByRole("heading", { name: "日历" })).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "我的" }));
    expect(await screen.findByTestId("location")).toHaveTextContent("/profile");
    expect(screen.getByRole("heading", { name: "我的" })).toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "记录" }));
    expect(await screen.findByTestId("location")).toHaveTextContent("/");
  });

  it("redirects unauthenticated tab clicks with correct redirect params", async () => {
    const user = userEvent.setup();
    renderApp("/");

    await user.click(screen.getByRole("button", { name: "记录" }));
    expect(await screen.findByTestId("location")).toHaveTextContent("/login?redirect=/");

    // 回到壳再点其他 Tab：从登录页无壳，需重新挂载未登录壳
  });
});

describe("AppShell unauthenticated redirects", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it.each([
    ["记录", "/"],
    ["日历", "/calendar"],
    ["我的", "/profile"],
  ] as const)("tab %s redirects to login?redirect=%s", async (label, path) => {
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/"]}>
        <LocationProbe />
        <App />
      </MemoryRouter>,
    );
    await user.click(screen.getByRole("button", { name: label }));
    expect(await screen.findByTestId("location")).toHaveTextContent(`/login?redirect=${path}`);
  });
});
