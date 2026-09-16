"use client";

import { FormEvent, useState } from "react";
import { api } from "@/lib/api";

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState("");
  const [message, setMessage] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    const response = await api<{ message: string }>("/auth/forgot-password", {
      method: "POST",
      body: JSON.stringify({ email })
    });
    setMessage(response.message);
  }

  return (
    <main className="login-shell">
      <form className="login-card" onSubmit={submit}>
        <div className="brand">
          <div className="brand-mark">360</div>
          <strong>Recuperar contrasena</strong>
        </div>
        <div className="field">
          <label htmlFor="email">Correo electronico</label>
          <div className="input-row">
            <input id="email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
          </div>
        </div>
        <button className="primary" type="submit">Enviar instrucciones</button>
        {message ? <p>{message}</p> : null}
      </form>
    </main>
  );
}
