"use client";

import { useEffect, useMemo, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { AppLayout } from "@/components/AppLayout";
import { currentUser, type Me } from "@/lib/api";

const titles: Record<string, string> = {
  clientes: "Clientes",
  vehiculos: "Vehiculos",
  citas: "Citas",
  recepcion: "Recepcion",
  ordenes: "Ordenes de trabajo",
  kanban: "Kanban",
  tecnicos: "Tecnicos",
  inventario: "Inventario",
  compras: "Compras",
  facturacion: "Facturacion",
  caja: "Caja",
  reportes: "Reportes",
  configuracion: "Configuracion"
};

export default function ModulePlaceholderPage() {
  const params = useParams<{ module: string }>();
  const router = useRouter();
  const [user, setUser] = useState<Me | null>(null);
  const title = useMemo(() => titles[params.module] ?? "Modulo", [params.module]);

  useEffect(() => {
    const me = currentUser();
    if (!me) router.push("/login");
    setUser(me);
  }, [router]);

  if (!user) return null;

  return (
    <AppLayout user={user}>
      <h1>{title}</h1>
      <p>Modulo preparado para implementacion posterior.</p>
    </AppLayout>
  );
}
