package com.taller360.common.tenant;

import com.taller360.common.security.AuthPrincipal;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class TenantContextFilter extends OncePerRequestFilter {
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      Object principal = SecurityContextHolder.getContext().getAuthentication() == null
          ? null
          : SecurityContextHolder.getContext().getAuthentication().getPrincipal();
      if (principal instanceof AuthPrincipal auth) {
        UUID branchId = auth.branchId();
        String requestedBranch = request.getHeader("X-Branch-Id");
        if (requestedBranch != null && !requestedBranch.isBlank()) {
          branchId = UUID.fromString(requestedBranch);
        }
        if (branchId != null && !auth.allowedBranchIds().contains(branchId)) {
          throw new AccessDeniedException("Sucursal no autorizada para el usuario");
        }
        TenantContext.set(new TenantContext.CurrentTenant(
            auth.userId(),
            auth.companyId(),
            branchId,
            clientIp(request),
            request.getHeader("User-Agent"),
            correlationId(request)));
      }
      filterChain.doFilter(request, response);
    } finally {
      TenantContext.clear();
    }
  }

  private UUID correlationId(HttpServletRequest request) {
    String value = request.getHeader("X-Correlation-Id");
    if (value == null || value.isBlank()) {
      return UUID.randomUUID();
    }
    return UUID.fromString(value);
  }

  private String clientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    return forwarded == null ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
  }
}
