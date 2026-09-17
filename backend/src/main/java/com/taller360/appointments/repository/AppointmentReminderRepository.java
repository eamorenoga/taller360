package com.taller360.appointments.repository;

import com.taller360.appointments.entity.AppointmentReminder;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppointmentReminderRepository extends JpaRepository<AppointmentReminder, UUID> {
  List<AppointmentReminder> findByCita_IdAndEmpresa_IdOrderByProgramadoParaAsc(UUID citaId, UUID empresaId);
}
