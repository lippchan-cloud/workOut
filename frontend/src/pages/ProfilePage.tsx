import { FormEvent, useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { apiDelete, apiGet, apiPut } from "../api/client";
import { GrowthCurvePanel } from "./GrowthCurvePanel";

type Profile = {
  nickname: string | null;
  heightCm: number | null;
  weightKg: number | null;
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
 * 「我的」二级选项层：身体资料 / 账号安全 / 退出登录。不摊开表单。
 */
export function ProfilePage() {
  const { isAuthenticated, clearToken } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login?redirect=/profile");
    }
  }, [isAuthenticated, navigate]);

  const onLogout = () => {
    clearToken();
    navigate("/");
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
        <button type="button" className="btn btn-text btn-block" onClick={onLogout}>
          退出登录
        </button>
      </div>
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

/**
 * 「我的」三级：改密 / 注销；ADMIN 可见 CMS。
 */
export function ProfileAccountPage() {
  const { isAuthenticated, isAdmin, clearToken } = useAuth();
  const navigate = useNavigate();
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [passwordMessage, setPasswordMessage] = useState("");

  useEffect(() => {
    if (!isAuthenticated) {
      navigate("/login?redirect=/profile/account");
    }
  }, [isAuthenticated, navigate]);

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
      <BackToProfile />
      <p className="page__eyebrow">Account</p>
      <h1 className="page__title">账号安全</h1>
      <p className="page__subtitle">改密、注销与管理入口。</p>
      <section className="card stack">
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
        <button type="button" className="btn btn-ghost btn-block" onClick={onDeleteAccount}>
          注销账号
        </button>
      </section>
    </div>
  );
}
