package com.taller360.common.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class TenantContextTest {
  @Test
  void contextIsScopedAndClearable() {
    UUID userId = UUID.randomUUID();
    UUID companyId = UUID.randomUUID();
    UUID branchId = UUID.randomUUID();
    UUID correlationId = UUID.randomUUID();

    TenantContext.set(new TenantContext.CurrentTenant(userId, companyId, branchId, "127.0.0.1", "test", correlationId));

    assertEquals(userId, TenantContext.get().userId());
    assertEquals(companyId, TenantContext.get().companyId());
    assertEquals(branchId, TenantContext.get().branchId());
    assertEquals(correlationId, TenantContext.get().correlationId());

    TenantContext.clear();

    assertNull(TenantContext.get());
  }
}
