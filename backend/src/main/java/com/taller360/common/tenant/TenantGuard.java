package com.taller360.common.tenant;

import com.taller360.common.security.AuthPrincipal;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class TenantGuard {
  public AuthPrincipal current() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if (principal instanceof AuthPrincipal authPrincipal) {
      return authPrincipal;
    }
    throw new AccessDeniedException("Usuario no autenticado");
  }

  public UUID companyId() {
    return current().companyId();
  }

  public UUID userId() {
    return current().userId();
  }

  public void assertBranchAllowed(UUID branchId) {
    if (branchId != null && !current().allowedBranchIds().contains(branchId)) {
      throw new AccessDeniedException("Sucursal no autorizada para el usuario");
    }
  }
}
