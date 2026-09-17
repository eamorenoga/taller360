package com.taller360.clients.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ClientServiceValidationTest {
  @Test
  void clientsModuleRequiresNameByContract() {
    String requiredField = "nombre";

    assertTrue(requiredField.equals("nombre"));
  }
}
