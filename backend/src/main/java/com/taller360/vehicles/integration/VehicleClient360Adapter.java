package com.taller360.vehicles.integration;

import com.taller360.clients.dto.ClientDtos.PortfolioSummary;
import com.taller360.clients.dto.ClientDtos.RelatedItem;
import com.taller360.clients.port.Client360ExternalPort;
import com.taller360.common.tenant.TenantGuard;
import com.taller360.vehicles.repository.VehicleRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class VehicleClient360Adapter implements Client360ExternalPort {
  private final VehicleRepository vehicles;
  private final TenantGuard tenant;

  public VehicleClient360Adapter(VehicleRepository vehicles, TenantGuard tenant) {
    this.vehicles = vehicles;
    this.tenant = tenant;
  }

  @Override
  public List<RelatedItem> vehicles(UUID clientId) {
    return vehicles.findByCliente_IdAndEmpresa_IdOrderByFechaModificacionDesc(clientId, tenant.companyId()).stream()
        .map(vehicle -> new RelatedItem(vehicle.id, vehicle.placa + " · " + vehicle.marca + " " + vehicle.modelo,
            vehicle.estado, vehicle.vin, vehicle.fechaModificacion))
        .toList();
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
    return vehicles.findClientIdsByPlate(companyId, plate);
  }
}
