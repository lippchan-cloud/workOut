import { FormEvent, useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";
import { apiGet, apiPut } from "../api/client";

type Profile = {
  nickname: string | null;
  heightCm: number | null;
  weightKg: number | null;
};

/**
 * 「我的」资料页：回填、保存、退出登录。
 */
export function ProfilePage() {
  const { isAuthenticated, clearToken } = useAuth();
  const navigate = useNavigate();
  const [nickname, setNickname] = useState("");
  const [heightCm, setHeightCm] = useState("");
  const [weightKg, setWeightKg] = useState("");
  const [message, setMessage] = useState("");

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

  const onLogout = () => {
    clearToken();
    navigate("/");
  };

  return (
    <div className="page">
      <p className="page__eyebrow">Athlete</p>
      <h1 className="page__title">我的</h1>
      <p className="page__subtitle">身体数据归档，保持轻量。</p>

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
        <button type="submit" className="btn btn-primary btn-block">
          保存资料
        </button>
        {message ? <p className="flash">{message}</p> : null}
      </form>

      <button type="button" className="btn btn-ghost btn-block" onClick={onLogout}>
        退出登录
      </button>
    </div>
  );
}
