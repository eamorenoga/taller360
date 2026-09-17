CREATE TABLE vehiculos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  sucursal_id UUID REFERENCES sucursales(id) ON DELETE SET NULL,
  cliente_id UUID NOT NULL REFERENCES clientes(id) ON DELETE RESTRICT,
  placa VARCHAR(20) NOT NULL,
  vin VARCHAR(80),
  marca VARCHAR(120) NOT NULL,
  modelo VARCHAR(120) NOT NULL,
  version VARCHAR(120),
  anio INT,
  motor VARCHAR(120),
  combustible VARCHAR(60),
  transmision VARCHAR(60),
  color VARCHAR(80),
  kilometraje BIGINT NOT NULL DEFAULT 0,
  estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
  notas TEXT,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (empresa_id, placa),
  UNIQUE (empresa_id, vin),
  CHECK (kilometraje >= 0),
  CHECK (anio IS NULL OR anio BETWEEN 1900 AND 2100)
);
CREATE INDEX idx_vehiculos_empresa_cliente ON vehiculos(empresa_id, cliente_id);
CREATE INDEX idx_vehiculos_empresa_placa ON vehiculos(empresa_id, placa);
CREATE INDEX idx_vehiculos_empresa_vin ON vehiculos(empresa_id, vin);
CREATE INDEX idx_vehiculos_sucursal ON vehiculos(sucursal_id);

CREATE TABLE vehiculo_documentos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  vehiculo_id UUID NOT NULL REFERENCES vehiculos(id) ON DELETE CASCADE,
  tipo VARCHAR(80) NOT NULL,
  nombre_archivo VARCHAR(220) NOT NULL,
  url TEXT NOT NULL,
  vence_en DATE,
  estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
  notas TEXT,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_vehiculo_documentos_vehiculo ON vehiculo_documentos(vehiculo_id);

CREATE TABLE vehiculo_fotos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  vehiculo_id UUID NOT NULL REFERENCES vehiculos(id) ON DELETE CASCADE,
  url TEXT NOT NULL,
  descripcion VARCHAR(250),
  principal BOOLEAN NOT NULL DEFAULT false,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_vehiculo_fotos_vehiculo ON vehiculo_fotos(vehiculo_id);

CREATE TABLE vehiculo_garantias (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  vehiculo_id UUID NOT NULL REFERENCES vehiculos(id) ON DELETE CASCADE,
  tipo VARCHAR(80) NOT NULL,
  descripcion TEXT,
  proveedor VARCHAR(160),
  inicia_en DATE,
  vence_en DATE,
  kilometraje_limite BIGINT,
  estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVA',
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (kilometraje_limite IS NULL OR kilometraje_limite >= 0)
);
CREATE INDEX idx_vehiculo_garantias_vehiculo ON vehiculo_garantias(vehiculo_id);

CREATE TABLE vehiculo_timeline (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  vehiculo_id UUID NOT NULL REFERENCES vehiculos(id) ON DELETE CASCADE,
  modulo VARCHAR(80) NOT NULL,
  titulo VARCHAR(180) NOT NULL,
  descripcion TEXT,
  referencia_id UUID,
  estado VARCHAR(60),
  fecha_hora TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_vehiculo_timeline_vehiculo_fecha ON vehiculo_timeline(vehiculo_id, fecha_hora DESC);

INSERT INTO permisos (modulo, accion)
SELECT 'VEHICULOS', accion
FROM unnest(ARRAY['VER','CREAR','EDITAR','ELIMINAR','APROBAR','ASIGNAR','EXPORTAR','ANULAR']) AS accion
ON CONFLICT (modulo, accion) DO NOTHING;
