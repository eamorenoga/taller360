CREATE TABLE clientes (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  sucursal_id UUID REFERENCES sucursales(id) ON DELETE SET NULL,
  tipo_persona VARCHAR(30) NOT NULL,
  tipo_identificacion VARCHAR(30),
  identificacion VARCHAR(80),
  nombre VARCHAR(180) NOT NULL,
  razon_social VARCHAR(220),
  telefono_principal VARCHAR(40),
  telefono_secundario VARCHAR(40),
  whatsapp VARCHAR(40),
  email VARCHAR(180),
  direccion VARCHAR(250),
  notas TEXT,
  preferencias TEXT,
  estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (empresa_id, tipo_identificacion, identificacion)
);
CREATE INDEX idx_clientes_empresa_nombre ON clientes(empresa_id, nombre);
CREATE INDEX idx_clientes_empresa_identificacion ON clientes(empresa_id, identificacion);
CREATE INDEX idx_clientes_empresa_telefono ON clientes(empresa_id, telefono_principal);
CREATE INDEX idx_clientes_empresa_email ON clientes(empresa_id, email);
CREATE INDEX idx_clientes_sucursal ON clientes(sucursal_id);

CREATE TABLE cliente_contactos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  cliente_id UUID NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
  nombre VARCHAR(160) NOT NULL,
  relacion VARCHAR(80),
  telefono VARCHAR(40),
  whatsapp VARCHAR(40),
  email VARCHAR(180),
  principal BOOLEAN NOT NULL DEFAULT false,
  notas TEXT,
  estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_cliente_contactos_cliente ON cliente_contactos(cliente_id);
CREATE INDEX idx_cliente_contactos_empresa ON cliente_contactos(empresa_id);

CREATE TABLE cliente_documentos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  cliente_id UUID NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
  tipo VARCHAR(80) NOT NULL,
  nombre_archivo VARCHAR(220) NOT NULL,
  url TEXT NOT NULL,
  notas TEXT,
  estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_cliente_documentos_cliente ON cliente_documentos(cliente_id);
CREATE INDEX idx_cliente_documentos_empresa ON cliente_documentos(empresa_id);

CREATE TABLE cliente_comunicaciones (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  cliente_id UUID NOT NULL REFERENCES clientes(id) ON DELETE CASCADE,
  canal VARCHAR(40) NOT NULL,
  direccion VARCHAR(180),
  asunto VARCHAR(180),
  contenido TEXT,
  estado VARCHAR(30) NOT NULL DEFAULT 'REGISTRADA',
  fecha_hora TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_cliente_comunicaciones_cliente ON cliente_comunicaciones(cliente_id, fecha_hora DESC);
CREATE INDEX idx_cliente_comunicaciones_empresa ON cliente_comunicaciones(empresa_id);

INSERT INTO permisos (modulo, accion)
SELECT 'CLIENTES', accion
FROM unnest(ARRAY['VER','CREAR','EDITAR','ELIMINAR','APROBAR','ASIGNAR','EXPORTAR','ANULAR']) AS accion
ON CONFLICT (modulo, accion) DO NOTHING;
