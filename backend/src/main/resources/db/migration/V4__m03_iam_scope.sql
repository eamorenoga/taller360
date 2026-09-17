ALTER TABLE roles
  ADD COLUMN estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
  ADD COLUMN alcance VARCHAR(30) NOT NULL DEFAULT 'EMPRESA';

CREATE TABLE usuario_roles_sucursal (
  usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
  rol_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
  sucursal_id UUID REFERENCES sucursales(id) ON DELETE CASCADE,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (usuario_id, rol_id, sucursal_id)
);
CREATE INDEX idx_usuario_roles_sucursal_usuario ON usuario_roles_sucursal(usuario_id);
CREATE INDEX idx_usuario_roles_sucursal_sucursal ON usuario_roles_sucursal(sucursal_id);

INSERT INTO permisos (modulo, accion)
SELECT modulo, accion
FROM unnest(ARRAY['PERMISOS']) AS modulo
CROSS JOIN unnest(ARRAY['VER','CREAR','EDITAR','ELIMINAR','APROBAR','ASIGNAR','EXPORTAR','ANULAR']) AS accion
ON CONFLICT (modulo, accion) DO NOTHING;

INSERT INTO rol_permisos (rol_id, permiso_id)
SELECT '33333333-3333-3333-3333-333333333332', p.id
FROM permisos p
WHERE p.modulo IN ('PERMISOS')
ON CONFLICT DO NOTHING;
