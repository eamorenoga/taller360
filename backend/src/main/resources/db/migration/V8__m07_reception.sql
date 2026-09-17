CREATE TABLE recepciones (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  sucursal_id UUID NOT NULL REFERENCES sucursales(id) ON DELETE CASCADE,
  cita_id UUID REFERENCES citas(id) ON DELETE SET NULL,
  cliente_id UUID NOT NULL REFERENCES clientes(id) ON DELETE RESTRICT,
  vehiculo_id UUID NOT NULL REFERENCES vehiculos(id) ON DELETE RESTRICT,
  asesor_id UUID REFERENCES usuarios(id) ON DELETE SET NULL,
  kilometraje BIGINT NOT NULL,
  combustible_porcentaje INT NOT NULL DEFAULT 0,
  motivo VARCHAR(220) NOT NULL,
  accesorios_json TEXT NOT NULL DEFAULT '[]',
  checklist_json TEXT NOT NULL DEFAULT '[]',
  danos_json TEXT NOT NULL DEFAULT '[]',
  observaciones TEXT,
  estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
  version_firmada INT,
  firmado_en TIMESTAMPTZ,
  firmado_por VARCHAR(180),
  firma_url TEXT,
  pdf_url TEXT,
  orden_trabajo_id UUID,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (kilometraje >= 0),
  CHECK (combustible_porcentaje BETWEEN 0 AND 100)
);
CREATE INDEX idx_recepciones_empresa_fecha ON recepciones(empresa_id, fecha_creacion DESC);
CREATE INDEX idx_recepciones_sucursal_estado ON recepciones(sucursal_id, estado);
CREATE INDEX idx_recepciones_cliente ON recepciones(cliente_id);
CREATE INDEX idx_recepciones_vehiculo ON recepciones(vehiculo_id);
CREATE INDEX idx_recepciones_cita ON recepciones(cita_id);

CREATE TABLE recepcion_fotos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  recepcion_id UUID NOT NULL REFERENCES recepciones(id) ON DELETE CASCADE,
  url TEXT NOT NULL,
  nombre_archivo VARCHAR(220) NOT NULL,
  tipo VARCHAR(80) NOT NULL,
  tamano_original_bytes BIGINT NOT NULL DEFAULT 0,
  tamano_comprimido_bytes BIGINT NOT NULL DEFAULT 0,
  ancho INT,
  alto INT,
  metadatos_json TEXT NOT NULL DEFAULT '{}',
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (tamano_original_bytes >= 0),
  CHECK (tamano_comprimido_bytes >= 0)
);
CREATE INDEX idx_recepcion_fotos_recepcion ON recepcion_fotos(recepcion_id, fecha_creacion DESC);

CREATE TABLE recepcion_firmas (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  recepcion_id UUID NOT NULL REFERENCES recepciones(id) ON DELETE CASCADE,
  firmante VARCHAR(180) NOT NULL,
  firma_url TEXT NOT NULL,
  hash_contenido VARCHAR(128) NOT NULL,
  version INT NOT NULL,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (recepcion_id, version)
);
CREATE INDEX idx_recepcion_firmas_recepcion ON recepcion_firmas(recepcion_id);

CREATE TABLE recepcion_pdfs (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  recepcion_id UUID NOT NULL REFERENCES recepciones(id) ON DELETE CASCADE,
  version INT NOT NULL,
  url TEXT NOT NULL,
  contenido_base64 TEXT NOT NULL,
  hash_contenido VARCHAR(128) NOT NULL,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (recepcion_id, version)
);
CREATE INDEX idx_recepcion_pdfs_recepcion ON recepcion_pdfs(recepcion_id);

INSERT INTO permisos (modulo, accion)
SELECT 'RECEPCION', accion
FROM unnest(ARRAY['VER','CREAR','EDITAR','ELIMINAR','APROBAR','ASIGNAR','EXPORTAR','ANULAR']) AS accion
ON CONFLICT (modulo, accion) DO NOTHING;
