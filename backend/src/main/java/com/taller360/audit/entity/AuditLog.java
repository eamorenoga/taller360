package com.taller360.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "auditoria")
public class AuditLog {
  @Id
  public UUID id;
  @Column(name = "usuario_id")
  public UUID usuarioId;
  @Column(name = "empresa_id")
  public UUID empresaId;
  @Column(name = "sucursal_id")
  public UUID sucursalId;
  @Column(name = "fecha_hora")
  public Instant fechaHora;
  public String ip;
  @Column(name = "user_agent")
  public String userAgent;
  public String modulo;
  public String accion;
  public String entidad;
  @Column(name = "registro_id")
  public UUID registroId;
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "valor_anterior")
  public String valorAnterior;
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "valor_nuevo")
  public String valorNuevo;
  @Column(name = "correlation_id")
  public UUID correlationId;
}
