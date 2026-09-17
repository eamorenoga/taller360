package com.taller360.appointments.port;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PendingReceptionPort implements ReceptionPort {
  @Override
  public UUID convertAppointment(UUID appointmentId) {
    return UUID.randomUUID();
  }
}
