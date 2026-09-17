ALTER TABLE empresas
  ADD COLUMN moneda VARCHAR(3) NOT NULL DEFAULT 'COP',
  ADD COLUMN zona_horaria VARCHAR(80) NOT NULL DEFAULT 'America/Bogota',
  ADD COLUMN email VARCHAR(180),
  ADD COLUMN telefono VARCHAR(40),
  ADD COLUMN direccion VARCHAR(250);

ALTER TABLE sucursales
  ADD COLUMN codigo VARCHAR(40),
  ADD COLUMN moneda VARCHAR(3),
  ADD COLUMN zona_horaria VARCHAR(80),
  ADD COLUMN email VARCHAR(180),
  ADD COLUMN permite_operacion BOOLEAN NOT NULL DEFAULT true;

CREATE UNIQUE INDEX ux_sucursales_empresa_codigo
  ON sucursales(empresa_id, codigo)
  WHERE codigo IS NOT NULL;

CREATE TABLE empresa_impuestos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  nombre VARCHAR(120) NOT NULL,
  codigo VARCHAR(40) NOT NULL,
  porcentaje NUMERIC(7,4) NOT NULL,
  incluido BOOLEAN NOT NULL DEFAULT false,
  activo BOOLEAN NOT NULL DEFAULT true,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (empresa_id, codigo),
  CHECK (porcentaje >= 0)
);
CREATE INDEX idx_empresa_impuestos_empresa ON empresa_impuestos(empresa_id);

CREATE TABLE consecutivos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  sucursal_id UUID REFERENCES sucursales(id) ON DELETE CASCADE,
  documento VARCHAR(80) NOT NULL,
  prefijo VARCHAR(20) NOT NULL DEFAULT '',
  siguiente_numero BIGINT NOT NULL DEFAULT 1,
  longitud INT NOT NULL DEFAULT 6,
  activo BOOLEAN NOT NULL DEFAULT true,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (empresa_id, sucursal_id, documento),
  CHECK (siguiente_numero > 0),
  CHECK (longitud BETWEEN 1 AND 20)
);
CREATE INDEX idx_consecutivos_empresa ON consecutivos(empresa_id);
CREATE INDEX idx_consecutivos_sucursal ON consecutivos(sucursal_id);

CREATE TABLE parametros_operativos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id) ON DELETE CASCADE,
  sucursal_id UUID REFERENCES sucursales(id) ON DELETE CASCADE,
  clave VARCHAR(120) NOT NULL,
  valor TEXT NOT NULL,
  tipo VARCHAR(30) NOT NULL DEFAULT 'TEXTO',
  descripcion VARCHAR(250),
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (empresa_id, sucursal_id, clave)
);
CREATE INDEX idx_parametros_empresa ON parametros_operativos(empresa_id);
CREATE INDEX idx_parametros_sucursal ON parametros_operativos(sucursal_id);

INSERT INTO permisos (modulo, accion)
SELECT modulo, accion
FROM unnest(ARRAY['EMPRESAS','IMPUESTOS','CONSECUTIVOS','PARAMETROS']) AS modulo
CROSS JOIN unnest(ARRAY['VER','CREAR','EDITAR','ELIMINAR','EXPORTAR']) AS accion
ON CONFLICT (modulo, accion) DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT '33333333-3333-3333-3333-333333333332', p.id
FROM permisos p
WHERE p.modulo IN ('EMPRESAS','IMPUESTOS','CONSECUTIVOS','PARAMETROS')
ON CONFLICT DO NOTHING;

UPDATE empresas
SET moneda = 'COP',
    zona_horaria = 'America/Bogota',
    telefono = COALESCE(telefono, '+570000000000')
WHERE id = '11111111-1111-1111-1111-111111111111';

UPDATE sucursales
SET codigo = CASE nombre
    WHEN 'Sucursal Norte' THEN 'NORTE'
    WHEN 'Sucursal Centro' THEN 'CENTRO'
    WHEN 'Sucursal Sur' THEN 'SUR'
    ELSE upper(replace(nombre, ' ', '_'))
  END,
  moneda = 'COP',
  zona_horaria = 'America/Bogota'
WHERE empresa_id = '11111111-1111-1111-1111-111111111111';
