import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { FormEvent, useState } from "react";
import { useAuth } from "../auth/AuthContext";
import { apiPost } from "../api/client";

/**
 * 登录页：校验空用户名；成功后写入 token 并按 redirect 回跳。
 */
export function LoginPage() {
  const [searchParams] = useSearchParams();
  const redirect = searchParams.get("redirect") || "/";
  const { setSession } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!username.trim()) {
      setError("请填写用户名");
      return;
    }
    setError("");
    try {
      const data = await apiPost<{ token: string; role?: string }>("/api/v1/auth/login", {
        username: username.trim(),
        password,
      });
      setSession(data.token, data.role);
      navigate(redirect);
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
        <form onSubmit={onSubmit}>
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
        <p className="auth-card__footer">
          还没有账号？ <Link to="/register">去注册</Link>
        </p>
      </div>
    </div>
  );
}
