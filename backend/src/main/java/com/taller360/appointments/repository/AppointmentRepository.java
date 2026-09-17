package com.taller360.appointments.repository;

import com.taller360.appointments.entity.Appointment;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
  Optional<Appointment> findByIdAndEmpresa_Id(UUID id, UUID empresaId);

  @Query("""
      select a from Appointment a
      where a.empresa.id = :empresaId
        and (:sucursalId is null or a.sucursal.id = :sucursalId)
        and a.fechaInicio < :end
        and a.fechaFin > :start
      order by a.fechaInicio asc
      """)
  List<Appointment> calendar(@Param("empresaId") UUID empresaId, @Param("sucursalId") UUID sucursalId,
      @Param("start") Instant start, @Param("end") Instant end);

  @Query("""
      select count(a) from Appointment a
      where a.empresa.id = :empresaId
        and a.sucursal.id = :sucursalId
        and (:excludeId is null or a.id <> :excludeId)
        and a.estado not in ('CANCELADA', 'NO_ASISTIO')
        and a.fechaInicio < :end
        and a.fechaFin > :start
      """)
  long overlappingCount(@Param("empresaId") UUID empresaId, @Param("sucursalId") UUID sucursalId,
      @Param("start") Instant start, @Param("end") Instant end, @Param("excludeId") UUID excludeId);
}
