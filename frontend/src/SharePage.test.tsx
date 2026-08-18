import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, useLocation } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import App from "./App";

function LocationProbe() {
  const location = useLocation();
  return <div data-testid="location">{`${location.pathname}${location.search}`}</div>;
}

function renderShare(path: string) {
  return render(
    <MemoryRouter initialEntries={[path]}>
      <LocationProbe />
      <App />
    </MemoryRouter>,
  );
}

describe("calendar share secondary page", () => {
  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem("workout_token", "tok");
  });

  it("posts the share for the query and shows a copyable url", async () => {
    const fetchMock = vi.fn().mockImplementation(async (url: string, init?: RequestInit) => {
      if (String(url).includes("/api/v1/shareReports")) {
        expect(init?.method).toBe("POST");
        return {
          ok: true,
          status: 200,
          json: async () => ({
            code: 200,
            data: { id: "abc123token", url: "http://localhost:8080/report/abc123token" },
          }),
        };
      }
      return {
        ok: true,
        status: 200,
        json: async () => ({ code: 200, data: { list: [] } }),
      };
    });
    vi.stubGlobal("fetch", fetchMock);
    renderShare("/calendar/share?date=2026-08-18");
    expect(await screen.findByText("http://localhost:8080/report/abc123token")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "复制" })).toBeInTheDocument();
    await waitFor(() => {
      expect(fetchMock.mock.calls.some((call) => String(call[0]).includes("/api/v1/shareReports?date=2026-08-18"))).toBe(
        true,
      );
    });
  });

  it("returns to the calendar with the same filter query", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockImplementation(async (url: string) => {
        if (String(url).includes("/api/v1/shareReports")) {
          return {
            ok: true,
            status: 200,
            json: async () => ({
              code: 200,
              data: { id: "abc123token", url: "http://localhost:8080/report/abc123token" },
            }),
          };
        }
        return {
          ok: true,
          status: 200,
          json: async () => ({ code: 200, data: { list: [] } }),
        };
      }),
    );
    const user = userEvent.setup();
    renderShare("/calendar/share?date=2026-08-18");
    await user.click(await screen.findByRole("button", { name: "返回日历" }));
    expect(await screen.findByTestId("location")).toHaveTextContent("/calendar?date=2026-08-18");
  });
});
