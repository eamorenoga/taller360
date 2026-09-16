"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { AppLayout } from "@/components/AppLayout";
import { currentUser, type Me } from "@/lib/api";

const metrics = [
  "Vehiculos recibidos hoy",
  "Ordenes abiertas",
  "Ordenes retrasadas",
  "Vehiculos listos para entregar",
  "Citas del dia",
  "Ventas del dia",
  "Tecnicos trabajando",
  "Alertas de inventario"
];

export default function DashboardPage() {
  const router = useRouter();
  const [user, setUser] = useState<Me | null>(null);

  useEffect(() => {
    const me = currentUser();
    if (!me) router.push("/login");
    setUser(me);
  }, [router]);

  if (!user) return null;

  return (
    <AppLayout user={user}>
      <h1>Dashboard</h1>
      <div className="metric-grid">
        {metrics.map((metric) => (
          <article className="metric" key={metric}>
            <span>{metric}</span>
            <strong>0</strong>
          </article>
        ))}
      </div>
    </AppLayout>
  );
}
