package com.taller360.appointments.repository;

import com.taller360.appointments.entity.Technician;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnicianRepository extends JpaRepository<Technician, UUID> {
  List<Technician> findByEmpresa_IdOrderByNombreAsc(UUID empresaId);
  Optional<Technician> findByIdAndEmpresa_Id(UUID id, UUID empresaId);
}
