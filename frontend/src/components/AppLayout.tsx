"use client";

import { Bell, LogOut, UserCircle } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { allowedMenu } from "@/lib/permissions";
import type { Me } from "@/lib/api";
import { logout, switchBranch } from "@/lib/api";

export function AppLayout({ user, children }: { user: Me; children: React.ReactNode }) {
  const router = useRouter();
  const branch = user.sucursales.find((item) => item.id === user.sucursalId);
  const menu = allowedMenu(user.permisos);

  function closeSession() {
    logout();
    router.push("/login");
  }

  async function changeBranch(branchId: string) {
    await switchBranch(branchId);
    router.refresh();
    window.location.reload();
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">360</div>
          <strong>Taller 360</strong>
        </div>
        <nav>
          {menu.map(([label, href]) => (
            <Link className="nav-link" href={href} key={href}>
              {label}
            </Link>
          ))}
        </nav>
      </aside>
      <main className="main">
        <header className="topbar">
          <div>
            <strong>{user.empresa}</strong>
            <div>
              {user.sucursales.length > 1 ? (
                <select className="branch-select" value={user.sucursalId ?? ""} onChange={(event) => changeBranch(event.target.value)} aria-label="Sucursal actual">
                  <option value="" disabled>Seleccionar sucursal</option>
                  {user.sucursales.map((item) => (
                    <option key={item.id} value={item.id}>{item.nombre}</option>
                  ))}
                </select>
              ) : (
                branch?.nombre ?? "Sin sucursal seleccionada"
              )}
            </div>
          </div>
          <div className="topbar-actions">
            <Bell size={20} aria-label="Notificaciones" />
            <UserCircle size={20} aria-label="Perfil" />
            <span>{user.nombre} {user.apellido}</span>
            <button className="icon-button" onClick={closeSession} title="Cerrar sesion" aria-label="Cerrar sesion">
              <LogOut size={20} />
            </button>
          </div>
        </header>
        <section className="content">{children}</section>
      </main>
    </div>
  );
}
