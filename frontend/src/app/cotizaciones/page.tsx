"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { AppLayout } from "@/components/AppLayout";
import { api, currentUser, type Client, type Me, type Quotation, type QuotationDetail, type Vehicle } from "@/lib/api";

type DraftItem = {
  tipo: string;
  codigo: string;
  descripcion: string;
  cantidad: number;
  valorUnitario: number;
  impuestoPorcentaje: number;
  descuentoPorcentaje: number;
};

export default function QuotationsPage() {
  const router = useRouter();
  const [user, setUser] = useState<Me | null>(null);
  const [branchId, setBranchId] = useState("");
  const [query, setQuery] = useState("");
  const [quotes, setQuotes] = useState<Quotation[]>([]);
  const [detail, setDetail] = useState<QuotationDetail | null>(null);
  const [clients, setClients] = useState<Client[]>([]);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [clientId, setClientId] = useState("");
  const [items, setItems] = useState<DraftItem[]>([]);
  const [message, setMessage] = useState("");

  const availableVehicles = useMemo(
      () => vehicles.filter((vehicle) => !clientId || vehicle.clienteId === clientId),
      [vehicles, clientId]);

  useEffect(() => {
    const me = currentUser();
    if (!me) router.push("/login");
    setUser(me);
    setBranchId(me?.sucursalId ?? me?.sucursales[0]?.id ?? "");
    void loadCatalogs();
  }, [router]);

  useEffect(() => {
    if (branchId) void loadQuotes();
  }, [branchId]);

  async function loadCatalogs() {
    const [clientList, vehicleList] = await Promise.all([
      api<Client[]>("/clients"),
      api<Vehicle[]>("/vehicles")
    ]);
    setClients(clientList);
    setVehicles(vehicleList);
  }

  async function loadQuotes() {
    const params = new URLSearchParams({ branchId });
    if (query) params.set("q", query);
    const rows = await api<Quotation[]>(`/quotations?${params.toString()}`);
    setQuotes(rows);
    if (rows.length > 0 && !detail) await openQuote(rows[0].id);
  }

  async function openQuote(id: string) {
    setDetail(await api<QuotationDetail>(`/quotations/${id}`));
  }

  async function createQuote(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (items.length === 0) {
      setMessage("Agrega al menos un item");
      return;
    }
    const form = new FormData(event.currentTarget);
    const created = await api<Quotation>("/quotations", {
      method: "POST",
      body: JSON.stringify({
        sucursalId: branchId,
        ordenTrabajoId: String(form.get("ordenTrabajoId")),
        clienteId: String(form.get("clienteId")),
        vehiculoId: String(form.get("vehiculoId")),
        venceEn: optionalText(form.get("venceEn")),
        moneda: "COP",
        items
      })
    });
    event.currentTarget.reset();
    setClientId("");
    setItems([]);
    setMessage("Cotizacion creada");
    await loadQuotes();
    await openQuote(created.id);
  }

  async function approve(itemIds: string[], action: string) {
    if (!detail) return;
    await api<QuotationDetail>(`/quotations/${detail.cotizacion.id}/approve`, {
      method: "POST",
      body: JSON.stringify({
        accion: action,
        aprobadorNombre: detail.cotizacion.cliente,
        aprobadorEmail: null,
        itemIds,
        evidenciaJson: JSON.stringify({ channel: "frontend", at: new Date().toISOString() })
      })
    });
    setMessage(action === "APROBAR" ? "Aprobacion registrada" : "Rechazo registrado");
    await openQuote(detail.cotizacion.id);
    await loadQuotes();
  }

  async function sendQuote() {
    if (!detail) return;
    await api<Quotation>(`/quotations/${detail.cotizacion.id}/send`, { method: "POST" });
    setMessage("Cotizacion enviada");
    await openQuote(detail.cotizacion.id);
    await loadQuotes();
  }

  async function generateWork() {
    if (!detail) return;
    await api(`/quotations/${detail.cotizacion.id}/generate-work`, { method: "POST" });
    setMessage("Trabajo generado solo con items aprobados");
    await openQuote(detail.cotizacion.id);
    await loadQuotes();
  }

  if (!user) return null;

  return (
    <AppLayout user={user}>
      <h1>Cotizaciones</h1>
      <section className="panel stack">
        <div className="split">
          <div className="field">
            <label htmlFor="branchId">Sucursal</label>
            <div className="input-row">
              <select id="branchId" value={branchId} onChange={(event) => setBranchId(event.target.value)}>
                {user.sucursales.map((branch) => <option key={branch.id} value={branch.id}>{branch.nombre}</option>)}
              </select>
            </div>
          </div>
          <div className="field">
            <label htmlFor="q">Buscar</label>
            <div className="input-row">
              <input id="q" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Cliente, placa o estado" />
            </div>
          </div>
        </div>
        <button className="secondary" onClick={loadQuotes}>Buscar</button>
      </section>
      {message ? <p className="muted">{message}</p> : null}
      <div className="settings-grid">
        <section className="panel stack">
          <h2>Listado</h2>
          {quotes.length === 0 ? (
            <p className="muted">No hay cotizaciones para la sucursal seleccionada.</p>
          ) : quotes.map((quote) => (
            <button className="client-row" key={quote.id} onClick={() => openQuote(quote.id)}>
              <strong>{quote.placa} - {quote.estado}</strong>
              <span>{quote.cliente} - {money(quote.total)} {quote.moneda}</span>
            </button>
          ))}
        </section>
        <form className="panel stack" onSubmit={createQuote}>
          <h2>Nueva cotizacion</h2>
          <Field name="ordenTrabajoId" label="OT id" />
          <SelectClient clients={clients} value={clientId} onChange={setClientId} />
          <VehicleSelect vehicles={availableVehicles} />
          <Field name="venceEn" label="Vence en" type="date" required={false} />
          <ItemBuilder items={items} setItems={setItems} />
          <button className="primary" type="submit">Crear cotizacion</button>
        </form>
      </div>
      {detail ? (
        <section className="panel stack">
          <h2>Detalle</h2>
          <p><strong>{detail.cotizacion.placa}</strong> - {detail.cotizacion.cliente} - v{detail.cotizacion.versionActual}</p>
          <p className="muted">Total {money(detail.cotizacion.total)} {detail.cotizacion.moneda}</p>
          <div className="tabs">
            <button className="tab" onClick={sendQuote}>Enviar</button>
            <button className="tab" onClick={() => approve(detail.items.map((item) => item.id), "APROBAR")}>Aprobar todo</button>
            <button className="tab" onClick={() => approve(detail.items.map((item) => item.id), "RECHAZAR")}>Rechazar todo</button>
            <button className="tab" onClick={generateWork}>Generar trabajo</button>
          </div>
          <div className="stack">
            {detail.items.map((item) => (
              <article className="client-row" key={item.id}>
                <strong>{item.tipo} - {item.descripcion}</strong>
                <span>{item.estadoAprobacion} - {item.cantidad} x {money(item.valorUnitario)} = {money(item.total)}</span>
                <div className="tabs">
                  <button className="tab" onClick={() => approve([item.id], "APROBAR")}>Aprobar item</button>
                  <button className="tab" onClick={() => approve([item.id], "RECHAZAR")}>Rechazar item</button>
                </div>
              </article>
            ))}
          </div>
          {detail.pdf ? (
            <a className="secondary" href={`data:application/pdf;base64,${detail.pdf.contenidoBase64}`} download={`cotizacion-${detail.cotizacion.placa}.pdf`}>
              Descargar PDF
            </a>
          ) : null}
          <p className="muted">Enlace web: /api/v1/quotations/public/{detail.cotizacion.publicToken}</p>
          <p className="muted">Versiones guardadas: {detail.versiones.length}. Aprobaciones: {detail.aprobaciones.length}.</p>
        </section>
      ) : null}
    </AppLayout>
  );
}

function ItemBuilder({ items, setItems }: { items: DraftItem[]; setItems: (items: DraftItem[]) => void }) {
  const [draft, setDraft] = useState<DraftItem>({
    tipo: "SERVICIO",
    codigo: "",
    descripcion: "",
    cantidad: 1,
    valorUnitario: 0,
    impuestoPorcentaje: 19,
    descuentoPorcentaje: 0
  });
  return (
    <div className="stack">
      <h3>Items</h3>
      <div className="split">
        <div className="field">
          <label htmlFor="tipoItem">Tipo</label>
          <div className="input-row">
            <select id="tipoItem" value={draft.tipo} onChange={(event) => setDraft({ ...draft, tipo: event.target.value })}>
              <option>MANO_OBRA</option>
              <option>SERVICIO</option>
              <option>REPUESTO</option>
            </select>
          </div>
        </div>
        <InlineInput label="Codigo" value={draft.codigo} onChange={(value) => setDraft({ ...draft, codigo: value })} />
        <InlineInput label="Descripcion" value={draft.descripcion} onChange={(value) => setDraft({ ...draft, descripcion: value })} />
        <InlineInput label="Cantidad" value={String(draft.cantidad)} onChange={(value) => setDraft({ ...draft, cantidad: Number(value || 1) })} />
        <InlineInput label="Valor" value={String(draft.valorUnitario)} onChange={(value) => setDraft({ ...draft, valorUnitario: Number(value || 0) })} />
        <InlineInput label="IVA %" value={String(draft.impuestoPorcentaje)} onChange={(value) => setDraft({ ...draft, impuestoPorcentaje: Number(value || 0) })} />
        <InlineInput label="Desc %" value={String(draft.descuentoPorcentaje)} onChange={(value) => setDraft({ ...draft, descuentoPorcentaje: Number(value || 0) })} />
      </div>
      <button className="secondary" type="button" onClick={() => {
        if (!draft.descripcion.trim()) return;
        setItems([...items, draft]);
        setDraft({ tipo: "SERVICIO", codigo: "", descripcion: "", cantidad: 1, valorUnitario: 0, impuestoPorcentaje: 19, descuentoPorcentaje: 0 });
      }}>Agregar item</button>
      {items.length === 0 ? <p className="muted">Sin items.</p> : items.map((item, index) => (
        <p key={`${item.descripcion}-${index}`}>{item.tipo} - {item.descripcion} - {money(item.valorUnitario)}</p>
      ))}
    </div>
  );
}

function SelectClient({ clients, value, onChange }: { clients: Client[]; value: string; onChange: (value: string) => void }) {
  return (
    <div className="field">
      <label htmlFor="clienteId">Cliente</label>
      <div className="input-row">
        <select id="clienteId" name="clienteId" required value={value} onChange={(event) => onChange(event.target.value)}>
          <option value="" disabled>Seleccionar cliente</option>
          {clients.map((client) => <option key={client.id} value={client.id}>{client.nombre}</option>)}
        </select>
      </div>
    </div>
  );
}

function VehicleSelect({ vehicles }: { vehicles: Vehicle[] }) {
  return (
    <div className="field">
      <label htmlFor="vehiculoId">Vehiculo</label>
      <div className="input-row">
        <select id="vehiculoId" name="vehiculoId" required defaultValue="">
          <option value="" disabled>Seleccionar vehiculo</option>
          {vehicles.map((vehicle) => <option key={vehicle.id} value={vehicle.id}>{vehicle.placa} - {vehicle.marca} {vehicle.modelo}</option>)}
        </select>
      </div>
    </div>
  );
}

function Field({ name, label, type = "text", required = true }: { name: string; label: string; type?: string; required?: boolean }) {
  return (
    <div className="field">
      <label htmlFor={name}>{label}</label>
      <div className="input-row">
        <input id={name} name={name} type={type} required={required} />
      </div>
    </div>
  );
}

function InlineInput({ label, value, onChange }: { label: string; value: string; onChange: (value: string) => void }) {
  return (
    <div className="field">
      <label>{label}</label>
      <div className="input-row">
        <input value={value} onChange={(event) => onChange(event.target.value)} />
      </div>
    </div>
  );
}

function optionalText(value: FormDataEntryValue | null) {
  const text = String(value ?? "");
  return text ? text : null;
}

function money(value: number) {
  return new Intl.NumberFormat("es-CO", { maximumFractionDigits: 0 }).format(value);
}
