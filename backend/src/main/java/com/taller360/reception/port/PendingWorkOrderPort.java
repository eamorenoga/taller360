package com.taller360.reception.port;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PendingWorkOrderPort implements WorkOrderPort {
  @Override
  public UUID createFromReception(UUID receptionId) {
    return UUID.randomUUID();
  }
}
