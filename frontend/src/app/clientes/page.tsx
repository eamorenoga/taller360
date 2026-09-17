"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { AppLayout } from "@/components/AppLayout";
import { api, currentUser, type Client, type Client360, type Me } from "@/lib/api";

export default function ClientsPage() {
  const router = useRouter();
  const [user, setUser] = useState<Me | null>(null);
  const [query, setQuery] = useState("");
  const [plate, setPlate] = useState("");
  const [clients, setClients] = useState<Client[]>([]);
  const [selected, setSelected] = useState<Client360 | null>(null);
  const [message, setMessage] = useState("");

  useEffect(() => {
    const me = currentUser();
    if (!me) router.push("/login");
    setUser(me);
    void load();
  }, [router]);

  async function load() {
    const params = new URLSearchParams();
    if (query) params.set("q", query);
    if (plate) params.set("plate", plate);
    const data = await api<Client[]>(`/clients${params.toString() ? `?${params}` : ""}`);
    setClients(data);
    if (data.length > 0 && !selected) {
      await openClient(data[0].id);
    }
  }

  async function openClient(id: string) {
    setSelected(await api<Client360>(`/clients/${id}`));
  }

  async function createClient(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const created = await api<Client>("/clients", {
      method: "POST",
      body: JSON.stringify({ ...Object.fromEntries(form), estado: "ACTIVO" })
    });
    event.currentTarget.reset();
    setMessage("Cliente creado");
    await load();
    await openClient(created.id);
  }

  if (!user) return null;

  return (
    <AppLayout user={user}>
      <h1>Clientes</h1>
      <section className="panel stack">
        <div className="split">
          <div className="field">
            <label htmlFor="q">Buscar cliente</label>
            <div className="input-row">
              <input id="q" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Nombre, identificacion, telefono o email" />
            </div>
          </div>
          <div className="field">
            <label htmlFor="plate">Placa relacionada</label>
            <div className="input-row">
              <input id="plate" value={plate} onChange={(event) => setPlate(event.target.value)} placeholder="ABC123" />
            </div>
          </div>
        </div>
        <button className="secondary" onClick={load}>Buscar</button>
      </section>
      {message ? <p className="muted">{message}</p> : null}
      <div className="settings-grid">
        <section className="panel">
          <h2>Listado</h2>
          {clients.length === 0 ? (
            <p className="muted">No hay clientes para los filtros actuales.</p>
          ) : (
            <div className="stack">
              {clients.map((client) => (
                <button className="client-row" key={client.id} onClick={() => openClient(client.id)}>
                  <strong>{client.nombre}</strong>
                  <span>{client.identificacion ?? "Sin identificacion"} · {client.telefonoPrincipal ?? "Sin telefono"}</span>
                </button>
              ))}
            </div>
          )}
        </section>
        <form className="panel stack" onSubmit={createClient}>
          <h2>Nuevo cliente</h2>
          <div className="split">
            <Field name="tipoPersona" label="Tipo persona" defaultValue="NATURAL" />
            <Field name="tipoIdentificacion" label="Tipo identificacion" defaultValue="CC" />
            <Field name="identificacion" label="Identificacion" />
            <Field name="nombre" label="Nombre / razon comercial" />
            <Field name="razonSocial" label="Razon social" />
            <Field name="telefonoPrincipal" label="Telefono" />
            <Field name="telefonoSecundario" label="Telefono alterno" />
            <Field name="whatsapp" label="WhatsApp" />
            <Field name="email" label="Email" />
            <Field name="direccion" label="Direccion" />
            <Field name="preferencias" label="Preferencias" />
            <Field name="notas" label="Notas" />
          </div>
          <button className="primary" type="submit">Crear cliente</button>
        </form>
      </div>
      {selected ? <Client360Panel data={selected} /> : null}
    </AppLayout>
  );
}

function Client360Panel({ data }: { data: Client360 }) {
  return (
    <section className="panel stack">
      <h2>Ficha 360</h2>
      <div className="split">
        <Info title="Cliente" rows={[
          ["Nombre", data.cliente.nombre],
          ["Identificacion", data.cliente.identificacion ?? "-"],
          ["Telefono", data.cliente.telefonoPrincipal ?? "-"],
          ["WhatsApp", data.cliente.whatsapp ?? "-"],
          ["Email", data.cliente.email ?? "-"],
          ["Direccion", data.cliente.direccion ?? "-"],
          ["Estado", data.cliente.estado]
        ]} />
        <Info title="Cartera" rows={[
          ["Estado", data.cartera.estado],
          ["Saldo", `${data.cartera.saldoPendiente} ${data.cartera.moneda}`]
        ]} />
      </div>
      <Related title="Vehiculos" rows={data.vehiculos.map((item) => item.titulo)} empty="Sin vehiculos asociados." />
      <Related title="Contactos" rows={data.contactos.map((item) => `${item.nombre} · ${item.telefono ?? item.email ?? "Sin contacto"}`)} empty="Sin contactos registrados." />
      <Related title="Citas" rows={data.citas.map((item) => item.titulo)} empty="Sin citas registradas." />
      <Related title="Ordenes de trabajo" rows={data.ordenesTrabajo.map((item) => item.titulo)} empty="Sin ordenes registradas." />
      <Related title="Facturas" rows={data.facturas.map((item) => item.titulo)} empty="Sin facturas registradas." />
      <Related title="Pagos" rows={data.pagos.map((item) => item.titulo)} empty="Sin pagos registrados." />
      <Related title="Comunicaciones" rows={data.comunicaciones.map((item) => `${item.canal} · ${item.asunto ?? item.estado}`)} empty="Sin comunicaciones registradas." />
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
