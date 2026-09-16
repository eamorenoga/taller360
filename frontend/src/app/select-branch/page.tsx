"use client";

import { FormEvent, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { api, currentUser, saveSession, type LoginResponse } from "@/lib/api";

export default function SelectBranchPage() {
  const router = useRouter();
  const user = useMemo(() => currentUser(), []);
  const [branchId, setBranchId] = useState(user?.sucursales[0]?.id ?? "");
  const [error, setError] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    const refreshToken = localStorage.getItem("refreshToken");
    try {
      const response = await api<LoginResponse>("/auth/refresh", {
        method: "POST",
        body: JSON.stringify({ refreshToken, branchId })
      });
      saveSession(response);
      router.push("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error del servidor");
    }
  }

  return (
    <main className="login-shell">
      <form className="login-card" onSubmit={submit}>
        <div className="brand">
          <div className="brand-mark">360</div>
          <strong>Seleccionar sucursal</strong>
        </div>
        <div className="field">
          <label htmlFor="branch">Sucursal</label>
          <div className="input-row">
            <select id="branch" value={branchId} onChange={(event) => setBranchId(event.target.value)}>
              {user?.sucursales.map((branch) => (
                <option key={branch.id} value={branch.id}>
                  {branch.nombre}
                </option>
              ))}
            </select>
          </div>
        </div>
        {error ? <p className="error">{error}</p> : null}
        <button className="primary" type="submit">Continuar</button>
      </form>
    </main>
  );
}
