package com.taller360.vehicles.repository;

import com.taller360.vehicles.entity.VehiclePhoto;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiclePhotoRepository extends JpaRepository<VehiclePhoto, UUID> {
  List<VehiclePhoto> findByVehiculo_IdAndEmpresa_IdOrderByPrincipalDescFechaCreacionDesc(UUID vehiculoId, UUID empresaId);
}
