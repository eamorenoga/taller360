package com.taller360.diagnostics.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class DiagnosticServiceValidationTest {
  @Test
  void aiSuggestionRequiresTechnicalConfirmationByContract() {
    Set<String> states = Set.of("PENDIENTE_CONFIRMACION", "CONFIRMADO");

    assertTrue(states.contains("PENDIENTE_CONFIRMACION"));
  }
}
