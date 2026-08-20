import { FormEvent, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { apiDelete, apiGet, apiPost, apiPut } from "../api/client";
import { ConfirmDialog } from "../components/ConfirmDialog";
import { GrowthCurvePanel } from "./GrowthCurvePanel";

type Profile = {
  nickname: string | null;
  heightCm: number | null;
  weightKg: number | null;
};

type AuthMe = {
  username: string;
  email: string | null;
  role?: string;
};

type ShareItem = {
  id: string;
  from: string;
  to: string;
  createdAt: string;
};

type ConfirmSpec = {
  title: string;
  message: string;
  onConfirm: () => void | Promise<void>;
};

function BackToProfile() {
  const navigate = useNavigate();
  return (
    <button type="button" className="btn btn-ghost page-back" onClick={() => navigate("/profile")}>
      返回
    </button>
  );
}

/**
 * 「我的」二级选项层：身体资料 / 账号安全 / 报告记录 / 退出登录。不摊开表单。
 */
export function ProfilePage() {
  const { isAuthenticated, clearToken } = useAuth();
  const navigate = useNavigate();
  const [confirm, setConfirm] = useState<ConfirmSpec | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login?redirect=/profile");
    }
  }, [isAuthenticated, navigate]);

  const onLogout = () => {
    setConfirm({
      title: "退出登录",
      message: "确认退出当前账号？",
      onConfirm: () => {
        clearToken();
        setConfirm(null);
        navigate("/");
      },
    });
  };

  return (
    <div className="page">
      <p className="page__eyebrow">Athlete</p>
      <h1 className="page__title">我的</h1>
      <p className="page__subtitle">先选一项，再填写或管理账号。</p>
      <div className="card stack">
        <button type="button" className="btn btn-primary btn-block" onClick={() => navigate("/profile/body")}>
          身体资料
        </button>
        <button type="button" className="btn btn-ghost btn-block" onClick={() => navigate("/profile/account")}>
          账号安全
        </button>
        <button type="button" className="btn btn-ghost btn-block" onClick={() => navigate("/profile/reports")}>
          报告记录
        </button>
        <button type="button" className="btn btn-text btn-block" onClick={onLogout}>
          退出登录
        </button>
      </div>
      <ConfirmDialog
        open={Boolean(confirm)}
        title={confirm?.title ?? ""}
        message={confirm?.message ?? ""}
        onCancel={() => setConfirm(null)}
        onConfirm={() => {
          void confirm?.onConfirm();
        }}
      />
    </div>
  );
}

/**
 * 「我的」三级：昵称 / 身高 / 体重 / 资料真实日期；下方成长曲线。
 */
export function ProfileBodyPage() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [nickname, setNickname] = useState("");
  const [heightCm, setHeightCm] = useState("");
  const [weightKg, setWeightKg] = useState("");
  const [changedAt, setChangedAt] = useState(nowDatetimeLocal);
  const [message, setMessage] = useState("");

  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login?redirect=/profile/body");
      return;
    }
    apiGet<Profile>("/api/v1/profile").then((data) => {
      setNickname(data.nickname ?? "");
      setHeightCm(data.heightCm == null ? "" : String(data.heightCm));
      setWeightKg(data.weightKg == null ? "" : String(data.weightKg));
    });
  }, [isAuthenticated, navigate]);

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    await apiPut<Profile>("/api/v1/profile", {
      nickname: nickname || null,
      heightCm: heightCm === "" ? null : Number(heightCm),
      weightKg: weightKg === "" ? null : Number(weightKg),
      changedAt: new Date(changedAt).toISOString(),
    });
    setMessage("保存成功");
  };

  return (
    <div className="page">
      <BackToProfile />
      <p className="page__eyebrow">Body</p>
      <h1 className="page__title">身体资料</h1>
      <p className="page__subtitle">记下当时的身高体重，不算 BMI。</p>
      <form className="card" onSubmit={onSubmit}>
        <label>
          昵称
          <input value={nickname} onChange={(e) => setNickname(e.target.value)} placeholder="怎么称呼你" />
        </label>
        <label>
          身高 (cm)
          <input value={heightCm} onChange={(e) => setHeightCm(e.target.value)} placeholder="例如 175" />
        </label>
        <label>
          体重 (kg)
          <input value={weightKg} onChange={(e) => setWeightKg(e.target.value)} placeholder="例如 70" />
        </label>
        <label>
          资料真实日期
          <input
            type="datetime-local"
            aria-label="资料真实日期"
            value={changedAt}
            onChange={(e) => setChangedAt(e.target.value)}
          />
        </label>
        <button type="submit" className="btn btn-primary btn-block">
          保存资料
        </button>
        {message ? <p className="flash">{message}</p> : null}
      </form>
      <GrowthCurvePanel />
    </div>
  );
}

function nowDatetimeLocal(): string {
  const date = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

type EmailPanelStep = "entry" | "email" | "code";

const CODE_SENT_HINT = "验证码已发送，请查收邮箱";

/**
 * 「我的」三级：改密 / 邮箱绑定解绑 / 注销分卡；邮箱递进，不与改密注销挤成一块。
 * ADMIN 可见 CMS。
 */
export function ProfileAccountPage() {
  const { isAuthenticated, isAdmin, clearToken } = useAuth();
  const navigate = useNavigate();
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [passwordMessage, setPasswordMessage] = useState("");
  const [email, setEmail] = useState<string | null>(null);
  const [bindEmail, setBindEmail] = useState("");
  const [bindCode, setBindCode] = useState("");
  const [unbindCode, setUnbindCode] = useState("");
  const [emailStep, setEmailStep] = useState<EmailPanelStep>("entry");
  const [emailMessage, setEmailMessage] = useState("");
  const [confirm, setConfirm] = useState<ConfirmSpec | null>(null);

  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login?redirect=/profile/account");
      return;
    }
    apiGet<AuthMe>("/api/v1/auth/me")
      .then((data) => {
        setEmail(data.email ?? null);
      })
      .catch(() => {
        setEmail(null);
      });
  }, [isAuthenticated, navigate]);

  const resetEmailPanel = () => {
    setEmailStep("entry");
    setBindEmail("");
    setBindCode("");
    setUnbindCode("");
    setEmailMessage("");
  };

  const onChangePassword = (event: FormEvent) => {
    event.preventDefault();
    setPasswordMessage("");
    setConfirm({
      title: "修改密码",
      message: "确认修改登录密码？",
      onConfirm: async () => {
        setConfirm(null);
        await apiPut("/api/v1/auth/password", {
          currentPassword,
          newPassword,
        });
        setCurrentPassword("");
        setNewPassword("");
        setPasswordMessage("密码已更新");
      },
    });
  };

  const onSendBindCode = async () => {
    if (!bindEmail.trim()) {
      setEmailMessage("请填写邮箱");
      return;
    }
    setEmailMessage("");
    try {
      await apiPost("/api/v1/auth/email/sendCode", { email: bindEmail.trim(), purpose: "BIND" });
      setEmailStep("code");
      setEmailMessage(CODE_SENT_HINT);
    } catch (err) {
      setEmailMessage(err instanceof Error ? err.message : "发送失败");
    }
  };

  const onBindEmail = (event: FormEvent) => {
    event.preventDefault();
    if (!/^\d{4}$/.test(bindCode.trim())) {
      setEmailMessage("请填写4位验证码");
      return;
    }
    setConfirm({
      title: "绑定邮箱",
      message: `确认将邮箱绑定为 ${bindEmail.trim()}？绑定后可用邮箱验证码登录。`,
      onConfirm: async () => {
        setConfirm(null);
        await apiPost("/api/v1/auth/email/bind", { email: bindEmail.trim(), code: bindCode.trim() });
        setEmail(bindEmail.trim().toLowerCase());
        setBindEmail("");
        setBindCode("");
        setEmailStep("entry");
        setEmailMessage("邮箱已绑定");
      },
    });
  };

  const onStartUnbind = async () => {
    if (!email) {
      return;
    }
    setEmailMessage("");
    try {
      await apiPost("/api/v1/auth/email/sendCode", { email, purpose: "UNBIND" });
      setEmailStep("code");
      setEmailMessage(CODE_SENT_HINT);
    } catch (err) {
      setEmailMessage(err instanceof Error ? err.message : "发送失败");
    }
  };

  const onUnbindEmail = (event: FormEvent) => {
    event.preventDefault();
    if (!/^\d{4}$/.test(unbindCode.trim())) {
      setEmailMessage("请填写4位验证码");
      return;
    }
    setConfirm({
      title: "解绑邮箱",
      message: "确认解绑邮箱？解绑后将无法用该邮箱验证码登录。",
      onConfirm: async () => {
        setConfirm(null);
        await apiPost("/api/v1/auth/email/unbind", { code: unbindCode.trim() });
        setEmail(null);
        setUnbindCode("");
        setEmailStep("entry");
        setEmailMessage("邮箱已解绑");
      },
    });
  };

  const onDeleteAccount = () => {
    setConfirm({
      title: "注销账号",
      message: "确认注销账号并删除本人全部数据？此操作不可恢复。",
      onConfirm: async () => {
        setConfirm(null);
        await apiDelete("/api/v1/auth/me");
        clearToken();
        navigate("/");
      },
    });
  };

  return (
    <div className="page">
      <BackToProfile />
      <p className="page__eyebrow">Account</p>
      <h1 className="page__title">账号安全</h1>
      <p className="page__subtitle">先选一项，再填写。</p>
      {isAdmin ? (
        <p>
          <Link to="/cms">后台管理</Link>
        </p>
      ) : null}
      <form className="card" onSubmit={onChangePassword}>
        <label>
          当前密码
          <input
            type="password"
            value={currentPassword}
            onChange={(e) => setCurrentPassword(e.target.value)}
            autoComplete="current-password"
          />
        </label>
        <label>
          新密码
          <input
            type="password"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            autoComplete="new-password"
          />
        </label>
        <button type="submit" className="btn btn-primary btn-block">
          修改密码
        </button>
        {passwordMessage ? <p className="flash">{passwordMessage}</p> : null}
      </form>
      <section className="card stack">
        {email ? (
          emailStep === "entry" ? (
            <>
              <p>
                已绑定邮箱：<strong>{email}</strong>
              </p>
              {emailMessage ? <p className="flash">{emailMessage}</p> : null}
              <button type="button" className="btn btn-ghost btn-block" onClick={onStartUnbind}>
                解绑邮箱
              </button>
            </>
          ) : (
            <form onSubmit={onUnbindEmail}>
              <p>
                已绑定邮箱：<strong>{email}</strong>
              </p>
              {emailMessage ? <p className={emailMessage === CODE_SENT_HINT ? "flash" : "flash flash--error"}>{emailMessage}</p> : null}
              <label>
                验证码
                <input
                  value={unbindCode}
                  onChange={(e) => setUnbindCode(e.target.value)}
                  inputMode="numeric"
                  maxLength={4}
                  placeholder="4位数字"
                />
              </label>
              <button type="submit" className="btn btn-ghost btn-block">
                确认解绑
              </button>
              <button type="button" className="btn btn-ghost btn-block" onClick={resetEmailPanel}>
                取消
              </button>
            </form>
          )
        ) : emailStep === "entry" ? (
          <>
            {emailMessage ? <p className="flash">{emailMessage}</p> : null}
            <button type="button" className="btn btn-ghost btn-block" onClick={() => setEmailStep("email")}>
              绑定邮箱
            </button>
          </>
        ) : (
          <form onSubmit={onBindEmail}>
            <label>
              邮箱
              <input
                value={bindEmail}
                onChange={(e) => setBindEmail(e.target.value)}
                autoComplete="email"
                placeholder="name@example.com"
              />
            </label>
            {emailStep === "code" ? (
              <>
                {emailMessage ? (
                  <p className={emailMessage === CODE_SENT_HINT ? "flash" : "flash flash--error"}>{emailMessage}</p>
                ) : null}
                <label>
                  验证码
                  <input
                    value={bindCode}
                    onChange={(e) => setBindCode(e.target.value)}
                    inputMode="numeric"
                    maxLength={4}
                    placeholder="4位数字"
                  />
                </label>
                <button type="submit" className="btn btn-ghost btn-block">
                  确认绑定
                </button>
              </>
            ) : (
              <>
                {emailMessage ? <p className="flash flash--error">{emailMessage}</p> : null}
                <button type="button" className="btn btn-ghost btn-block" onClick={onSendBindCode}>
                  发送验证码
                </button>
              </>
            )}
            <button type="button" className="btn btn-ghost btn-block" onClick={resetEmailPanel}>
              取消
            </button>
          </form>
        )}
      </section>
      <section className="card">
        <button type="button" className="btn btn-ghost btn-block" onClick={onDeleteAccount}>
          注销账号
        </button>
      </section>
      <ConfirmDialog
        open={Boolean(confirm)}
        title={confirm?.title ?? ""}
        message={confirm?.message ?? ""}
        onCancel={() => setConfirm(null)}
        onConfirm={() => {
          void confirm?.onConfirm();
        }}
      />
    </div>
  );
}

/**
 * 「我的」三级：当前用户已创建的分享报告列表。
 */
export function ProfileReportsPage() {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [reports, setReports] = useState<ShareItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login?redirect=/profile/reports");
      return;
    }
    apiGet<{ list: ShareItem[] }>("/api/v1/shareReports")
      .then((data) => setReports(data.list ?? []))
      .catch((err) => setError(err instanceof Error ? err.message : "加载失败"))
      .finally(() => setLoading(false));
  }, [isAuthenticated, navigate]);

  return (
    <div className="page">
      <BackToProfile />
      <p className="page__eyebrow">Reports</p>
      <h1 className="page__title">报告记录</h1>
      <p className="page__subtitle">打开自己生成过的分享报告。</p>
      {loading ? <p className="empty-state">加载中…</p> : null}
      {!loading && error ? <p role="alert">{error}</p> : null}
      {!loading && !error && reports.length === 0 ? <p className="empty-state">暂无分享报告</p> : null}
      {!loading && reports.length > 0 ? (
        <div className="card stack">
          {reports.map((row) => (
            <Link key={row.id} className="report-record-link" to={`/report/${row.id}`}>
              <span>
                {row.from} ~ {row.to}
              </span>
              <span className="report-record-link__meta">{row.createdAt}</span>
            </Link>
          ))}
        </div>
      ) : null}
    </div>
  );
}
