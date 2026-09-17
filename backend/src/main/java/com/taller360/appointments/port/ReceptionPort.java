package com.taller360.appointments.port;

import java.util.UUID;

public interface ReceptionPort {
  UUID convertAppointment(UUID appointmentId);
}
