CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE empresas (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nombre VARCHAR(180) NOT NULL,
  nit VARCHAR(60),
  estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVA',
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (nit)
);

CREATE TABLE sucursales (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id),
  nombre VARCHAR(180) NOT NULL,
  direccion VARCHAR(250),
  telefono VARCHAR(40),
  estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVA',
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (empresa_id, nombre)
);
CREATE INDEX idx_sucursales_empresa ON sucursales(empresa_id);

CREATE TABLE usuarios (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID NOT NULL REFERENCES empresas(id),
  nombre VARCHAR(120) NOT NULL,
  apellido VARCHAR(120) NOT NULL,
  email VARCHAR(180) NOT NULL,
  telefono VARCHAR(40),
  password_hash VARCHAR(255) NOT NULL,
  estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
  ultimo_login TIMESTAMPTZ,
  failed_attempts INT NOT NULL DEFAULT 0,
  locked_until TIMESTAMPTZ,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (empresa_id, email)
);
CREATE INDEX idx_usuarios_empresa ON usuarios(empresa_id);
CREATE INDEX idx_usuarios_email ON usuarios(email);

CREATE TABLE usuario_sucursal (
  usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
  sucursal_id UUID NOT NULL REFERENCES sucursales(id) ON DELETE CASCADE,
  PRIMARY KEY (usuario_id, sucursal_id)
);

CREATE TABLE roles (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  empresa_id UUID REFERENCES empresas(id),
  nombre VARCHAR(80) NOT NULL,
  descripcion VARCHAR(250),
  configurable BOOLEAN NOT NULL DEFAULT true,
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_modificacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (empresa_id, nombre)
);

CREATE TABLE permisos (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  modulo VARCHAR(80) NOT NULL,
  accion VARCHAR(40) NOT NULL,
  descripcion VARCHAR(250),
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (modulo, accion)
);

CREATE TABLE usuario_roles (
  usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
  rol_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
  PRIMARY KEY (usuario_id, rol_id)
);

CREATE TABLE rol_permisos (
  rol_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
  permiso_id UUID NOT NULL REFERENCES permisos(id) ON DELETE CASCADE,
  PRIMARY KEY (rol_id, permiso_id)
);

CREATE TABLE refresh_tokens (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
  token_hash VARCHAR(255) NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  revoked_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  created_by_ip VARCHAR(80)
);
CREATE INDEX idx_refresh_tokens_usuario ON refresh_tokens(usuario_id);

CREATE TABLE sesiones (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
  empresa_id UUID NOT NULL REFERENCES empresas(id),
  sucursal_id UUID REFERENCES sucursales(id),
  refresh_token_id UUID REFERENCES refresh_tokens(id) ON DELETE SET NULL,
  ip VARCHAR(80),
  user_agent TEXT,
  activa BOOLEAN NOT NULL DEFAULT true,
  fecha_inicio TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_ultimo_uso TIMESTAMPTZ NOT NULL DEFAULT now(),
  fecha_revocacion TIMESTAMPTZ
);
CREATE INDEX idx_sesiones_usuario ON sesiones(usuario_id);
CREATE INDEX idx_sesiones_empresa ON sesiones(empresa_id);

CREATE TABLE password_reset_tokens (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  usuario_id UUID NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
  token_hash VARCHAR(255) NOT NULL UNIQUE,
  expires_at TIMESTAMPTZ NOT NULL,
  used_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE auditoria (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  usuario_id UUID,
  empresa_id UUID NOT NULL,
  sucursal_id UUID,
  fecha_hora TIMESTAMPTZ NOT NULL DEFAULT now(),
  ip VARCHAR(80),
  user_agent TEXT,
  modulo VARCHAR(80) NOT NULL,
  accion VARCHAR(80) NOT NULL,
  entidad VARCHAR(120),
  registro_id UUID,
  valor_anterior JSONB,
  valor_nuevo JSONB,
  correlation_id UUID NOT NULL
);
CREATE INDEX idx_auditoria_empresa_fecha ON auditoria(empresa_id, fecha_hora DESC);
CREATE INDEX idx_auditoria_entidad_registro ON auditoria(entidad, registro_id);

CREATE OR REPLACE FUNCTION prevent_auditoria_update_delete()
RETURNS trigger AS $$
BEGIN
  RAISE EXCEPTION 'auditoria is append-only';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_auditoria_no_update
BEFORE UPDATE OR DELETE ON auditoria
FOR EACH ROW EXECUTE FUNCTION prevent_auditoria_update_delete();
