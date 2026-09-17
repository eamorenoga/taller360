package com.taller360.appointments.repository;

import com.taller360.appointments.entity.ScheduleCapacity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleCapacityRepository extends JpaRepository<ScheduleCapacity, UUID> {
  List<ScheduleCapacity> findByEmpresa_IdAndSucursal_IdAndDiaSemanaAndActivoTrue(UUID empresaId, UUID sucursalId, int diaSemana);
  List<ScheduleCapacity> findByEmpresa_IdAndSucursal_IdOrderByDiaSemanaAscHoraInicioAsc(UUID empresaId, UUID sucursalId);
}
