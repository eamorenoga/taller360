CREATE TABLE tecnicos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  sucursal_id UUID REFERENCES sucursales(id) ON DELETE SET NULL,
  usuario_id UUID REFERENCES usuarios(id) ON DELETE SET NULL,
  nombre VARCHAR(160) NOT NULL,
  especialidad VARCHAR(120),
  activo BOOLEAN NOT NULL DEFAULT true,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_tecnicos_empresa ON tecnicos(empresa_id);
CREATE INDEX idx_tecnicos_sucursal ON tecnicos(sucursal_id);

CREATE TABLE bahias (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  sucursal_id UUID NOT NULL REFERENCES sucursales(id) ON DELETE CASCADE,
  nombre VARCHAR(120) NOT NULL,
  tipo VARCHAR(80),
  activa BOOLEAN NOT NULL DEFAULT true,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (empresa_id, sucursal_id, nombre)
);
CREATE INDEX idx_bahias_empresa_sucursal ON bahias(empresa_id, sucursal_id);

CREATE TABLE capacidad_agenda (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  sucursal_id UUID NOT NULL REFERENCES sucursales(id) ON DELETE CASCADE,
  dia_semana INT NOT NULL,
  hora_inicio TIME NOT NULL,
  hora_fin TIME NOT NULL,
  capacidad_maxima INT NOT NULL DEFAULT 1,
  activo BOOLEAN NOT NULL DEFAULT true,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (dia_semana BETWEEN 1 AND 7),
  CHECK (capacidad_maxima > 0),
  CHECK (hora_fin > hora_inicio)
);
CREATE INDEX idx_capacidad_agenda_sucursal ON capacidad_agenda(empresa_id, sucursal_id, dia_semana);

CREATE TABLE citas (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  sucursal_id UUID NOT NULL REFERENCES sucursales(id) ON DELETE CASCADE,
  cliente_id UUID NOT NULL REFERENCES clientes(id) ON DELETE RESTRICT,
  vehiculo_id UUID NOT NULL REFERENCES vehiculos(id) ON DELETE RESTRICT,
  asesor_id UUID REFERENCES usuarios(id) ON DELETE SET NULL,
  tecnico_id UUID REFERENCES tecnicos(id) ON DELETE SET NULL,
  bahia_id UUID REFERENCES bahias(id) ON DELETE SET NULL,
  servicio VARCHAR(180) NOT NULL,
  fecha_inicio TIMESTAMPTZ NOT NULL,
  fecha_fin TIMESTAMPTZ NOT NULL,
  duracion_minutos INT NOT NULL,
  estado VARCHAR(30) NOT NULL DEFAULT 'PROGRAMADA',
  notas TEXT,
  sobrecapacidad BOOLEAN NOT NULL DEFAULT false,
  recepcion_id UUID,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (duracion_minutos > 0),
  CHECK (fecha_fin > fecha_inicio)
);
CREATE INDEX idx_citas_empresa_inicio ON citas(empresa_id, fecha_inicio);
CREATE INDEX idx_citas_sucursal_inicio ON citas(sucursal_id, fecha_inicio);
CREATE INDEX idx_citas_cliente ON citas(cliente_id);
CREATE INDEX idx_citas_vehiculo ON citas(vehiculo_id);
CREATE INDEX idx_citas_estado ON citas(estado);

CREATE TABLE cita_recordatorios (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  cita_id UUID NOT NULL REFERENCES citas(id) ON DELETE CASCADE,
  canal VARCHAR(40) NOT NULL,
  programado_para TIMESTAMPTZ NOT NULL,
  enviado_en TIMESTAMPTZ,
  estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
  mensaje TEXT,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_cita_recordatorios_cita ON cita_recordatorios(cita_id);
CREATE INDEX idx_cita_recordatorios_programado ON cita_recordatorios(empresa_id, programado_para);

INSERT INTO permisos (modulo, accion)
SELECT 'CITAS', accion
FROM unnest(ARRAY['VER','CREAR','EDITAR','ELIMINAR','APROBAR','ASIGNAR','EXPORTAR','ANULAR']) AS accion
ON CONFLICT (modulo, accion) DO NOTHING;
