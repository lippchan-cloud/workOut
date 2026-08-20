import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { FormEvent, useState } from "react";
import { useAuth } from "../auth/AuthContext";
import { apiPost } from "../api/client";

type LoginMode = "password" | "email";
type EmailLoginStep = "email" | "code";

const CODE_SENT_HINT = "验证码已发送，请查收邮箱";

/**
 * 登录页：用户名密码或已绑定邮箱验证码；成功后写入 token 并按 redirect 回跳。
 * 邮箱登录递进：先填邮箱发码，成功后再出现验证码与登录。
 */
export function LoginPage() {
  const [searchParams] = useSearchParams();
  const redirect = searchParams.get("redirect") || "/";
  const { setSession } = useAuth();
  const navigate = useNavigate();
  const [mode, setMode] = useState<LoginMode>("password");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [email, setEmail] = useState("");
  const [code, setCode] = useState("");
  const [emailStep, setEmailStep] = useState<EmailLoginStep>("email");
  const [error, setError] = useState("");
  const [codeMessage, setCodeMessage] = useState("");

  const applySession = (data: { token: string; role?: string; username?: string }) => {
    setSession(data.token, data.role, data.username);
    navigate(redirect);
  };

  const onPasswordSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!username.trim()) {
      setError("请填写用户名");
      return;
    }
    setError("");
    try {
      const data = await apiPost<{ token: string; role?: string; username?: string }>("/api/v1/auth/login", {
        username: username.trim(),
        password,
      });
      applySession(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "登录失败");
    }
  };

  const onSendCode = async () => {
    setError("");
    setCodeMessage("");
    if (!email.trim()) {
      setError("请填写邮箱");
      return;
    }
    try {
      await apiPost("/api/v1/auth/email/sendCode", { email: email.trim(), purpose: "LOGIN" });
      setEmailStep("code");
      setCodeMessage(CODE_SENT_HINT);
    } catch (err) {
      setError(err instanceof Error ? err.message : "发送失败");
    }
  };

  const onEmailSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (emailStep !== "code") {
      await onSendCode();
      return;
    }
    if (!email.trim()) {
      setError("请填写邮箱");
      return;
    }
    if (!/^\d{4}$/.test(code.trim())) {
      setError("请填写4位验证码");
      return;
    }
    setError("");
    try {
      const data = await apiPost<{ token: string; role?: string; username?: string }>(
        "/api/v1/auth/loginByEmail",
        { email: email.trim(), code: code.trim() },
      );
      applySession(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "登录失败");
    }
  };

  return (
    <div className="auth-page" data-testid="login-page">
      <div className="auth-card">
        <div className="auth-card__brand">
          <span className="app-shell__logo-mark" aria-hidden />
          workOut
        </div>
        <p className="page__eyebrow">Sign in</p>
        <h1>登录</h1>
        <p className="page__subtitle">回到训练节奏，继续记账。</p>
        <div data-testid="login-redirect" hidden>
          {redirect}
        </div>
        <div className="auth-mode-switch">
          <button
            type="button"
            className={mode === "password" ? "btn btn-primary" : "btn btn-ghost"}
            onClick={() => {
              setMode("password");
              setError("");
              setCodeMessage("");
            }}
          >
            用户名登录
          </button>
          <button
            type="button"
            className={mode === "email" ? "btn btn-primary" : "btn btn-ghost"}
            onClick={() => {
              setMode("email");
              setError("");
              setCodeMessage("");
              setEmailStep("email");
              setCode("");
            }}
          >
            邮箱登录
          </button>
        </div>
        {mode === "password" ? (
          <form onSubmit={onPasswordSubmit}>
            <label>
              用户名
              <input
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                autoComplete="username"
              />
            </label>
            <label>
              密码
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
              />
            </label>
            {error ? <p role="alert">{error}</p> : null}
            <button type="submit" className="btn btn-primary btn-block">
              登录
            </button>
          </form>
        ) : (
          <form onSubmit={onEmailSubmit}>
            <label>
              邮箱
              <input
                value={email}
                onChange={(e) => {
                  setEmail(e.target.value);
                  if (emailStep === "code") {
                    setEmailStep("email");
                    setCode("");
                    setCodeMessage("");
                  }
                }}
                autoComplete="email"
                inputMode="email"
              />
            </label>
            {emailStep === "email" ? (
              <button type="button" className="btn btn-ghost btn-block" onClick={onSendCode}>
                发送验证码
              </button>
            ) : (
              <>
                {codeMessage ? <p className="flash">{codeMessage}</p> : null}
                <label>
                  验证码
                  <input
                    value={code}
                    onChange={(e) => setCode(e.target.value)}
                    inputMode="numeric"
                    maxLength={4}
                    placeholder="4位数字"
                  />
                </label>
                <button type="button" className="btn btn-ghost btn-block" onClick={onSendCode}>
                  发送验证码
                </button>
                <button type="submit" className="btn btn-primary btn-block">
                  登录
                </button>
              </>
            )}
            {error ? <p role="alert">{error}</p> : null}
          </form>
        )}
        <p className="auth-card__footer">
          还没有账号？ <Link to="/register">去注册</Link>
        </p>
      </div>
    </div>
  );
}
