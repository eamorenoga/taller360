CREATE TABLE diagnosticos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  sucursal_id UUID NOT NULL REFERENCES sucursales(id) ON DELETE CASCADE,
  orden_trabajo_id UUID NOT NULL,
  recepcion_id UUID REFERENCES recepciones(id) ON DELETE SET NULL,
  cliente_id UUID NOT NULL REFERENCES clientes(id) ON DELETE RESTRICT,
  vehiculo_id UUID NOT NULL REFERENCES vehiculos(id) ON DELETE RESTRICT,
  tecnico_id UUID REFERENCES tecnicos(id) ON DELETE SET NULL,
  sintomas_json TEXT NOT NULL DEFAULT '[]',
  inspecciones_json TEXT NOT NULL DEFAULT '[]',
  dtc_json TEXT NOT NULL DEFAULT '[]',
  pruebas_json TEXT NOT NULL DEFAULT '[]',
  hallazgos_json TEXT NOT NULL DEFAULT '[]',
  causa_probable TEXT,
  solucion_recomendada TEXT,
  prioridad VARCHAR(30) NOT NULL DEFAULT 'MEDIA',
  estado VARCHAR(30) NOT NULL DEFAULT 'BORRADOR',
  tiempo_estimado_minutos INT NOT NULL DEFAULT 0,
  ia_sugerencia_json TEXT,
  ia_confirmada_por UUID REFERENCES usuarios(id) ON DELETE SET NULL,
  ia_confirmada_en TIMESTAMPTZ,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (tiempo_estimado_minutos >= 0)
);
CREATE INDEX idx_diagnosticos_empresa_fecha ON diagnosticos(empresa_id, fecha_creacion DESC);
CREATE INDEX idx_diagnosticos_sucursal_estado ON diagnosticos(sucursal_id, estado);
CREATE INDEX idx_diagnosticos_ot ON diagnosticos(empresa_id, orden_trabajo_id);
CREATE INDEX idx_diagnosticos_vehiculo ON diagnosticos(vehiculo_id);

CREATE TABLE diagnostico_evidencias (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  diagnostico_id UUID NOT NULL REFERENCES diagnosticos(id) ON DELETE CASCADE,
  tipo VARCHAR(30) NOT NULL,
  url TEXT NOT NULL,
  nombre_archivo VARCHAR(220) NOT NULL,
  mime_type VARCHAR(120),
  tamano_bytes BIGINT NOT NULL DEFAULT 0,
  metadatos_json TEXT NOT NULL DEFAULT '{}',
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (tamano_bytes >= 0)
);
CREATE INDEX idx_diagnostico_evidencias_diagnostico ON diagnostico_evidencias(diagnostico_id, fecha_creacion DESC);

CREATE TABLE diagnostico_tareas (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  diagnostico_id UUID NOT NULL REFERENCES diagnosticos(id) ON DELETE CASCADE,
  descripcion VARCHAR(260) NOT NULL,
  prioridad VARCHAR(30) NOT NULL DEFAULT 'MEDIA',
  estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
  tiempo_estimado_minutos INT NOT NULL DEFAULT 0,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (tiempo_estimado_minutos >= 0)
);
CREATE INDEX idx_diagnostico_tareas_diagnostico ON diagnostico_tareas(diagnostico_id);

CREATE TABLE diagnostico_repuestos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  diagnostico_id UUID NOT NULL REFERENCES diagnosticos(id) ON DELETE CASCADE,
  codigo VARCHAR(80),
  nombre VARCHAR(180) NOT NULL,
  cantidad NUMERIC(12,2) NOT NULL DEFAULT 1,
  requerido BOOLEAN NOT NULL DEFAULT true,
  notas TEXT,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK (cantidad > 0)
);
CREATE INDEX idx_diagnostico_repuestos_diagnostico ON diagnostico_repuestos(diagnostico_id);

INSERT INTO permisos (modulo, accion)
SELECT 'DIAGNOSTICOS', accion
FROM unnest(ARRAY['VER','CREAR','EDITAR','ELIMINAR','APROBAR','ASIGNAR','EXPORTAR','ANULAR']) AS accion
ON CONFLICT (modulo, accion) DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT r.id, p.id
FROM roles r
JOIN permisos p ON p.modulo = 'DIAGNOSTICOS'
WHERE r.nombre IN ('ADMINISTRADOR', 'SUPER_ADMIN')
ON CONFLICT DO NOTHING;
