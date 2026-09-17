package com.taller360.appointments.repository;

import com.taller360.appointments.entity.ServiceBay;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceBayRepository extends JpaRepository<ServiceBay, UUID> {
  List<ServiceBay> findByEmpresa_IdOrderByNombreAsc(UUID empresaId);
  List<ServiceBay> findByEmpresa_IdAndSucursal_IdAndActivaTrueOrderByNombreAsc(UUID empresaId, UUID sucursalId);
  Optional<ServiceBay> findByIdAndEmpresa_Id(UUID id, UUID empresaId);
}
