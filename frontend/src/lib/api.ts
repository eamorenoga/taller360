export type BranchOption = { id: string; nombre: string };
export type Company = {
  id: string;
  nombre: string;
  nit: string | null;
  estado: string;
  moneda: string;
  zonaHoraria: string;
  email: string | null;
  telefono: string | null;
  direccion: string | null;
};
export type Branch = BranchOption & {
  empresaId: string;
  codigo: string | null;
  direccion: string | null;
  telefono: string | null;
  email: string | null;
  moneda: string | null;
  zonaHoraria: string | null;
  permiteOperacion: boolean;
  estado: string;
};
export type Tax = { id: string; nombre: string; codigo: string; porcentaje: number; incluido: boolean; activo: boolean };
export type Consecutive = {
  id: string;
  sucursalId: string | null;
  documento: string;
  prefijo: string;
  siguienteNumero: number;
  longitud: number;
  activo: boolean;
  vistaPrevia: string;
};
export type OperationalParameter = {
  id: string;
  sucursalId: string | null;
  clave: string;
  valor: string;
  tipo: string;
  descripcion: string | null;
};
export type UserSummary = {
  id: string;
  empresaId: string;
  nombre: string;
  apellido: string;
  email: string;
  telefono: string | null;
  estado: string;
  roles: RoleAssignment[];
  sucursales: string[];
};
export type RoleAssignment = { roleId: string; rol: string; branchId: string | null; sucursal: string };
export type RoleSummary = {
  id: string;
  empresaId: string | null;
  nombre: string;
  descripcion: string | null;
  estado: string;
  alcance: string;
  configurable: boolean;
  permisos: string[];
};
export type PermissionSummary = { id: string; modulo: string; accion: string; code: string; descripcion: string | null };
export type PermissionMatrix = {
  recursos: string[];
  acciones: string[];
  roles: { roleId: string; nombre: string; alcance: string; permisos: string[] }[];
};
export type Client = {
  id: string;
  empresaId: string;
  sucursalId: string | null;
  tipoPersona: string;
  tipoIdentificacion: string | null;
  identificacion: string | null;
  nombre: string;
  razonSocial: string | null;
  telefonoPrincipal: string | null;
  telefonoSecundario: string | null;
  whatsapp: string | null;
  email: string | null;
  direccion: string | null;
  notas: string | null;
  preferencias: string | null;
  estado: string;
};
export type Client360 = {
  cliente: Client;
  contactos: { id: string; nombre: string; relacion: string | null; telefono: string | null; whatsapp: string | null; email: string | null; principal: boolean; notas: string | null; estado: string }[];
  documentos: { id: string; tipo: string; nombreArchivo: string; url: string; notas: string | null; estado: string; fechaCreacion: string }[];
  comunicaciones: { id: string; canal: string; direccion: string | null; asunto: string | null; contenido: string | null; estado: string; fechaHora: string }[];
  vehiculos: RelatedItem[];
  citas: RelatedItem[];
  ordenesTrabajo: RelatedItem[];
  facturas: RelatedItem[];
  pagos: RelatedItem[];
  cartera: { estado: string; saldoPendiente: number; moneda: string; documentos: RelatedItem[] };
};
export type RelatedItem = { id: string; titulo: string; estado: string; referencia: string; fecha: string };
export type Vehicle = {
  id: string;
  empresaId: string;
  clienteId: string;
  cliente: string;
  sucursalId: string | null;
  placa: string;
  vin: string | null;
  marca: string;
  modelo: string;
  version: string | null;
  anio: number | null;
  motor: string | null;
  combustible: string | null;
  transmision: string | null;
  color: string | null;
  kilometraje: number;
  estado: string;
  notas: string | null;
};
export type Vehicle360 = {
  vehiculo: Vehicle;
  documentos: { id: string; tipo: string; nombreArchivo: string; url: string; venceEn: string | null; estado: string; notas: string | null; fechaCreacion: string }[];
  fotos: { id: string; url: string; descripcion: string | null; principal: boolean; fechaCreacion: string }[];
  garantias: { id: string; tipo: string; descripcion: string | null; proveedor: string | null; iniciaEn: string | null; venceEn: string | null; kilometrajeLimite: number | null; estado: string; fechaCreacion: string }[];
  timeline: { id: string; modulo: string; titulo: string; descripcion: string | null; referenciaId: string | null; estado: string | null; fechaHora: string }[];
};
export type Me = {
  id: string;
  empresaId: string;
  empresa: string;
  sucursalId: string | null;
  nombre: string;
  apellido: string;
  email: string;
  sucursales: BranchOption[];
  roles: string[];
  permisos: string[];
};

export type LoginResponse = {
  accessToken: string;
  refreshToken: string;
  expiresAt: string;
  user: Me;
};

const DEFAULT_RENDER_API_URL = "https://taller360.onrender.com/api/v1";

function apiUrl() {
  if (process.env.NEXT_PUBLIC_API_URL) {
    return process.env.NEXT_PUBLIC_API_URL;
  }
  if (typeof window !== "undefined" && window.location.hostname.endsWith(".onrender.com")) {
    return DEFAULT_RENDER_API_URL;
  }
  return "http://localhost:8080/api/v1";
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const token = typeof window !== "undefined" ? localStorage.getItem("accessToken") : null;
  let response: Response;
  try {
    response = await fetch(`${apiUrl()}${path}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...options.headers
      }
    });
  } catch {
    throw new Error("No se pudo conectar con la API. Verifica NEXT_PUBLIC_API_URL y CORS del backend.");
  }
  if (!response.ok) {
    const body = await response.json().catch(() => ({ message: "Error del servidor" }));
    throw new Error(body.message ?? "Error del servidor");
  }
  if (response.status === 204) {
    return undefined as T;
  }
  return response.json();
}

export function saveSession(response: LoginResponse) {
  localStorage.setItem("accessToken", response.accessToken);
  localStorage.setItem("refreshToken", response.refreshToken);
  localStorage.setItem("me", JSON.stringify(response.user));
}

export async function switchBranch(branchId: string) {
  const refreshToken = localStorage.getItem("refreshToken");
  const response = await api<LoginResponse>("/auth/refresh", {
    method: "POST",
    body: JSON.stringify({ refreshToken, branchId })
  });
  saveSession(response);
  return response.user;
}

export function currentUser(): Me | null {
  if (typeof window === "undefined") return null;
  const raw = localStorage.getItem("me");
  return raw ? (JSON.parse(raw) as Me) : null;
}

export function logout() {
  localStorage.removeItem("accessToken");
  localStorage.removeItem("refreshToken");
  localStorage.removeItem("me");
}
