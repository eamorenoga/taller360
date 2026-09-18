CREATE TABLE cotizaciones (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  sucursal_id UUID NOT NULL REFERENCES sucursales(id) ON DELETE CASCADE,
  orden_trabajo_id UUID NOT NULL,
  cliente_id UUID NOT NULL REFERENCES clientes(id) ON DELETE RESTRICT,
  vehiculo_id UUID NOT NULL REFERENCES vehiculos(id) ON DELETE RESTRICT,
  version_actual INT NOT NULL DEFAULT 1,
  estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
  vence_en DATE,
  moneda VARCHAR(10) NOT NULL DEFAULT 'COP',
  subtotal NUMERIC(14,2) NOT NULL DEFAULT 0,
  descuento_total NUMERIC(14,2) NOT NULL DEFAULT 0,
  impuesto_total NUMERIC(14,2) NOT NULL DEFAULT 0,
  total NUMERIC(14,2) NOT NULL DEFAULT 0,
  public_token VARCHAR(80) NOT NULL,
  pdf_url TEXT,
  trabajo_generado_id UUID,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (public_token),
  CHECK (version_actual > 0),
  CHECK (subtotal >= 0),
  CHECK (descuento_total >= 0),
  CHECK (impuesto_total >= 0),
  CHECK (total >= 0)
);
CREATE INDEX idx_cotizaciones_empresa_fecha ON cotizaciones(empresa_id, fecha_creacion DESC);
CREATE INDEX idx_cotizaciones_sucursal_estado ON cotizaciones(sucursal_id, estado);
CREATE INDEX idx_cotizaciones_ot ON cotizaciones(empresa_id, orden_trabajo_id);

CREATE TABLE cotizacion_items (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  cotizacion_id UUID NOT NULL REFERENCES cotizaciones(id) ON DELETE CASCADE,
  version INT NOT NULL,
  tipo VARCHAR(30) NOT NULL,
  codigo VARCHAR(80),
  descripcion VARCHAR(260) NOT NULL,
  cantidad NUMERIC(12,2) NOT NULL DEFAULT 1,
  valor_unitario NUMERIC(14,2) NOT NULL DEFAULT 0,
  impuesto_porcentaje NUMERIC(7,4) NOT NULL DEFAULT 0,
  descuento_porcentaje NUMERIC(7,4) NOT NULL DEFAULT 0,
  subtotal NUMERIC(14,2) NOT NULL DEFAULT 0,
  descuento NUMERIC(14,2) NOT NULL DEFAULT 0,
  impuesto NUMERIC(14,2) NOT NULL DEFAULT 0,
  total NUMERIC(14,2) NOT NULL DEFAULT 0,
  estado_aprobacion VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (cantidad > 0),
  CHECK (valor_unitario >= 0),
  CHECK (impuesto_porcentaje >= 0),
  CHECK (descuento_porcentaje >= 0)
);
CREATE INDEX idx_cotizacion_items_cotizacion ON cotizacion_items(cotizacion_id);

CREATE TABLE cotizacion_versiones (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  cotizacion_id UUID NOT NULL REFERENCES cotizaciones(id) ON DELETE CASCADE,
  version INT NOT NULL,
  snapshot_json TEXT NOT NULL,
  subtotal NUMERIC(14,2) NOT NULL DEFAULT 0,
  descuento_total NUMERIC(14,2) NOT NULL DEFAULT 0,
  impuesto_total NUMERIC(14,2) NOT NULL DEFAULT 0,
  total NUMERIC(14,2) NOT NULL DEFAULT 0,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (cotizacion_id, version)
);
CREATE INDEX idx_cotizacion_versiones_cotizacion ON cotizacion_versiones(cotizacion_id, version DESC);

CREATE TABLE cotizacion_aprobaciones (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  cotizacion_id UUID NOT NULL REFERENCES cotizaciones(id) ON DELETE CASCADE,
  item_id UUID REFERENCES cotizacion_items(id) ON DELETE CASCADE,
  accion VARCHAR(30) NOT NULL,
  aprobador_nombre VARCHAR(180) NOT NULL,
  aprobador_email VARCHAR(180),
  evidencia_json TEXT NOT NULL DEFAULT '{}',
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_cotizacion_aprobaciones_cotizacion ON cotizacion_aprobaciones(cotizacion_id, fecha_creacion DESC);

CREATE TABLE cotizacion_pdfs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  cotizacion_id UUID NOT NULL REFERENCES cotizaciones(id) ON DELETE CASCADE,
  version INT NOT NULL,
  url TEXT NOT NULL,
  contenido_base64 TEXT NOT NULL,
  hash_contenido VARCHAR(128) NOT NULL,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (cotizacion_id, version)
);

INSERT INTO permisos (modulo, accion)
SELECT 'COTIZACIONES', accion
FROM unnest(ARRAY['VER','CREAR','EDITAR','ELIMINAR','APROBAR','ASIGNAR','EXPORTAR','ANULAR']) AS accion
ON CONFLICT (modulo, accion) DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
JOIN permisos p ON p.modulo = 'COTIZACIONES'
WHERE r.nombre IN ('ADMINISTRADOR', 'SUPER_ADMIN')
ON CONFLICT DO NOTHING;
