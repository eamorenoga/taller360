package com.taller360.quotations.port;

import java.util.List;
import java.util.UUID;

public interface QuoteWorkOrderPort {
  UUID generateApprovedWork(UUID quotationId, List<UUID> approvedItemIds);
}
