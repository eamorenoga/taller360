package com.taller360.audit;

import java.util.UUID;

public interface AuditPort {
  void record(String modulo, String accion, String entidad, UUID registroId, String beforeValue, String afterValue);
}
