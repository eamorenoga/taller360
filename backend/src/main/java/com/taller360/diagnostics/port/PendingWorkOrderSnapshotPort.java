package com.taller360.diagnostics.port;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PendingWorkOrderSnapshotPort implements WorkOrderSnapshotPort {
  @Override
  public Optional<WorkOrderSnapshot> find(UUID workOrderId, UUID companyId) {
    return Optional.empty();
  }
}
