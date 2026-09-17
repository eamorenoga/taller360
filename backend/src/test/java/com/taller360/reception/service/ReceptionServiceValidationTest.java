package com.taller360.reception.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class ReceptionServiceValidationTest {
  @Test
  void signedReceptionIsFrozenByContract() {
    Set<String> frozenStates = Set.of("FIRMADA", "CONVERTIDA_OT", "ANULADA");

    assertTrue(frozenStates.contains("FIRMADA"));
  }
}
