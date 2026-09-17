package com.taller360.audit;

import com.taller360.audit.entity.AuditLog;
import com.taller360.audit.repository.AuditLogRepository;
import com.taller360.common.tenant.TenantContext;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class JpaAuditService implements AuditPort {
  private final AuditLogRepository auditLogs;

  public JpaAuditService(AuditLogRepository auditLogs) {
    this.auditLogs = auditLogs;
  }

  @Override
  public void record(String modulo, String accion, String entidad, UUID registroId, String beforeValue, String afterValue) {
    TenantContext.CurrentTenant current = TenantContext.get();
    AuditLog log = new AuditLog();
    log.id = UUID.randomUUID();
    log.usuarioId = current == null ? null : current.userId();
    log.empresaId = current == null ? new UUID(0, 0) : current.companyId();
    log.sucursalId = current == null ? null : current.branchId();
    log.fechaHora = Instant.now();
    log.ip = current == null ? null : current.ip();
    log.userAgent = current == null ? null : current.userAgent();
    log.modulo = modulo;
    log.accion = accion;
    log.entidad = entidad;
    log.registroId = registroId;
    log.valorAnterior = beforeValue;
    log.valorNuevo = afterValue;
    log.correlationId = current == null ? UUID.randomUUID() : current.correlationId();
    auditLogs.save(log);
  }
}
