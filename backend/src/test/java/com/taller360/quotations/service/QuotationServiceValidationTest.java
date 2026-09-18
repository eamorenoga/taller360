package com.taller360.quotations.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.junit.jupiter.api.Test;

class QuotationServiceValidationTest {
  @Test
  void itemTotalsUseDiscountBeforeTaxByContract() {
    BigDecimal subtotal = new BigDecimal("2").multiply(new BigDecimal("100"));
    BigDecimal discount = subtotal.multiply(new BigDecimal("10")).divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
    BigDecimal tax = subtotal.subtract(discount).multiply(new BigDecimal("19")).divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);

    assertEquals(new BigDecimal("214.00"), subtotal.subtract(discount).add(tax).setScale(2, RoundingMode.HALF_UP));
  }
}
