import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, useLocation } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import App from "./App";

function LocationProbe() {
  const location = useLocation();
  return <div data-testid="location">{`${location.pathname}${location.search}`}</div>;
}

describe("csv export click", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.restoreAllMocks();
  });

  it("triggers download when authenticated", async () => {
    localStorage.setItem("workout_token", "tok");
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        ok: true,
        status: 200,
        json: async () => ({ code: 200, data: { list: [] } }),
        blob: async () => new Blob(["csv"]),
      }),
    );
    vi.stubGlobal("URL", {
      createObjectURL: vi.fn(() => "blob:mock"),
      revokeObjectURL: vi.fn(),
    });
    const clickSpy = vi.spyOn(HTMLAnchorElement.prototype, "click").mockImplementation(() => {});
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/calendar"]}>
        <App />
      </MemoryRouter>,
    );
    await user.click(screen.getByRole("button", { name: "导出 CSV" }));
    expect(clickSpy).toHaveBeenCalled();
  });

  it("redirects to login when unauthenticated export clicked", async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/calendar"]}>
        <LocationProbe />
        <App />
      </MemoryRouter>,
    );
    await user.click(screen.getByRole("button", { name: "导出 CSV" }));
    expect(await screen.findByTestId("location")).toHaveTextContent("/login?redirect=/calendar");
  });
});
