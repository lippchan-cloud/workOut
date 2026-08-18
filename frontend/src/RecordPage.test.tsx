import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, useLocation } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import App from "./App";

function LocationProbe() {
  const location = useLocation();
  return <div data-testid="location">{`${location.pathname}${location.search}`}</div>;
}

describe("RecordPage", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.unstubAllGlobals();
  });

  it("clears consume input after successful save", async () => {
    localStorage.setItem("workout_token", "tok");
    const user = userEvent.setup();
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({ code: 200, msg: "OK", data: { id: 1, type: "CONSUME" } }),
      }),
    );
    render(
      <MemoryRouter initialEntries={["/"]}>
        <App />
      </MemoryRouter>,
    );

    const consume = screen.getByLabelText("消耗内容");
    await user.type(consume, "跑步 30 分钟");
    await user.click(screen.getByRole("button", { name: "保存消耗" }));

    await waitFor(() => {
      expect(consume).toHaveValue("");
    });
  });

  it("redirects to login when unauthenticated user clicks save", async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/"]}>
        <LocationProbe />
        <App />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole("button", { name: "保存消耗" }));
    expect(await screen.findByTestId("location")).toHaveTextContent("/login?redirect=/");
  });
});
