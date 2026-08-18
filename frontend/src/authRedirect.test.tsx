import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, useLocation } from "react-router-dom";
import { describe, expect, it, beforeEach } from "vitest";
import App from "./App";

function LocationProbe() {
  const location = useLocation();
  return <div data-testid="location">{`${location.pathname}${location.search}`}</div>;
}

describe("authRedirect", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("navigates to login with redirect=/calendar when unauthenticated calendar tab clicked", async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={["/"]}>
        <LocationProbe />
        <App />
      </MemoryRouter>,
    );

    await user.click(screen.getByRole("button", { name: "日历" }));

    expect(await screen.findByTestId("location")).toHaveTextContent("/login?redirect=/calendar");
  });
});
