package com.taller360.vehicles.repository;

import com.taller360.vehicles.entity.VehicleTimelineEvent;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleTimelineRepository extends JpaRepository<VehicleTimelineEvent, UUID> {
  List<VehicleTimelineEvent> findByVehiculo_IdAndEmpresa_IdOrderByFechaHoraDesc(UUID vehiculoId, UUID empresaId);
}
