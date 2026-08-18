import { FormEvent, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { apiDelete, apiGet, apiPut } from "../api/client";

type Profile = {
  nickname: string | null;
  heightCm: number | null;
  weightKg: number | null;
};

/**
 * 「我的」资料页：账号区（改密/注销/退出）与身体数据分开。
 */
export function ProfilePage() {
  const { isAuthenticated, isAdmin, clearToken } = useAuth();
  const navigate = useNavigate();
  const [nickname, setNickname] = useState("");
  const [heightCm, setHeightCm] = useState("");
  const [weightKg, setWeightKg] = useState("");
  const [message, setMessage] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [passwordMessage, setPasswordMessage] = useState("");

  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login?redirect=/profile");
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
    });
    setMessage("保存成功");
  };

  const onChangePassword = async (event: FormEvent) => {
    event.preventDefault();
    setPasswordMessage("");
    await apiPut("/api/v1/auth/password", {
      currentPassword,
      newPassword,
    });
    setCurrentPassword("");
    setNewPassword("");
    setPasswordMessage("密码已更新");
  };

  const onLogout = () => {
    clearToken();
    navigate("/");
  };

  const onDeleteAccount = async () => {
    if (!window.confirm("确认注销账号并删除本人全部数据？此操作不可恢复。")) {
      return;
    }
    await apiDelete("/api/v1/auth/me");
    clearToken();
    navigate("/");
  };

  return (
    <div className="page">
      <p className="page__eyebrow">Athlete</p>
      <h1 className="page__title">我的</h1>
      <p className="page__subtitle">账号安全与身体数据分开归档。</p>

      <section className="card stack">
        <h2>账号</h2>
        {isAdmin ? <Link to="/cms">后台管理</Link> : null}
        <form onSubmit={onChangePassword}>
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
        <button type="button" className="btn btn-ghost btn-block" onClick={onLogout}>
          退出登录
        </button>
        <button type="button" className="btn btn-ghost btn-block" onClick={onDeleteAccount}>
          注销账号
        </button>
      </section>

      <form className="card" onSubmit={onSubmit}>
        <h2>身体数据</h2>
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
        <button type="submit" className="btn btn-primary btn-block">
          保存资料
        </button>
        {message ? <p className="flash">{message}</p> : null}
      </form>
    </div>
  );
}
