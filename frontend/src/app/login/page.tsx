"use client";

import { Eye, EyeOff } from "lucide-react";
import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";
import { api, saveSession, type LoginResponse } from "@/lib/api";

export default function LoginPage() {
  const router = useRouter();
  const [email, setEmail] = useState("admin@taller360.local");
  const [password, setPassword] = useState("Admin123!");
  const [rememberMe, setRememberMe] = useState(true);
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setLoading(true);
    setError("");
    try {
      const response = await api<LoginResponse>("/auth/login", {
        method: "POST",
        body: JSON.stringify({ email, password, rememberMe })
      });
      saveSession(response);
      if (!response.user.sucursalId && response.user.sucursales.length > 1) {
        router.push("/select-branch");
      } else {
        router.push("/");
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error del servidor");
    } finally {
      setLoading(false);
    }
  }

  return (
    <main className="login-shell">
      <form className="login-card" onSubmit={submit}>
        <div className="brand">
          <div className="brand-mark">360</div>
          <div>
            <strong>Taller 360</strong>
            <div>Acceso seguro</div>
          </div>
        </div>

        <div className="field">
          <label htmlFor="email">Correo electronico</label>
          <div className="input-row">
            <input id="email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
          </div>
        </div>

        <div className="field">
          <label htmlFor="password">Contrasena</label>
          <div className="input-row">
            <input
              id="password"
              type={showPassword ? "text" : "password"}
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
            />
            <button className="icon-button" type="button" onClick={() => setShowPassword((value) => !value)} aria-label="Mostrar u ocultar contrasena">
              {showPassword ? <EyeOff size={20} /> : <Eye size={20} />}
            </button>
          </div>
        </div>

        <label className="checkbox">
          <input type="checkbox" checked={rememberMe} onChange={(event) => setRememberMe(event.target.checked)} /> Recordarme
        </label>
        {error ? <p className="error">{error}</p> : null}
        <button className="primary" disabled={loading} type="submit">
          {loading ? "Ingresando..." : "Iniciar sesion"}
        </button>
        <p>
          <a href="/forgot-password">Olvidaste tu contrasena?</a>
        </p>
      </form>
    </main>
  );
}
