import { FormEvent, useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { apiPost, apiPut } from "../api/client";

const DRAFT_KEY = "workout_record_draft";

type RecordDraft = {
  type: "CONSUME" | "INTAKE";
  content: string;
  recordedAt: string;
  path: string;
};

function nowLocalInput(): string {
  const now = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`;
}

function dateToLocalInput(ymd: string): string {
  return `${ymd}T${nowLocalInput().slice(11)}`;
}

function toIso(local: string): string {
  return new Date(local).toISOString();
}

function toLocalInput(iso: string): string {
  const date = new Date(iso);
  if (Number.isNaN(date.getTime())) {
    return nowLocalInput();
  }
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function saveDraft(draft: RecordDraft) {
  sessionStorage.setItem(DRAFT_KEY, JSON.stringify(draft));
}

function readDraft(path: string): RecordDraft | null {
  const raw = sessionStorage.getItem(DRAFT_KEY);
  if (!raw) {
    return null;
  }
  try {
    const draft = JSON.parse(raw) as RecordDraft;
    return draft.path === path ? draft : null;
  } catch {
    return null;
  }
}

function RecordForm({
  type,
  title,
  contentLabel,
  saveLabel,
}: {
  type: "CONSUME" | "INTAKE";
  title: string;
  contentLabel: string;
  saveLabel: string;
}) {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const state = location.state as { id?: number; content?: string; recordedAt?: string } | null;
  const editId = searchParams.get("edit");
  const dateParam = searchParams.get("date");
  const isConsume = type === "CONSUME";
  const formPath = isConsume ? "/record/consume" : "/record/intake";
  const [content, setContent] = useState("");
  const [recordedAt, setRecordedAt] = useState(nowLocalInput);
  const [message, setMessage] = useState("");
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    const draft = readDraft(formPath);
    if (draft) {
      setContent(draft.content);
      setRecordedAt(draft.recordedAt);
      sessionStorage.removeItem(DRAFT_KEY);
      return;
    }
    if (state?.content) {
      setContent(state.content);
    }
    if (state?.recordedAt) {
      setRecordedAt(toLocalInput(state.recordedAt));
    } else if (dateParam) {
      setRecordedAt(dateToLocalInput(dateParam));
    }
  }, [formPath, state, dateParam]);

  const validate = (value: string): string => {
    const trimmed = value.trim();
    if (!trimmed) {
      return "请填写内容";
    }
    if (trimmed.length > 500) {
      return "内容最多500字";
    }
    return "";
  };

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!isAuthenticated) {
      saveDraft({ type, content, recordedAt, path: location.pathname + location.search });
      navigate(`/login?redirect=${location.pathname}${location.search}`);
      return;
    }
    const error = validate(content);
    if (error) {
      setMessage(error);
      return;
    }
    setMessage("");
    saveDraft({ type, content, recordedAt, path: location.pathname + location.search });
    const payload = { type, content, recordedAt: toIso(recordedAt) };
    if (editId || state?.id) {
      await apiPut(`/api/v1/dailyRecords/${editId || state?.id}`, payload);
    } else {
      await apiPost("/api/v1/dailyRecords", payload);
    }
    sessionStorage.removeItem(DRAFT_KEY);
    setContent("");
    setSaved(true);
    setMessage("保存成功");
  };

  return (
    <form
      className={`card ${isConsume ? "card--consume" : "card--intake"}`}
      onSubmit={onSubmit}
      aria-label={title}
    >
      <span className={`card__badge ${isConsume ? "card__badge--consume" : "card__badge--intake"}`}>
        {isConsume ? "Burn" : "Fuel"} · {title}
      </span>
      <h2>{title}</h2>
      <label>
        {contentLabel}
        <textarea
          value={content}
          onChange={(e) => {
            setContent(e.target.value);
            setMessage(validate(e.target.value));
            setSaved(false);
          }}
          onBlur={() => setMessage(validate(content))}
          placeholder={isConsume ? "例如：跑步 30 分钟" : "例如：鸡胸肉一份"}
        />
      </label>
      <label>
        记录时间
        <input type="datetime-local" value={recordedAt} onChange={(e) => setRecordedAt(e.target.value)} />
      </label>
      <button type="submit" className={`btn ${isConsume ? "btn-consume" : "btn-intake"} btn-block`}>
        {saveLabel}
      </button>
      {message ? <p className="flash">{message}</p> : null}
      {saved ? (
        <div className="row">
          <button
            type="button"
            className="btn btn-ghost"
            onClick={() => {
              setSaved(false);
              setMessage("");
              setContent("");
            }}
          >
            再记一条
          </button>
          <button type="button" className="btn btn-ghost" onClick={() => navigate("/calendar")}>
            回日历
          </button>
        </div>
      ) : null}
    </form>
  );
}

function BackButton() {
  const navigate = useNavigate();
  return (
    <button type="button" className="btn btn-ghost page-back" onClick={() => navigate(-1)}>
      返回
    </button>
  );
}

/**
 * 记录一级页：大按钮入口，不直接展示消耗/摄入。
 */
export function RecordPage() {
  const navigate = useNavigate();
  return (
    <div className="page">
      <p className="page__eyebrow">Today</p>
      <h1 className="page__title">记录</h1>
      <p className="page__subtitle">一条一事，绿耗红食，马上开练。</p>
      <button type="button" className="btn btn-primary btn-record-hero" onClick={() => navigate("/record")}>
        开始记录
      </button>
    </div>
  );
}

/**
 * 记录二级页：选择消耗或摄入；可携带 date 查询参数。
 */
export function RecordTypePage() {
  const navigate = useNavigate();
  const [params] = useSearchParams();
  const date = params.get("date");
  const suffix = date ? `?date=${date}` : "";
  return (
    <div className="page">
      <BackButton />
      <p className="page__eyebrow">Record</p>
      <h1 className="page__title">记什么</h1>
      <p className="page__subtitle">先选消耗或摄入，再写下这一条。</p>
      <div className="stack">
        <button type="button" className="btn btn-consume record-type-btn" onClick={() => navigate(`/record/consume${suffix}`)}>
          消耗
        </button>
        <button type="button" className="btn btn-intake record-type-btn" onClick={() => navigate(`/record/intake${suffix}`)}>
          摄入
        </button>
      </div>
    </div>
  );
}

/**
 * 记录表单页：消耗或摄入其中一种。
 */
export function RecordFormPage({ type }: { type: "CONSUME" | "INTAKE" }) {
  const defaults = useMemo(() => nowLocalInput(), []);
  const isConsume = type === "CONSUME";
  return (
    <div className="page">
      <BackButton />
      <p className="page__eyebrow">{isConsume ? "Burn" : "Fuel"}</p>
      <h1 className="page__title">{isConsume ? "当日消耗" : "当日摄入"}</h1>
      <p className="page__subtitle">{isConsume ? "绿耗一条，马上开练。" : "红食一条，记清楚就好。"}</p>
      {isConsume ? (
        <RecordForm type="CONSUME" title="当日消耗" contentLabel="消耗内容" saveLabel="保存消耗" />
      ) : (
        <RecordForm type="INTAKE" title="当日摄入" contentLabel="摄入内容" saveLabel="保存摄入" />
      )}
      <p hidden>{defaults}</p>
    </div>
  );
}
