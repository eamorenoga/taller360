package com.taller360.diagnostics.port;

import java.util.Optional;
import java.util.UUID;

public interface WorkOrderSnapshotPort {
  Optional<WorkOrderSnapshot> find(UUID workOrderId, UUID companyId);

  record WorkOrderSnapshot(UUID id, UUID companyId, UUID branchId, UUID clientId, UUID vehicleId) {}
}
