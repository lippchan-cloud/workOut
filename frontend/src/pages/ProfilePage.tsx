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
    <div>
      <h1>我的</h1>
      <form onSubmit={onSubmit}>
        <label>
          昵称
          <input value={nickname} onChange={(e) => setNickname(e.target.value)} />
        </label>
        <label>
          身高
          <input value={heightCm} onChange={(e) => setHeightCm(e.target.value)} />
        </label>
        <label>
          体重
          <input value={weightKg} onChange={(e) => setWeightKg(e.target.value)} />
        </label>
        <button type="submit">保存资料</button>
      </form>
      {message ? <p>{message}</p> : null}
      <button type="button" onClick={onLogout}>
        退出登录
      </button>
    </div>
  );
}
