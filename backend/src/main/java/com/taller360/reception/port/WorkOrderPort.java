package com.taller360.reception.port;

import java.util.UUID;

public interface WorkOrderPort {
  UUID createFromReception(UUID receptionId);
}
