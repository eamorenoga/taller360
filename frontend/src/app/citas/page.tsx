"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { AppLayout } from "@/components/AppLayout";
import {
  api,
  currentUser,
  type Appointment,
  type AppointmentBay,
  type AppointmentCalendar,
  type AppointmentCapacity,
  type AppointmentTechnician,
  type Client,
  type Me,
  type ScheduleCapacityRule,
  type Vehicle
} from "@/lib/api";

const views = ["DIA", "SEMANA", "MES"] as const;

export default function AppointmentsPage() {
  const router = useRouter();
  const [user, setUser] = useState<Me | null>(null);
  const [view, setView] = useState<(typeof views)[number]>("SEMANA");
  const [baseDate, setBaseDate] = useState(() => new Date().toISOString().slice(0, 10));
  const [branchId, setBranchId] = useState("");
  const [appointments, setAppointments] = useState<Appointment[]>([]);
  const [capacity, setCapacity] = useState<AppointmentCapacity | null>(null);
  const [capacityRules, setCapacityRules] = useState<ScheduleCapacityRule[]>([]);
  const [clients, setClients] = useState<Client[]>([]);
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [technicians, setTechnicians] = useState<AppointmentTechnician[]>([]);
  const [bays, setBays] = useState<AppointmentBay[]>([]);
  const [selectedClientId, setSelectedClientId] = useState("");
  const [message, setMessage] = useState("");

  const range = useMemo(() => dateRange(baseDate, view), [baseDate, view]);
  const availableVehicles = vehicles.filter((vehicle) => !selectedClientId || vehicle.clienteId === selectedClientId);

  useEffect(() => {
    const me = currentUser();
    if (!me) router.push("/login");
    setUser(me);
    setBranchId(me?.sucursalId ?? me?.sucursales[0]?.id ?? "");
    void loadCatalogs();
  }, [router]);

  useEffect(() => {
    if (branchId) {
      void loadAgenda();
    }
  }, [branchId, range.start, range.end, view]);

  async function loadCatalogs() {
    const [clientList, vehicleList] = await Promise.all([
      api<Client[]>("/clients"),
      api<Vehicle[]>("/vehicles")
    ]);
    setClients(clientList);
    setVehicles(vehicleList);
    setTechnicians(await api<AppointmentTechnician[]>("/appointments/technicians"));
  }

  async function loadAgenda() {
    const params = new URLSearchParams({
      start: range.start,
      end: range.end,
      branchId,
      view
    });
    const [calendar, currentCapacity, bayList, ruleList] = await Promise.all([
      api<AppointmentCalendar>(`/appointments?${params.toString()}`),
      api<AppointmentCapacity>(`/appointments/capacity?branchId=${encodeURIComponent(branchId)}&start=${encodeURIComponent(range.start)}&end=${encodeURIComponent(range.end)}`),
      api<AppointmentBay[]>(`/appointments/bays?branchId=${encodeURIComponent(branchId)}`),
      api<ScheduleCapacityRule[]>(`/appointments/capacity-rules?branchId=${encodeURIComponent(branchId)}`)
    ]);
    setAppointments(calendar.citas);
    setCapacity(currentCapacity);
    setBays(bayList);
    setCapacityRules(ruleList);
  }

  async function createAppointment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const fechaInicio = String(form.get("fechaInicio"));
    const created = await api<Appointment>("/appointments", {
      method: "POST",
      body: JSON.stringify({
        sucursalId: branchId,
        clienteId: String(form.get("clienteId")),
        vehiculoId: String(form.get("vehiculoId")),
        tecnicoId: optionalValue(form.get("tecnicoId")),
        bahiaId: optionalValue(form.get("bahiaId")),
        servicio: String(form.get("servicio")),
        fechaInicio: new Date(fechaInicio).toISOString(),
        duracionMinutos: Number(form.get("duracionMinutos") || 60),
        estado: "PROGRAMADA",
        notas: String(form.get("notas") ?? "")
      })
    });
    event.currentTarget.reset();
    setSelectedClientId("");
    setMessage(created.sobrecapacidad ? "Cita creada con alerta de sobrecapacidad" : "Cita creada");
    await loadAgenda();
  }

  async function setState(id: string, state: string) {
    await api<Appointment>(`/appointments/${id}/state?state=${encodeURIComponent(state)}`, { method: "PATCH" });
    setMessage("Estado actualizado");
    await loadAgenda();
  }

  async function addReminder(appointment: Appointment) {
    const scheduledAt = new Date(appointment.fechaInicio);
    scheduledAt.setHours(scheduledAt.getHours() - 24);
    await api<unknown>(`/appointments/${appointment.id}/reminders`, {
      method: "POST",
      body: JSON.stringify({
        canal: "WHATSAPP",
        programadoPara: scheduledAt.toISOString(),
        mensaje: `Recordatorio de cita para ${appointment.placa}`
      })
    });
    setMessage("Recordatorio creado");
  }

  async function convertToReception(id: string) {
    await api(`/appointments/${id}/convert-to-reception`, { method: "POST" });
    setMessage("Cita convertida a recepcion");
    await loadAgenda();
  }

  if (!user) return null;

  return (
    <AppLayout user={user}>
      <h1>Agenda y citas</h1>
      <section className="panel stack">
        <div className="tabs">
          {views.map((item) => (
            <button className="tab" data-active={view === item} key={item} onClick={() => setView(item)}>
              {item}
            </button>
          ))}
        </div>
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
            <label htmlFor="baseDate">Fecha</label>
            <div className="input-row">
              <input id="baseDate" type="date" value={baseDate} onChange={(event) => setBaseDate(event.target.value)} />
            </div>
          </div>
        </div>
        {capacity ? (
          <p className="muted">
            {capacity.citasProgramadas} citas programadas de {capacity.capacidadMaxima} cupos.
            {capacity.sobrecapacidad ? " Hay sobrecapacidad para este rango." : " Capacidad disponible."}
          </p>
        ) : null}
        {capacityRules.length > 0 ? (
          <p className="muted">Reglas activas/configuradas: {capacityRules.length}</p>
        ) : (
          <p className="muted">Sin reglas de capacidad configuradas para esta sucursal. Se usa capacidad minima 1.</p>
        )}
      </section>
      {message ? <p className="muted">{message}</p> : null}
      <div className="settings-grid">
        <section className="panel">
          <h2>Calendario</h2>
          {appointments.length === 0 ? (
            <p className="muted">No hay citas en el rango seleccionado.</p>
          ) : (
            <div className="stack">
              {appointments.map((appointment) => (
                <article className="client-row" key={appointment.id}>
                  <strong>{formatDateTime(appointment.fechaInicio)} - {appointment.servicio}</strong>
                  <span>{appointment.cliente} - {appointment.placa} - {appointment.estado}</span>
                  {appointment.sobrecapacidad ? <span className="error">Sobrecapacidad detectada</span> : null}
                  <div className="tabs">
                    <button className="tab" onClick={() => setState(appointment.id, "CONFIRMADA")}>Confirmar</button>
                    <button className="tab" onClick={() => setState(appointment.id, "LLEGO")}>Llego</button>
                    <button className="tab" onClick={() => setState(appointment.id, "NO_ASISTIO")}>No asistio</button>
                    <button className="tab" onClick={() => setState(appointment.id, "CANCELADA")}>Cancelar</button>
                    <button className="tab" onClick={() => addReminder(appointment)}>Recordatorio</button>
                    <button className="tab" onClick={() => convertToReception(appointment.id)}>Recepcion</button>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
        <form className="panel stack" onSubmit={createAppointment}>
          <h2>Nueva cita</h2>
          <div className="field">
            <label htmlFor="clienteId">Cliente</label>
            <div className="input-row">
              <select id="clienteId" name="clienteId" required value={selectedClientId} onChange={(event) => setSelectedClientId(event.target.value)}>
                <option value="" disabled>Seleccionar cliente</option>
                {clients.map((client) => <option key={client.id} value={client.id}>{client.nombre}</option>)}
              </select>
            </div>
          </div>
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
            <Field name="servicio" label="Servicio" required />
            <Field name="fechaInicio" label="Fecha y hora" type="datetime-local" required />
            <Field name="duracionMinutos" label="Duracion minutos" defaultValue="60" required />
            <Field name="notas" label="Notas" />
          </div>
          <div className="split">
            <div className="field">
              <label htmlFor="tecnicoId">Tecnico opcional</label>
              <div className="input-row">
                <select id="tecnicoId" name="tecnicoId" defaultValue="">
                  <option value="">Sin tecnico</option>
                  {technicians.map((technician) => <option key={technician.id} value={technician.id}>{technician.nombre}</option>)}
                </select>
              </div>
            </div>
            <div className="field">
              <label htmlFor="bahiaId">Bahia opcional</label>
              <div className="input-row">
                <select id="bahiaId" name="bahiaId" defaultValue="">
                  <option value="">Sin bahia</option>
                  {bays.map((bay) => <option key={bay.id} value={bay.id}>{bay.nombre}</option>)}
                </select>
              </div>
            </div>
          </div>
          <button className="primary" type="submit">Crear cita</button>
        </form>
      </div>
    </AppLayout>
  );
}

function Field({ name, label, type = "text", defaultValue = "", required = false }: {
  name: string;
  label: string;
  type?: string;
  defaultValue?: string;
  required?: boolean;
}) {
  return (
    <div className="field">
      <label htmlFor={name}>{label}</label>
      <div className="input-row">
        <input id={name} name={name} type={type} defaultValue={defaultValue} required={required} />
      </div>
    </div>
  );
}

function dateRange(value: string, view: string) {
  const start = new Date(`${value}T00:00:00`);
  const end = new Date(start);
  if (view === "DIA") {
    end.setDate(start.getDate() + 1);
  } else if (view === "MES") {
    start.setDate(1);
    end.setMonth(start.getMonth() + 1, 1);
  } else {
    const day = start.getDay() || 7;
    start.setDate(start.getDate() - day + 1);
    end.setDate(start.getDate() + 7);
  }
  return { start: start.toISOString(), end: end.toISOString() };
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat("es-CO", {
    dateStyle: "medium",
    timeStyle: "short"
  }).format(new Date(value));
}

function optionalValue(value: FormDataEntryValue | null) {
  const text = String(value ?? "");
  return text ? text : null;
}
