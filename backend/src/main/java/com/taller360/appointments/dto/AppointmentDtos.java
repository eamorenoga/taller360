package com.taller360.appointments.dto;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class AppointmentDtos {
  public record AppointmentRequest(UUID sucursalId, UUID clienteId, UUID vehiculoId, UUID asesorId, UUID tecnicoId,
      UUID bahiaId, String servicio, Instant fechaInicio, int duracionMinutos, String estado, String notas) {}

  public record AppointmentResponse(UUID id, UUID empresaId, UUID sucursalId, UUID clienteId, String cliente,
      UUID vehiculoId, String placa, UUID asesorId, UUID tecnicoId, UUID bahiaId, String servicio,
      Instant fechaInicio, Instant fechaFin, int duracionMinutos, String estado, String notas,
      boolean sobrecapacidad, UUID recepcionId) {}

  public record AppointmentReminderRequest(String canal, Instant programadoPara, String mensaje) {}

  public record AppointmentReminderResponse(UUID id, String canal, Instant programadoPara, Instant enviadoEn,
      String estado, String mensaje) {}

  public record CalendarResponse(Instant start, Instant end, String view, List<AppointmentResponse> citas) {}

  public record CapacityResponse(UUID sucursalId, Instant start, Instant end, long citasProgramadas,
      int capacidadMaxima, boolean sobrecapacidad) {}

  public record ReceptionConversionResponse(UUID citaId, UUID recepcionId, String estado, String message) {}

  public record TechnicianResponse(UUID id, UUID sucursalId, UUID usuarioId, String nombre, String especialidad,
      boolean activo) {}

  public record ServiceBayResponse(UUID id, UUID sucursalId, String nombre, String tipo, boolean activa) {}

  public record ScheduleCapacityResponse(UUID id, UUID sucursalId, int diaSemana, LocalTime horaInicio,
      LocalTime horaFin, int capacidadMaxima, boolean activo) {}
}
