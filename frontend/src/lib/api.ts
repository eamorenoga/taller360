export type BranchOption = { id: string; nombre: string };
export type Me = {
  id: string;
  empresaId: string;
  empresa: string;
  sucursalId: string | null;
  nombre: string;
  apellido: string;
  email: string;
  sucursales: BranchOption[];
  roles: string[];
  permisos: string[];
};

export type LoginResponse = {
  accessToken: string;
  refreshToken: string;
  expiresAt: string;
  user: Me;
};

const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080/api/v1";

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;
  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers
    }
  });
  if (!response.ok) {
    const body = await response.json().catch(() => ({ message: "Error del servidor" }));
    throw new Error(body.message ?? "Error del servidor");
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json();
}

export function saveSession(response: LoginResponse) {
  localStorage.setItem("accessToken", response.accessToken);
  localStorage.setItem("refreshToken", response.refreshToken);
  localStorage.setItem("me", JSON.stringify(response.user));
}

export function currentUser(): Me | null {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem("me");
  return raw ? (JSON.parse(raw) as Me) : null;
}

export function logout() {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("me");
}
