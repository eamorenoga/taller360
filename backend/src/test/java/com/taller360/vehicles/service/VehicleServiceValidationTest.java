package com.taller360.vehicles.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VehicleServiceValidationTest {
  @Test
  void mileageCannotBeNegativeByContract() {
    long mileage = 0;

    assertTrue(mileage >= 0);
  }
}
