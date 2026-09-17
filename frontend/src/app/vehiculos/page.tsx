"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { AppLayout } from "@/components/AppLayout";
import { api, currentUser, type Client, type Me, type Vehicle, type Vehicle360 } from "@/lib/api";

export default function VehiclesPage() {
  const router = useRouter();
  const [user, setUser] = useState<Me | null>(null);
  const [query, setQuery] = useState("");
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [selected, setSelected] = useState<Vehicle360 | null>(null);
  const [message, setMessage] = useState("");

  useEffect(() => {
    const me = currentUser();
    if (!me) router.push("/login");
    setUser(me);
    void load();
  }, [router]);

  async function load() {
    const params = query ? `?q=${encodeURIComponent(query)}` : "";
    const [vehicleList, clientList] = await Promise.all([
      api<Vehicle[]>(`/vehicles${params}`),
      api<Client[]>("/clients")
    ]);
    setVehicles(vehicleList);
    setClients(clientList);
    if (vehicleList.length > 0 && !selected) {
      await openVehicle(vehicleList[0].id);
    }
  }

  async function openVehicle(id: string) {
    setSelected(await api<Vehicle360>(`/vehicles/${id}`));
  }

  async function createVehicle(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const created = await api<Vehicle>("/vehicles", {
      method: "POST",
      body: JSON.stringify({
        ...Object.fromEntries(form),
        kilometraje: Number(form.get("kilometraje") ?? 0),
        anio: Number(form.get("anio") || 0) || null,
        estado: "ACTIVO"
      })
    });
    event.currentTarget.reset();
    setMessage("Vehiculo creado");
    await load();
    await openVehicle(created.id);
  }

  if (!user) return null;

  return (
    <AppLayout user={user}>
      <h1>Vehiculos</h1>
      <section className="panel stack">
        <div className="field">
          <label htmlFor="q">Buscar vehiculo</label>
          <div className="input-row">
            <input id="q" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Placa, VIN, marca, modelo o cliente" />
          </div>
        </div>
        <button className="secondary" onClick={load}>Buscar</button>
      </section>
      {message ? <p className="muted">{message}</p> : null}
      <div className="settings-grid">
        <section className="panel">
          <h2>Listado</h2>
          {vehicles.length === 0 ? (
            <p className="muted">No hay vehiculos para los filtros actuales.</p>
          ) : (
            <div className="stack">
              {vehicles.map((vehicle) => (
                <button className="client-row" key={vehicle.id} onClick={() => openVehicle(vehicle.id)}>
                  <strong>{vehicle.placa}</strong>
                  <span>{vehicle.marca} {vehicle.modelo} · {vehicle.cliente}</span>
                </button>
              ))}
            </div>
          )}
        </section>
        <form className="panel stack" onSubmit={createVehicle}>
          <h2>Nuevo vehiculo</h2>
          <div className="field">
            <label htmlFor="clienteId">Cliente</label>
            <div className="input-row">
              <select id="clienteId" name="clienteId" required defaultValue="">
                <option value="" disabled>Seleccionar cliente</option>
                {clients.map((client) => <option key={client.id} value={client.id}>{client.nombre}</option>)}
              </select>
            </div>
          </div>
          <div className="split">
            <Field name="placa" label="Placa" />
            <Field name="vin" label="VIN" />
            <Field name="marca" label="Marca" />
            <Field name="modelo" label="Modelo" />
            <Field name="version" label="Version" />
            <Field name="anio" label="Ano" />
            <Field name="motor" label="Motor" />
            <Field name="combustible" label="Combustible" />
            <Field name="transmision" label="Transmision" />
            <Field name="color" label="Color" />
            <Field name="kilometraje" label="Kilometraje" defaultValue="0" />
            <Field name="notas" label="Notas" />
          </div>
          <button className="primary" type="submit">Crear vehiculo</button>
        </form>
      </div>
      {selected ? <Vehicle360Panel data={selected} /> : null}
    </AppLayout>
  );
}

function Vehicle360Panel({ data }: { data: Vehicle360 }) {
  const v = data.vehiculo;
  return (
    <section className="panel stack">
      <h2>Ficha tecnica</h2>
      <div className="split">
        <Info title="Identificacion" rows={[
          ["Placa", v.placa],
          ["VIN", v.vin ?? "-"],
          ["Cliente", v.cliente],
          ["Estado", v.estado]
        ]} />
        <Info title="Datos tecnicos" rows={[
          ["Marca", v.marca],
          ["Modelo", v.modelo],
          ["Version", v.version ?? "-"],
          ["Ano", v.anio ? String(v.anio) : "-"],
          ["Motor", v.motor ?? "-"],
          ["Combustible", v.combustible ?? "-"],
          ["Transmision", v.transmision ?? "-"],
          ["Color", v.color ?? "-"],
          ["Kilometraje", String(v.kilometraje)]
        ]} />
      </div>
      <Related title="Documentos" rows={data.documentos.map((item) => `${item.tipo} · ${item.nombreArchivo}`)} empty="Sin documentos registrados." />
      <Related title="Fotos" rows={data.fotos.map((item) => item.descripcion ?? item.url)} empty="Sin fotos registradas." />
      <Related title="Garantias" rows={data.garantias.map((item) => `${item.tipo} · ${item.estado}`)} empty="Sin garantias registradas." />
      <Related title="Timeline" rows={data.timeline.map((item) => `${item.modulo} · ${item.titulo}`)} empty="Sin eventos de historial." />
    </section>
  );
}

function Field({ name, label, defaultValue = "" }: { name: string; label: string; defaultValue?: string }) {
  return (
    <div className="field">
      <label htmlFor={name}>{label}</label>
      <div className="input-row">
        <input id={name} name={name} defaultValue={defaultValue} />
      </div>
    </div>
  );
}

function Info({ title, rows }: { title: string; rows: string[][] }) {
  return (
    <article className="panel">
      <h3>{title}</h3>
      {rows.map(([label, value]) => <p key={label}><strong>{label}:</strong> {value}</p>)}
    </article>
  );
}

function Related({ title, rows, empty }: { title: string; rows: string[]; empty: string }) {
  return (
    <article>
      <h3>{title}</h3>
      {rows.length === 0 ? <p className="muted">{empty}</p> : rows.map((row) => <p key={row}>{row}</p>)}
    </article>
  );
}
