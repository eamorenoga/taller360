"use client";

import { ChangeEvent, FormEvent, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { AppLayout } from "@/components/AppLayout";
import {
  api,
  currentUser,
  type AiSuggestion,
  type Client,
  type Diagnostic,
  type DiagnosticDetail,
  type DtcItem,
  type FindingItem,
  type Me,
  type SymptomItem,
  type Vehicle
} from "@/lib/api";

export default function DiagnosticsPage() {
  const router = useRouter();
  const [user, setUser] = useState<Me | null>(null);
  const [branchId, setBranchId] = useState("");
  const [query, setQuery] = useState("");
  const [diagnostics, setDiagnostics] = useState<Diagnostic[]>([]);
  const [detail, setDetail] = useState<DiagnosticDetail | null>(null);
  const [clients, setClients] = useState<Client[]>([]);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [clientId, setClientId] = useState("");
  const [symptoms, setSymptoms] = useState<SymptomItem[]>([]);
  const [dtc, setDtc] = useState<DtcItem[]>([]);
  const [findings, setFindings] = useState<FindingItem[]>([]);
  const [ai, setAi] = useState<AiSuggestion | null>(null);
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
    if (branchId) void loadDiagnostics();
  }, [branchId]);

  async function loadCatalogs() {
    const [clientList, vehicleList] = await Promise.all([
      api<Client[]>("/clients"),
      api<Vehicle[]>("/vehicles")
    ]);
    setClients(clientList);
    setVehicles(vehicleList);
  }

  async function loadDiagnostics() {
    const params = new URLSearchParams({ branchId });
    if (query) params.set("q", query);
    const rows = await api<Diagnostic[]>(`/diagnostics?${params.toString()}`);
    setDiagnostics(rows);
    if (rows.length > 0 && !detail) await openDiagnostic(rows[0].id);
  }

  async function openDiagnostic(id: string) {
    const data = await api<DiagnosticDetail>(`/diagnostics/${id}`);
    setDetail(data);
    setAi(data.diagnostico.iaSugerencia);
  }

  async function createDiagnostic(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const created = await api<Diagnostic>("/diagnostics", {
      method: "POST",
      body: JSON.stringify({
        sucursalId: branchId,
        ordenTrabajoId: String(form.get("ordenTrabajoId")),
        clienteId: String(form.get("clienteId")),
        vehiculoId: String(form.get("vehiculoId")),
        sintomas: symptoms,
        inspecciones: [],
        dtc,
        pruebas: [],
        hallazgos: findings,
        causaProbable: String(form.get("causaProbable") ?? ""),
        solucionRecomendada: String(form.get("solucionRecomendada") ?? ""),
        prioridad: String(form.get("prioridad") || "MEDIA"),
        estado: "BORRADOR",
        tiempoEstimadoMinutos: Number(form.get("tiempoEstimadoMinutos") || 0)
      })
    });
    event.currentTarget.reset();
    setClientId("");
    setSymptoms([]);
    setDtc([]);
    setFindings([]);
    setMessage("Diagnostico creado");
    await loadDiagnostics();
    await openDiagnostic(created.id);
  }

  async function addEvidence(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file || !detail) return;
    const dataUrl = await readFile(file);
    await api(`/diagnostics/${detail.diagnostico.id}/evidences`, {
      method: "POST",
      body: JSON.stringify({
        tipo: evidenceType(file.type),
        url: dataUrl,
        nombreArchivo: file.name,
        mimeType: file.type,
        tamanoBytes: file.size,
        metadatosJson: JSON.stringify({ source: "frontend" })
      })
    });
    setMessage("Evidencia agregada");
    await openDiagnostic(detail.diagnostico.id);
  }

  async function addTask(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!detail) return;
    const form = new FormData(event.currentTarget);
    await api(`/diagnostics/${detail.diagnostico.id}/tasks`, {
      method: "POST",
      body: JSON.stringify({
        descripcion: String(form.get("descripcion")),
        prioridad: String(form.get("prioridad") || detail.diagnostico.prioridad),
        estado: "PENDIENTE",
        tiempoEstimadoMinutos: Number(form.get("tiempoEstimadoMinutos") || 0)
      })
    });
    event.currentTarget.reset();
    await openDiagnostic(detail.diagnostico.id);
  }

  async function addPart(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!detail) return;
    const form = new FormData(event.currentTarget);
    await api(`/diagnostics/${detail.diagnostico.id}/parts`, {
      method: "POST",
      body: JSON.stringify({
        codigo: String(form.get("codigo") ?? ""),
        nombre: String(form.get("nombre")),
        cantidad: Number(form.get("cantidad") || 1),
        requerido: true,
        notas: String(form.get("notas") ?? "")
      })
    });
    event.currentTarget.reset();
    await openDiagnostic(detail.diagnostico.id);
  }

  async function askAi() {
    if (!detail) return;
    const suggestion = await api<AiSuggestion>(`/diagnostics/${detail.diagnostico.id}/ai-assist`, {
      method: "POST",
      body: JSON.stringify({ pregunta: "Sugerir causa probable y pruebas", incluirDtc: true, incluirHallazgos: true })
    });
    setAi(suggestion);
    setMessage("Sugerencia IA generada pendiente de confirmacion tecnica");
    await openDiagnostic(detail.diagnostico.id);
  }

  async function confirmAi() {
    if (!detail) return;
    await api<Diagnostic>(`/diagnostics/${detail.diagnostico.id}/ai-confirm`, { method: "POST" });
    setMessage("Sugerencia IA confirmada por tecnico");
    await openDiagnostic(detail.diagnostico.id);
    await loadDiagnostics();
  }

  if (!user) return null;

  return (
    <AppLayout user={user}>
      <h1>Diagnostico tecnico</h1>
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
              <input id="q" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Cliente, placa o causa" />
            </div>
          </div>
        </div>
        <button className="secondary" onClick={loadDiagnostics}>Buscar</button>
      </section>
      {message ? <p className="muted">{message}</p> : null}
      <div className="settings-grid">
        <section className="panel stack">
          <h2>Diagnosticos</h2>
          {diagnostics.length === 0 ? (
            <p className="muted">No hay diagnosticos para la sucursal seleccionada.</p>
          ) : diagnostics.map((item) => (
            <button className="client-row" key={item.id} onClick={() => openDiagnostic(item.id)}>
              <strong>{item.placa} - {item.prioridad}</strong>
              <span>{item.cliente} - {item.estado}</span>
            </button>
          ))}
        </section>
        <form className="panel stack" onSubmit={createDiagnostic}>
          <h2>Nuevo diagnostico</h2>
          <Field name="ordenTrabajoId" label="OT id" />
          <SelectClient clients={clients} value={clientId} onChange={setClientId} />
          <VehicleSelect vehicles={availableVehicles} />
          <div className="split">
            <Field name="causaProbable" label="Causa probable" required={false} />
            <Field name="solucionRecomendada" label="Solucion recomendada" required={false} />
            <Field name="tiempoEstimadoMinutos" label="Tiempo minutos" defaultValue="60" />
            <div className="field">
              <label htmlFor="prioridad">Prioridad</label>
              <div className="input-row">
                <select id="prioridad" name="prioridad" defaultValue="MEDIA">
                  <option>BAJA</option>
                  <option>MEDIA</option>
                  <option>ALTA</option>
                  <option>CRITICA</option>
                </select>
              </div>
            </div>
          </div>
          <QuickAdd title="Sintoma" onAdd={(value) => setSymptoms([...symptoms, { descripcion: value, severidad: "MEDIA", condicion: null }])} />
          <ListPreview rows={symptoms.map((item) => item.descripcion)} empty="Sin sintomas." />
          <QuickAdd title="DTC" onAdd={(value) => setDtc([...dtc, { codigo: value, descripcion: "Codigo reportado", modulo: null, estado: "ACTIVO" }])} />
          <ListPreview rows={dtc.map((item) => item.codigo)} empty="Sin codigos DTC." />
          <QuickAdd title="Hallazgo" onAdd={(value) => setFindings([...findings, { descripcion: value, evidencia: null, impacto: null }])} />
          <ListPreview rows={findings.map((item) => item.descripcion)} empty="Sin hallazgos." />
          <button className="primary" type="submit">Crear diagnostico</button>
        </form>
      </div>
      {detail ? (
        <section className="panel stack">
          <h2>Detalle tecnico</h2>
          <p><strong>{detail.diagnostico.placa}</strong> - {detail.diagnostico.cliente} - {detail.diagnostico.estado}</p>
          <p className="muted">{detail.diagnostico.causaProbable || "Sin causa probable confirmada."}</p>
          <div className="split">
            <div>
              <h3>Evidencias</h3>
              <input type="file" accept="image/*,video/*,audio/*" onChange={addEvidence} />
              <ListPreview rows={detail.evidencias.map((item) => `${item.tipo} - ${item.nombreArchivo}`)} empty="Sin evidencias." />
            </div>
            <form className="stack" onSubmit={addTask}>
              <h3>Tareas</h3>
              <Field name="descripcion" label="Descripcion" />
              <Field name="tiempoEstimadoMinutos" label="Tiempo minutos" defaultValue="30" />
              <button className="secondary" type="submit">Agregar tarea</button>
              <ListPreview rows={detail.tareas.map((item) => item.descripcion)} empty="Sin tareas." />
            </form>
            <form className="stack" onSubmit={addPart}>
              <h3>Repuestos</h3>
              <Field name="codigo" label="Codigo" required={false} />
              <Field name="nombre" label="Nombre" />
              <Field name="cantidad" label="Cantidad" defaultValue="1" />
              <Field name="notas" label="Notas" required={false} />
              <button className="secondary" type="submit">Agregar repuesto</button>
              <ListPreview rows={detail.repuestos.map((item) => `${item.nombre} x ${item.cantidad}`)} empty="Sin repuestos." />
            </form>
          </div>
          <div className="panel stack">
            <h3>Asistencia IA</h3>
            <button className="secondary" onClick={askAi}>Solicitar sugerencia</button>
            {ai ? (
              <>
                <p className="muted">{ai.aviso}</p>
                <ListPreview rows={ai.causasProbables} empty="Sin causas sugeridas." />
                <ListPreview rows={ai.pruebasSugeridas} empty="Sin pruebas sugeridas." />
                {ai.requiereConfirmacionTecnica ? <button className="primary" onClick={confirmAi}>Confirmar tecnicamente</button> : null}
              </>
            ) : <p className="muted">Sin sugerencia IA generada.</p>}
          </div>
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

function QuickAdd({ title, onAdd }: { title: string; onAdd: (value: string) => void }) {
  const [value, setValue] = useState("");
  return (
    <div className="field">
      <label htmlFor={title}>{title}</label>
      <div className="input-row">
        <input id={title} value={value} onChange={(event) => setValue(event.target.value)} />
        <button className="secondary" type="button" onClick={() => {
          if (value.trim()) onAdd(value.trim());
          setValue("");
        }}>Agregar</button>
      </div>
    </div>
  );
}

function ListPreview({ rows, empty }: { rows: string[]; empty: string }) {
  return rows.length === 0 ? <p className="muted">{empty}</p> : (
    <div className="stack">
      {rows.map((row, index) => <p key={`${row}-${index}`}>{row}</p>)}
    </div>
  );
}

function evidenceType(mime: string) {
  if (mime.startsWith("video/")) return "VIDEO";
  if (mime.startsWith("audio/")) return "AUDIO";
  if (mime.startsWith("image/")) return "FOTO";
  return "DOCUMENTO";
}

function readFile(file: File) {
  return new Promise<string>((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(String(reader.result));
    reader.onerror = () => reject(reader.error);
    reader.readAsDataURL(file);
  });
}
