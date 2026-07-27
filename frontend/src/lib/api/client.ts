// Empty means same-origin. In production Next.js proxies /api to the private
// backend network, so cookies never need to cross origins.
const API_URL = (process.env.NEXT_PUBLIC_API_URL ?? "").replace(/\/$/, "");

type ApiErrorPayload = {
  message?: string;
};

export class ApiError extends Error {
  constructor(
    message: string,
    readonly status: number,
  ) {
    super(message);
  }
}

function readCookie(name: string): string | undefined {
  if (typeof document === "undefined") return undefined;

  return document.cookie
    .split("; ")
    .find((item) => item.startsWith(`${name}=`))
    ?.split("=")[1];
}

export async function initializeCsrf(): Promise<void> {
  const response = await fetch(`${API_URL}/api/v1/auth/csrf`, {
    credentials: "include",
  });

  if (!response.ok) throw new ApiError("Nao foi possivel iniciar a sessao segura.", response.status);
}

async function request<T>(path: string, init: RequestInit, retry: boolean): Promise<T> {
  const method = init.method?.toUpperCase() ?? "GET";
  const headers = new Headers(init.headers);

  if (init.body && !headers.has("Content-Type")) headers.set("Content-Type", "application/json");

  if (!["GET", "HEAD", "OPTIONS"].includes(method)) {
    const csrfToken = readCookie("XSRF-TOKEN");
    if (csrfToken) headers.set("X-XSRF-TOKEN", decodeURIComponent(csrfToken));
  }

  const response = await fetch(`${API_URL}${path}`, {
    ...init,
    credentials: "include",
    headers,
  });

  if (response.status === 401 && retry && !path.startsWith("/api/v1/auth/")) {
    const csrfToken = readCookie("XSRF-TOKEN");
    const refreshHeaders = new Headers({ "Content-Type": "application/json" });
    if (csrfToken) refreshHeaders.set("X-XSRF-TOKEN", decodeURIComponent(csrfToken));
    const refresh = await fetch(`${API_URL}/api/v1/auth/refresh`, {
      method: "POST", credentials: "include", headers: refreshHeaders,
    });
    if (refresh.ok) return request<T>(path, init, false);
  }

  if (!response.ok) {
    const payload = (await response.json().catch(() => ({}))) as ApiErrorPayload;
    throw new ApiError(payload.message ?? "Nao foi possivel concluir a operacao.", response.status);
  }

  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  return request<T>(path, init, true);
}
