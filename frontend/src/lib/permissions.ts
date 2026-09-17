const menuItems = [
  ["Dashboard", "/", "DASHBOARD:VER"],
  ["Clientes", "/clientes", "CLIENTES:VER"],
  ["Vehiculos", "/vehiculos", "VEHICULOS:VER"],
  ["Citas", "/citas", "CITAS:VER"],
  ["Recepcion", "/recepcion", "RECEPCION:VER"],
  ["Diagnosticos", "/diagnosticos", "DIAGNOSTICOS:VER"],
  ["Ordenes de trabajo", "/ordenes", "ORDENES:VER"],
  ["Kanban", "/kanban", "KANBAN:VER"],
  ["Tecnicos", "/tecnicos", "TECNICOS:VER"],
  ["Inventario", "/inventario", "INVENTARIO:VER"],
  ["Compras", "/compras", "COMPRAS:VER"],
  ["Facturacion", "/facturacion", "FACTURACION:VER"],
  ["Caja", "/caja", "CAJA:VER"],
  ["Reportes", "/reportes", "REPORTES:VER"],
  ["Configuracion", "/configuracion", "CONFIGURACION:VER"]
] as const;

export function allowedMenu(permisos: string[]) {
  return menuItems.filter(([, , permission]) => permisos.includes(permission));
}
