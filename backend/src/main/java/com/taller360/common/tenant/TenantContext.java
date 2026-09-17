package com.taller360.common.tenant;

import java.util.UUID;

public final class TenantContext {
  private static final ThreadLocal<CurrentTenant> CURRENT = new ThreadLocal<>();

  private TenantContext() {}

  public static void set(CurrentTenant tenant) {
    CURRENT.set(tenant);
  }

  public static CurrentTenant get() {
    return CURRENT.get();
  }

  public static void clear() {
    CURRENT.remove();
  }

  public record CurrentTenant(UUID userId, UUID companyId, UUID branchId, String ip, String userAgent, UUID correlationId) {}
}
