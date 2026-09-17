package com.taller360.vehicles.repository;

import com.taller360.vehicles.entity.VehicleDocument;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleDocumentRepository extends JpaRepository<VehicleDocument, UUID> {
  List<VehicleDocument> findByVehiculo_IdAndEmpresa_IdOrderByFechaCreacionDesc(UUID vehiculoId, UUID empresaId);
}
