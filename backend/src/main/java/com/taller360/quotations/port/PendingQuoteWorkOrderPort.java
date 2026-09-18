package com.taller360.quotations.port;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PendingQuoteWorkOrderPort implements QuoteWorkOrderPort {
  @Override
  public UUID generateApprovedWork(UUID quotationId, List<UUID> approvedItemIds) {
    return UUID.randomUUID();
  }
}
