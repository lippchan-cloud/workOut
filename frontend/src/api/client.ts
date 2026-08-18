type ApiEnvelope<T> = {
  code: number;
  msg: string;
  data: T;
};

function handleUnauthorized(): never {
  localStorage.removeItem("workout_token");
  const redirect = encodeURIComponent(window.location.pathname || "/");
  window.location.assign(`/login?redirect=${redirect}`);
  throw new Error("未登录或登录已过期");
}

/**
 * 统一 POST：业务字段包在 request 内；401 时清 token 并跳转登录。
 */
export async function apiPost<T>(path: string, request: unknown): Promise<T> {
  const token = localStorage.getItem("workout_token");
  const response = await fetch(path, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify({ request }),
  });
  if (response.status === 401) {
    handleUnauthorized();
  }
  const body = (await response.json()) as ApiEnvelope<T>;
  if (!response.ok || body.code !== 200) {
    throw new Error(body.msg || "请求失败");
  }
  return body.data;
}

/**
 * 统一 GET；401 清 token 并跳转登录。
 */
export async function apiGet<T>(path: string): Promise<T> {
  const token = localStorage.getItem("workout_token");
  const response = await fetch(path, {
    headers: {
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });
  if (response.status === 401) {
    handleUnauthorized();
  }
  const body = (await response.json()) as ApiEnvelope<T>;
  if (!response.ok || body.code !== 200) {
    throw new Error(body.msg || "请求失败");
  }
  return body.data;
}

/**
 * 统一 PUT；401 清 token 并跳转登录。
 */
export async function apiPut<T>(path: string, request: unknown): Promise<T> {
  const token = localStorage.getItem("workout_token");
  const response = await fetch(path, {
    method: "PUT",
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    body: JSON.stringify({ request }),
  });
  if (response.status === 401) {
    handleUnauthorized();
  }
  const body = (await response.json()) as ApiEnvelope<T>;
  if (!response.ok || body.code !== 200) {
    throw new Error(body.msg || "请求失败");
  }
  return body.data;
}
