import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { apiGet } from "./api/client";

describe("api401", () => {
  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem("workout_token", "stale-token");
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue({
        status: 401,
        ok: false,
        json: async () => ({ code: 401, msg: "未登录或登录已过期", data: null }),
      }),
    );
    vi.stubGlobal("location", {
      pathname: "/calendar",
      assign: vi.fn(),
    });
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("clears token and redirects to login on 401", async () => {
    await expect(apiGet("/api/v1/profile")).rejects.toThrow();
    expect(localStorage.getItem("workout_token")).toBeNull();
    expect(window.location.assign).toHaveBeenCalledWith("/login?redirect=%2Fcalendar");
  });
});
