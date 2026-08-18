import { FormEvent, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { apiPost } from "../api/client";

function nowLocalInput(): string {
  const now = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}T${pad(now.getHours())}:${pad(now.getMinutes())}`;
}

function toIso(local: string): string {
  return new Date(local).toISOString();
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
  const [content, setContent] = useState("");
  const [recordedAt, setRecordedAt] = useState(nowLocalInput);
  const [message, setMessage] = useState("");

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!isAuthenticated) {
      navigate("/login?redirect=/");
      return;
    }
    if (!content.trim()) {
      setMessage("请填写内容");
      return;
    }
    setMessage("");
    await apiPost("/api/v1/dailyRecords", {
      type,
      content,
      recordedAt: toIso(recordedAt),
    });
    setContent("");
    setMessage("保存成功");
  };

  return (
    <form onSubmit={onSubmit} aria-label={title}>
      <h2>{title}</h2>
      <label>
        {contentLabel}
        <textarea value={content} onChange={(e) => setContent(e.target.value)} />
      </label>
      <label>
        记录时间
        <input type="datetime-local" value={recordedAt} onChange={(e) => setRecordedAt(e.target.value)} />
      </label>
      <button type="submit">{saveLabel}</button>
      {message ? <p>{message}</p> : null}
    </form>
  );
}

/**
 * 记录页：消耗 / 摄入两表单。
 */
export function RecordPage() {
  const defaults = useMemo(() => nowLocalInput(), []);
  return (
    <div>
      <h1>记录</h1>
      <RecordForm type="CONSUME" title="当日消耗" contentLabel="消耗内容" saveLabel="保存消耗" />
      <RecordForm type="INTAKE" title="当日摄入" contentLabel="摄入内容" saveLabel="保存摄入" />
      <p hidden>{defaults}</p>
    </div>
  );
}
