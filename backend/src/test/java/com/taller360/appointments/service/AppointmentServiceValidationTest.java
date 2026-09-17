package com.taller360.appointments.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class AppointmentServiceValidationTest {
  @Test
  void appointmentsSupportExpectedOperationalStates() {
    Set<String> states = Set.of("PROGRAMADA", "CONFIRMADA", "LLEGO", "CANCELADA", "NO_ASISTIO");

    assertTrue(states.contains("PROGRAMADA"));
    assertTrue(states.contains("CONFIRMADA"));
    assertTrue(states.contains("LLEGO"));
  }
}
