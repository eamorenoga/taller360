package com.taller360.vehicles.repository;

import com.taller360.vehicles.entity.VehicleWarranty;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleWarrantyRepository extends JpaRepository<VehicleWarranty, UUID> {
  List<VehicleWarranty> findByVehiculo_IdAndEmpresa_IdOrderByFechaCreacionDesc(UUID vehiculoId, UUID empresaId);
}
