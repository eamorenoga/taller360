"use client";

import { ChangeEvent, FormEvent, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { AppLayout } from "@/components/AppLayout";
import {
  api,
  currentUser,
  type AccessoryItem,
  type ChecklistItem,
  type Client,
  type DamageItem,
  type Me,
  type Reception,
  type ReceptionDetail,
  type Vehicle
} from "@/lib/api";

const defaultAccessories: AccessoryItem[] = [
  { nombre: "Llaves", presente: true, observacion: null },
  { nombre: "Documentos", presente: false, observacion: null },
  { nombre: "Herramientas", presente: false, observacion: null },
  { nombre: "Llanta de repuesto", presente: false, observacion: null }
];

const defaultChecklist: ChecklistItem[] = [
  { codigo: "LUCES", etiqueta: "Luces exteriores", ok: true, observacion: null },
  { codigo: "FRENOS", etiqueta: "Frenos", ok: true, observacion: null },
  { codigo: "NIVELES", etiqueta: "Niveles de fluidos", ok: true, observacion: null },
  { codigo: "TESTIGOS", etiqueta: "Testigos en tablero", ok: true, observacion: null }
];

const zones = ["Frente", "Trasera", "Lateral izquierdo", "Lateral derecho", "Interior", "Techo"];

export default function ReceptionPage() {
  const router = useRouter();
  const [user, setUser] = useState<Me | null>(null);
  const [branchId, setBranchId] = useState("");
  const [query, setQuery] = useState("");
  const [receptions, setReceptions] = useState<Reception[]>([]);
  const [detail, setDetail] = useState<ReceptionDetail | null>(null);
  const [clients, setClients] = useState<Client[]>([]);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [clientId, setClientId] = useState("");
  const [accessories, setAccessories] = useState(defaultAccessories);
  const [checklist, setChecklist] = useState(defaultChecklist);
  const [damages, setDamages] = useState<DamageItem[]>([]);
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
    if (branchId) void loadReceptions();
  }, [branchId]);

  async function loadCatalogs() {
    const [clientList, vehicleList] = await Promise.all([
      api<Client[]>("/clients"),
      api<Vehicle[]>("/vehicles")
    ]);
    setClients(clientList);
    setVehicles(vehicleList);
  }

  async function loadReceptions() {
    const params = new URLSearchParams({ branchId });
    if (query) params.set("q", query);
    const rows = await api<Reception[]>(`/receptions?${params.toString()}`);
    setReceptions(rows);
    if (rows.length > 0 && !detail) await openReception(rows[0].id);
  }

  async function openReception(id: string) {
    setDetail(await api<ReceptionDetail>(`/receptions/${id}`));
  }

  async function createReception(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const created = await api<Reception>("/receptions", {
      method: "POST",
      body: JSON.stringify({
        sucursalId: branchId,
        clienteId: String(form.get("clienteId")),
        vehiculoId: String(form.get("vehiculoId")),
        kilometraje: Number(form.get("kilometraje") || 0),
        combustiblePorcentaje: Number(form.get("combustiblePorcentaje") || 0),
        motivo: String(form.get("motivo")),
        accesorios: accessories,
        checklist,
        danos: damages,
        observaciones: String(form.get("observaciones") ?? "")
      })
    });
    event.currentTarget.reset();
    setClientId("");
    setAccessories(defaultAccessories);
    setChecklist(defaultChecklist);
    setDamages([]);
    setMessage("Recepcion creada");
    await loadReceptions();
    await openReception(created.id);
  }

  async function addPhoto(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file || !detail) return;
    const dataUrl = await readFile(file);
    await api(`/receptions/${detail.recepcion.id}/photos`, {
      method: "POST",
      body: JSON.stringify({
        url: dataUrl,
        nombreArchivo: file.name,
        tipo: file.type || "image/jpeg",
        tamanoOriginalBytes: file.size,
        tamanoComprimidoBytes: Math.round(file.size * 0.72),
        metadatosJson: JSON.stringify({ source: "frontend", compression: "browser-placeholder" })
      })
    });
    setMessage("Foto agregada");
    await openReception(detail.recepcion.id);
  }

  async function signReception(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!detail) return;
    const form = new FormData(event.currentTarget);
    const firmante = String(form.get("firmante"));
    const firmaUrl = `data:text/plain;base64,${toBase64(firmante)}`;
    await api<ReceptionDetail>(`/receptions/${detail.recepcion.id}/sign`, {
      method: "POST",
      body: JSON.stringify({ firmante, firmaUrl })
    });
    setMessage("Recepcion firmada y PDF generado");
    await openReception(detail.recepcion.id);
    await loadReceptions();
  }

  async function createWorkOrder() {
    if (!detail) return;
    await api(`/receptions/${detail.recepcion.id}/work-order`, { method: "POST" });
    setMessage("OT reservada desde recepcion");
    await openReception(detail.recepcion.id);
  }

  if (!user) return null;

  return (
    <AppLayout user={user}>
      <h1>Recepcion digital</h1>
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
              <input id="q" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Cliente, placa o motivo" />
            </div>
          </div>
        </div>
        <button className="secondary" onClick={loadReceptions}>Buscar</button>
      </section>
      {message ? <p className="muted">{message}</p> : null}
      <div className="settings-grid">
        <section className="panel stack">
          <h2>Recepciones</h2>
          {receptions.length === 0 ? (
            <p className="muted">No hay recepciones para la sucursal seleccionada.</p>
          ) : receptions.map((item) => (
            <button className="client-row" key={item.id} onClick={() => openReception(item.id)}>
              <strong>{item.placa} - {item.estado}</strong>
              <span>{item.cliente} - {item.motivo}</span>
            </button>
          ))}
        </section>
        <form className="panel stack" onSubmit={createReception}>
          <h2>Nueva recepcion</h2>
          <SelectClient clients={clients} value={clientId} onChange={setClientId} />
          <div className="field">
            <label htmlFor="vehiculoId">Vehiculo</label>
            <div className="input-row">
              <select id="vehiculoId" name="vehiculoId" required defaultValue="">
                <option value="" disabled>Seleccionar vehiculo</option>
                {availableVehicles.map((vehicle) => <option key={vehicle.id} value={vehicle.id}>{vehicle.placa} - {vehicle.marca} {vehicle.modelo}</option>)}
              </select>
            </div>
          </div>
          <div className="split">
            <Field name="kilometraje" label="Kilometraje" defaultValue="0" />
            <Field name="combustiblePorcentaje" label="Combustible %" defaultValue="50" />
            <Field name="motivo" label="Motivo" />
            <Field name="observaciones" label="Observaciones" required={false} />
          </div>
          <ToggleList title="Accesorios" items={accessories} setItems={setAccessories} />
          <Checklist items={checklist} setItems={setChecklist} />
          <DamageMap damages={damages} setDamages={setDamages} />
          <button className="primary" type="submit">Crear recepcion</button>
        </form>
      </div>
      {detail ? (
        <section className="panel stack">
          <h2>Detalle firmado</h2>
          <p><strong>{detail.recepcion.placa}</strong> - {detail.recepcion.cliente} - {detail.recepcion.estado}</p>
          <p className="muted">Km {detail.recepcion.kilometraje} - Combustible {detail.recepcion.combustiblePorcentaje}%</p>
          <div className="split">
            <div>
              <h3>Fotos</h3>
              {detail.recepcion.estado === "BORRADOR" ? <input type="file" accept="image/*" capture="environment" onChange={addPhoto} /> : null}
              {detail.fotos.length === 0 ? <p className="muted">Sin fotos registradas.</p> : detail.fotos.map((photo) => (
                <p key={photo.id}>{photo.nombreArchivo} - {photo.tamanoComprimidoBytes} bytes</p>
              ))}
            </div>
            <form className="stack" onSubmit={signReception}>
              <h3>Firma</h3>
              {detail.recepcion.estado === "BORRADOR" ? (
                <>
                  <Field name="firmante" label="Nombre del firmante" />
                  <button className="primary" type="submit">Firmar y generar PDF</button>
                </>
              ) : (
                <p className="muted">Firmada por {detail.recepcion.firmadoPor}</p>
              )}
            </form>
          </div>
          {detail.pdf ? (
            <a className="secondary" href={`data:application/pdf;base64,${detail.pdf.contenidoBase64}`} download={`recepcion-${detail.recepcion.placa}.pdf`}>
              Descargar PDF firmado
            </a>
          ) : null}
          {detail.recepcion.estado === "FIRMADA" ? <button className="primary" onClick={createWorkOrder}>Crear OT</button> : null}
        </section>
      ) : null}
    </AppLayout>
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

function Field({ name, label, defaultValue = "", required = true }: {
  name: string;
  label: string;
  defaultValue?: string;
  required?: boolean;
}) {
  return (
    <div className="field">
      <label htmlFor={name}>{label}</label>
      <div className="input-row">
        <input id={name} name={name} defaultValue={defaultValue} required={required} />
      </div>
    </div>
  );
}

function ToggleList({ title, items, setItems }: { title: string; items: AccessoryItem[]; setItems: (items: AccessoryItem[]) => void }) {
  return (
    <div>
      <h3>{title}</h3>
      {items.map((item, index) => (
        <label className="checkbox" key={item.nombre}>
          <input type="checkbox" checked={item.presente} onChange={(event) => {
            const copy = [...items];
            copy[index] = { ...item, presente: event.target.checked };
            setItems(copy);
          }} /> {item.nombre}
        </label>
      ))}
    </div>
  );
}

function Checklist({ items, setItems }: { items: ChecklistItem[]; setItems: (items: ChecklistItem[]) => void }) {
  return (
    <div>
      <h3>Checklist</h3>
      {items.map((item, index) => (
        <label className="checkbox" key={item.codigo}>
          <input type="checkbox" checked={item.ok} onChange={(event) => {
            const copy = [...items];
            copy[index] = { ...item, ok: event.target.checked };
            setItems(copy);
          }} /> {item.etiqueta}
        </label>
      ))}
    </div>
  );
}

function DamageMap({ damages, setDamages }: { damages: DamageItem[]; setDamages: (items: DamageItem[]) => void }) {
  return (
    <div>
      <h3>Mapa de danos</h3>
      <div className="tabs">
        {zones.map((zone, index) => (
          <button className="tab" type="button" key={zone} onClick={() => setDamages([...damages, {
            zona: zone,
            x: 20 + index * 10,
            y: 40,
            severidad: "MEDIA",
            descripcion: "Dano registrado en recepcion"
          }])}>{zone}</button>
        ))}
      </div>
      {damages.length === 0 ? <p className="muted">Sin danos marcados.</p> : damages.map((damage, index) => (
        <p key={`${damage.zona}-${index}`}>{damage.zona} - {damage.severidad}</p>
      ))}
    </div>
  );
}

function readFile(file: File) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result));
    reader.onerror = () => reject(reader.error);
    reader.readAsDataURL(file);
  });
}

function toBase64(value: string) {
  const bytes = new TextEncoder().encode(value);
  let binary = "";
  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte);
  });
  return btoa(binary);
}
