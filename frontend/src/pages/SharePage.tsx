import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { formatYmd } from "../calendar/week";

/**
 * 日历分享二级页：按筛选 query 创建链接并支持复制、返回日历。
 */
export function SharePage() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [url, setUrl] = useState("");
  const [message, setMessage] = useState("");
  const [copyHint, setCopyHint] = useState("");
  const query = shareQuery(searchParams);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate(`/login?redirect=/calendar/share?${query}`);
      return;
    }
    let cancelled = false;
    const token = localStorage.getItem("workout_token");
    fetch(`/api/v1/shareReports?${query}`, {
      method: "POST",
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    })
      .then(async (response) => {
        if (response.status === 401) {
          localStorage.removeItem("workout_token");
          navigate("/login?redirect=/calendar");
          return;
        }
        const body = await response.json();
        if (!response.ok || body.code !== 200) {
          if (!cancelled) {
            setMessage(body.msg || "分享失败");
          }
          if (body.msg === "请先填写身高和体重") {
            navigate("/profile/body");
          }
          return;
        }
        if (!cancelled) {
          setUrl(body.data.url);
        }
      })
      .catch(() => {
        if (!cancelled) {
          setMessage("分享失败");
        }
      });
    return () => {
      cancelled = true;
    };
  }, [isAuthenticated, navigate, query]);

  const onCopy = async () => {
    try {
      await navigator.clipboard.writeText(url);
      setCopyHint("已复制");
    } catch {
      setCopyHint("请手动复制链接");
    }
  };

  return (
    <div className="page">
      <p className="page__eyebrow">Share</p>
      <h1 className="page__title">分享报告</h1>
      <p className="page__subtitle">只读链接 · 当前筛选范围</p>
      <div className="card stack">
        {url ? (
          <>
            <p className="share-url">{url}</p>
            <button type="button" className="btn btn-ghost btn-block" onClick={onCopy}>
              复制
            </button>
          </>
        ) : (
          <p className="empty-state">{message || "正在生成分享链接…"}</p>
        )}
        {copyHint ? <p className="flash">{copyHint}</p> : null}
        <button type="button" className="btn btn-ghost btn-block" onClick={() => navigate(`/calendar?${query}`)}>
          返回日历
        </button>
      </div>
    </div>
  );
}

function shareQuery(searchParams: URLSearchParams): string {
  const yearMonth = searchParams.get("yearMonth");
  const from = searchParams.get("from");
  const to = searchParams.get("to");
  const date = searchParams.get("date");
  if (yearMonth) {
    return `yearMonth=${yearMonth}`;
  }
  if (from && to) {
    return `from=${from}&to=${to}`;
  }
  if (date) {
    return `date=${date}`;
  }
  return `date=${formatYmd(new Date())}`;
}
