"use client";

import { FormEvent, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { AppLayout } from "@/components/AppLayout";
import { api, currentUser, type Branch, type Company, type Consecutive, type Me, type OperationalParameter, type Tax } from "@/lib/api";

type Tab = "empresa" | "sucursales" | "impuestos" | "consecutivos" | "parametros";

export default function ConfigurationPage() {
  const router = useRouter();
  const [user, setUser] = useState<Me | null>(null);
  const [tab, setTab] = useState<Tab>("empresa");
  const [company, setCompany] = useState<Company | null>(null);
  const [branches, setBranches] = useState<Branch[]>([]);
  const [taxes, setTaxes] = useState<Tax[]>([]);
  const [consecutives, setConsecutives] = useState<Consecutive[]>([]);
  const [parameters, setParameters] = useState<OperationalParameter[]>([]);
  const [message, setMessage] = useState("");

  useEffect(() => {
    const me = currentUser();
    if (!me) router.push("/login");
    setUser(me);
    void load();
  }, [router]);

  async function load() {
    const [companies, branchList, taxList, consecutiveList, parameterList] = await Promise.all([
      api<Company[]>("/companies"),
      api<Branch[]>("/branches"),
      api<Tax[]>("/taxes"),
      api<Consecutive[]>("/consecutives"),
      api<OperationalParameter[]>("/operational-parameters")
    ]);
    setCompany(companies[0] ?? null);
    setBranches(branchList);
    setTaxes(taxList);
    setConsecutives(consecutiveList);
    setParameters(parameterList);
  }

  async function saveCompany(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!company) return;
    const form = new FormData(event.currentTarget);
    const updated = await api<Company>(`/companies/${company.id}`, {
      method: "PUT",
      body: JSON.stringify(Object.fromEntries(form))
    });
    setCompany(updated);
    setMessage("Empresa actualizada");
  }

  if (!user) return null;

  return (
    <AppLayout user={user}>
      <h1>Configuracion</h1>
      <div className="tabs">
        {(["empresa", "sucursales", "impuestos", "consecutivos", "parametros"] as Tab[]).map((item) => (
          <button className="tab" data-active={tab === item} key={item} onClick={() => setTab(item)}>
            {label(item)}
          </button>
        ))}
      </div>
      {message ? <p className="muted">{message}</p> : null}
      {tab === "empresa" && company ? (
        <form className="panel stack" onSubmit={saveCompany}>
          <h2>Empresa</h2>
          <div className="split">
            <Field name="nombre" label="Nombre" defaultValue={company.nombre} />
            <Field name="nit" label="NIT" defaultValue={company.nit ?? ""} />
            <Field name="moneda" label="Moneda" defaultValue={company.moneda} />
            <Field name="zonaHoraria" label="Zona horaria" defaultValue={company.zonaHoraria} />
            <Field name="telefono" label="Telefono" defaultValue={company.telefono ?? ""} />
            <Field name="email" label="Email" defaultValue={company.email ?? ""} />
            <Field name="direccion" label="Direccion" defaultValue={company.direccion ?? ""} />
            <Field name="estado" label="Estado" defaultValue={company.estado} />
          </div>
          <button className="primary" type="submit">Guardar empresa</button>
        </form>
      ) : null}
      {tab === "sucursales" ? <BranchesPanel branches={branches} onReload={load} /> : null}
      {tab === "impuestos" ? <TaxesPanel taxes={taxes} onReload={load} /> : null}
      {tab === "consecutivos" ? <ConsecutivesPanel branches={branches} consecutives={consecutives} onReload={load} /> : null}
      {tab === "parametros" ? <ParametersPanel branches={branches} parameters={parameters} onReload={load} /> : null}
    </AppLayout>
  );
}

function TaxesPanel({ taxes, onReload }: { taxes: Tax[]; onReload: () => Promise<void> }) {
  async function createTax(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await api<Tax>("/taxes", {
      method: "POST",
      body: JSON.stringify({
        nombre: form.get("nombre"),
        codigo: form.get("codigo"),
        porcentaje: Number(form.get("porcentaje") ?? 0),
        incluido: form.get("incluido") === "on",
        activo: true
      })
    });
    event.currentTarget.reset();
    await onReload();
  }

  return (
    <div className="settings-grid">
      <form className="panel stack" onSubmit={createTax}>
        <h2>Nuevo impuesto</h2>
        <Field name="nombre" label="Nombre" />
        <Field name="codigo" label="Codigo" />
        <Field name="porcentaje" label="Porcentaje" />
        <label className="checkbox"><input name="incluido" type="checkbox" /> Incluido en precio</label>
        <button className="primary" type="submit">Crear impuesto</button>
      </form>
      <SimpleTable title="Impuestos" empty="No hay impuestos configurados." rows={taxes.map((tax) => [tax.codigo, tax.nombre, `${tax.porcentaje}%`, tax.activo ? "Activo" : "Inactivo"])} />
    </div>
  );
}

function ConsecutivesPanel({ branches, consecutives, onReload }: { branches: Branch[]; consecutives: Consecutive[]; onReload: () => Promise<void> }) {
  async function createConsecutive(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const sucursalId = String(form.get("sucursalId") ?? "");
    await api<Consecutive>("/consecutives", {
      method: "POST",
      body: JSON.stringify({
        sucursalId: sucursalId || null,
        documento: form.get("documento"),
        prefijo: form.get("prefijo"),
        siguienteNumero: Number(form.get("siguienteNumero") ?? 1),
        longitud: Number(form.get("longitud") ?? 6),
        activo: true
      })
    });
    event.currentTarget.reset();
    await onReload();
  }

  return (
    <div className="settings-grid">
      <form className="panel stack" onSubmit={createConsecutive}>
        <h2>Nuevo consecutivo</h2>
        <SelectBranch branches={branches} />
        <Field name="documento" label="Documento" />
        <Field name="prefijo" label="Prefijo" />
        <Field name="siguienteNumero" label="Siguiente numero" defaultValue="1" />
        <Field name="longitud" label="Longitud" defaultValue="6" />
        <button className="primary" type="submit">Crear consecutivo</button>
      </form>
      <SimpleTable title="Consecutivos" empty="No hay consecutivos configurados." rows={consecutives.map((item) => [item.documento, item.prefijo, String(item.siguienteNumero), item.vistaPrevia])} />
    </div>
  );
}

function ParametersPanel({ branches, parameters, onReload }: { branches: Branch[]; parameters: OperationalParameter[]; onReload: () => Promise<void> }) {
  async function createParameter(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    const sucursalId = String(form.get("sucursalId") ?? "");
    await api<OperationalParameter>("/operational-parameters", {
      method: "POST",
      body: JSON.stringify({
        sucursalId: sucursalId || null,
        clave: form.get("clave"),
        valor: form.get("valor"),
        tipo: form.get("tipo"),
        descripcion: form.get("descripcion")
      })
    });
    event.currentTarget.reset();
    await onReload();
  }

  return (
    <div className="settings-grid">
      <form className="panel stack" onSubmit={createParameter}>
        <h2>Nuevo parametro</h2>
        <SelectBranch branches={branches} />
        <Field name="clave" label="Clave" />
        <Field name="valor" label="Valor" />
        <Field name="tipo" label="Tipo" defaultValue="TEXTO" />
        <Field name="descripcion" label="Descripcion" />
        <button className="primary" type="submit">Crear parametro</button>
      </form>
      <SimpleTable title="Parametros" empty="No hay parametros operativos configurados." rows={parameters.map((item) => [item.clave, item.valor, item.tipo, item.descripcion ?? ""])} />
    </div>
  );
}

function SelectBranch({ branches }: { branches: Branch[] }) {
  return (
    <div className="field">
      <label htmlFor="sucursalId">Sucursal</label>
      <div className="input-row">
        <select id="sucursalId" name="sucursalId" defaultValue="">
          <option value="">Empresa completa</option>
          {branches.map((branch) => <option key={branch.id} value={branch.id}>{branch.nombre}</option>)}
        </select>
      </div>
    </div>
  );
}

function BranchesPanel({ branches, onReload }: { branches: Branch[]; onReload: () => Promise<void> }) {
  async function createBranch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const form = new FormData(event.currentTarget);
    await api<Branch>("/branches", {
      method: "POST",
      body: JSON.stringify({ ...Object.fromEntries(form), permiteOperacion: true, estado: "ACTIVA" })
    });
    event.currentTarget.reset();
    await onReload();
  }

  return (
    <div className="settings-grid">
      <form className="panel stack" onSubmit={createBranch}>
        <h2>Nueva sucursal</h2>
        <Field name="nombre" label="Nombre" />
        <Field name="codigo" label="Codigo" />
        <Field name="telefono" label="Telefono" />
        <Field name="email" label="Email" />
        <Field name="direccion" label="Direccion" />
        <button className="primary" type="submit">Crear sucursal</button>
      </form>
      <SimpleTable title="Sucursales" empty="No hay sucursales configuradas." rows={branches.map((branch) => [branch.codigo ?? "", branch.nombre, branch.estado, branch.permiteOperacion ? "Operativa" : "Bloqueada"])} />
    </div>
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

function SimpleTable({ title, empty, rows }: { title: string; empty: string; rows: string[][] }) {
  return (
    <section className="panel">
      <h2>{title}</h2>
      {rows.length === 0 ? (
        <p className="muted">{empty}</p>
      ) : (
        <table className="table">
          <tbody>
            {rows.map((row, index) => (
              <tr key={index}>
                {row.map((cell, cellIndex) => <td key={cellIndex}>{cell}</td>)}
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}

function label(tab: Tab) {
  return {
    empresa: "Empresa",
    sucursales: "Sucursales",
    impuestos: "Impuestos",
    consecutivos: "Consecutivos",
    parametros: "Parametros"
  }[tab];
}
