import { Link, useNavigate } from "react-router-dom";
import { FormEvent, useState } from "react";
import { useAuth } from "../auth/AuthContext";
import { apiPost } from "../api/client";

/**
 * 注册页：成功后写入 token 并进入记录页。
 */
export function RegisterPage() {
  const { setSession } = useAuth();
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const onSubmit = async (event: FormEvent) => {
    event.preventDefault();
    if (!username.trim()) {
      setError("请填写合法用户名");
      return;
    }
    setError("");
    try {
      const data = await apiPost<{ token: string; role?: string }>("/api/v1/auth/register", {
        username: username.trim(),
        password,
      });
      setSession(data.token, data.role);
      navigate("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "注册失败");
    }
  };

  return (
    <div className="auth-page" data-testid="register-page">
      <div className="auth-card">
        <div className="auth-card__brand">
          <span className="app-shell__logo-mark" aria-hidden />
          workOut
        </div>
        <p className="page__eyebrow">Join</p>
        <h1>注册</h1>
        <p className="page__subtitle">30 秒建号，马上开始记录。</p>
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
              autoComplete="new-password"
            />
          </label>
          {error ? <p role="alert">{error}</p> : null}
          <button type="submit" className="btn btn-primary btn-block">
            注册
          </button>
        </form>
        <p className="auth-card__footer">
          已有账号？ <Link to="/login">去登录</Link>
        </p>
      </div>
    </div>
  );
}
