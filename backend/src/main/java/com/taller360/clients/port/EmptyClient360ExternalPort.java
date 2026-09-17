package com.taller360.clients.port;

import com.taller360.clients.dto.ClientDtos.PortfolioSummary;
import com.taller360.clients.dto.ClientDtos.RelatedItem;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class EmptyClient360ExternalPort implements Client360ExternalPort {
  @Override
  public List<RelatedItem> vehicles(UUID clientId) {
    return List.of();
  }

  @Override
  public List<RelatedItem> appointments(UUID clientId) {
    return List.of();
  }

  @Override
  public List<RelatedItem> workOrders(UUID clientId) {
    return List.of();
  }

  @Override
  public List<RelatedItem> invoices(UUID clientId) {
    return List.of();
  }

  @Override
  public List<RelatedItem> payments(UUID clientId) {
    return List.of();
  }

  @Override
  public PortfolioSummary portfolio(UUID clientId) {
    return new PortfolioSummary("SIN_MOVIMIENTOS", 0, "COP", List.of());
  }

  @Override
  public List<UUID> findClientIdsByPlate(UUID companyId, String plate) {
    return List.of();
  }
}
