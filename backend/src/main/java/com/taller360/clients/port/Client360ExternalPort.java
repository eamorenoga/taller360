package com.taller360.clients.port;

import com.taller360.clients.dto.ClientDtos.PortfolioSummary;
import com.taller360.clients.dto.ClientDtos.RelatedItem;
import java.util.List;
import java.util.UUID;

public interface Client360ExternalPort {
  List<RelatedItem> vehicles(UUID clientId);
  List<RelatedItem> appointments(UUID clientId);
  List<RelatedItem> workOrders(UUID clientId);
  List<RelatedItem> invoices(UUID clientId);
  List<RelatedItem> payments(UUID clientId);
  PortfolioSummary portfolio(UUID clientId);
  List<UUID> findClientIdsByPlate(UUID companyId, String plate);
}
