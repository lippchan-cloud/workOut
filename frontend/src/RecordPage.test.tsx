import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, useLocation, useNavigate } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";
import App from "./App";

function LocationProbe() {
  const location = useLocation();
  return <div data-testid="location">{`${location.pathname}${location.search}`}</div>;
}

function HistoryBack() {
  const navigate = useNavigate();
  return (
    <button type="button" onClick={() => navigate(-1)}>
      模拟浏览器返回
    </button>
  );
}

function renderRecordApp(initialPath = "/") {
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <LocationProbe />
      <HistoryBack />
      <App />
    </MemoryRouter>,
  );
}

async function openConsumeForm(user: ReturnType<typeof userEvent.setup>) {
  await user.click(screen.getByRole("button", { name: "开始记录" }));
  await user.click(screen.getByRole("button", { name: "消耗" }));
}

describe("RecordPage", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.unstubAllGlobals();
  });

  it("shows a large record entry button on the hub, not consume or intake forms", () => {
    renderRecordApp("/");

    expect(screen.getByRole("button", { name: "开始记录" })).toBeInTheDocument();
    expect(screen.queryByLabelText("消耗内容")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("摄入内容")).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "保存消耗" })).not.toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "保存摄入" })).not.toBeInTheDocument();
  });

  it("opens a type picker after the entry button, still without forms", async () => {
    const user = userEvent.setup();
    renderRecordApp("/");

    await user.click(screen.getByRole("button", { name: "开始记录" }));

    expect(await screen.findByTestId("location")).toHaveTextContent(/^\/record$/);
    expect(screen.getByRole("button", { name: "消耗" })).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "摄入" })).toBeInTheDocument();
    expect(screen.queryByLabelText("消耗内容")).not.toBeInTheDocument();
    expect(screen.queryByLabelText("摄入内容")).not.toBeInTheDocument();
  });

  it("opens the consume form after choosing 消耗", async () => {
    const user = userEvent.setup();
    renderRecordApp("/");

    await openConsumeForm(user);

    expect(await screen.findByTestId("location")).toHaveTextContent(/^\/record\/consume$/);
    expect(screen.getByLabelText("消耗内容")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "保存消耗" })).toBeInTheDocument();
    expect(screen.queryByLabelText("摄入内容")).not.toBeInTheDocument();
  });

  it("opens the intake form after choosing 摄入", async () => {
    const user = userEvent.setup();
    renderRecordApp("/");

    await user.click(screen.getByRole("button", { name: "开始记录" }));
    await user.click(screen.getByRole("button", { name: "摄入" }));

    expect(await screen.findByTestId("location")).toHaveTextContent(/^\/record\/intake$/);
    expect(screen.getByLabelText("摄入内容")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "保存摄入" })).toBeInTheDocument();
    expect(screen.queryByLabelText("消耗内容")).not.toBeInTheDocument();
  });

  it("browser back from the form returns to the type picker, then the hub", async () => {
    const user = userEvent.setup();
    renderRecordApp("/");

    await openConsumeForm(user);
    expect(await screen.findByTestId("location")).toHaveTextContent(/^\/record\/consume$/);

    await user.click(screen.getByRole("button", { name: "模拟浏览器返回" }));
    expect(await screen.findByTestId("location")).toHaveTextContent(/^\/record$/);
    expect(screen.getByRole("button", { name: "消耗" })).toBeInTheDocument();
    expect(screen.queryByLabelText("消耗内容")).not.toBeInTheDocument();

    await user.click(screen.getByRole("button", { name: "模拟浏览器返回" }));
    expect(await screen.findByTestId("location")).toHaveTextContent(/^\/$/);
    expect(screen.getByRole("button", { name: "开始记录" })).toBeInTheDocument();
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
    renderRecordApp("/");

    await openConsumeForm(user);
    const consume = screen.getByLabelText("消耗内容");
    await user.type(consume, "跑步 30 分钟");
    await user.click(screen.getByRole("button", { name: "保存消耗" }));

    await waitFor(() => {
      expect(consume).toHaveValue("");
    });
  });

  it("redirects to login when unauthenticated user clicks save", async () => {
    const user = userEvent.setup();
    renderRecordApp("/");

    await openConsumeForm(user);
    await user.click(screen.getByRole("button", { name: "保存消耗" }));
    expect(await screen.findByTestId("location")).toHaveTextContent("/login?redirect=/record/consume");
  });
});
