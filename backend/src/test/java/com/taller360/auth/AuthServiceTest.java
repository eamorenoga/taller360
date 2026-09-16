package com.taller360.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.taller360.common.security.AuthPrincipal;
import com.taller360.common.tenant.TenantGuard;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class AuthServiceTest {
  @Test
  void usuarioSinPermisosNoTieneAutoridad() {
    AuthPrincipal principal = principal(Set.of(), Set.of());
    assertEquals(0, principal.authorities().size());
  }

  @Test
  void usuarioIntentandoAccederSucursalNoAutorizadaEsBloqueado() {
    UUID allowedBranch = UUID.randomUUID();
    UUID forbiddenBranch = UUID.randomUUID();
    AuthPrincipal principal = principal(Set.of(allowedBranch), Set.of("CLIENTES:VER"));
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(principal, null, principal.authorities()));

    TenantGuard guard = new TenantGuard();
    assertThrows(AccessDeniedException.class, () -> guard.assertBranchAllowed(forbiddenBranch));
  }

  @Test
  void usuarioSoloPuedeOperarSuEmpresaActual() {
    UUID companyId = UUID.randomUUID();
    AuthPrincipal principal = new AuthPrincipal(
        UUID.randomUUID(),
        companyId,
        null,
        "admin@taller360.local",
        "",
        Set.of(),
        List.of(new SimpleGrantedAuthority("USUARIOS:VER")));
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken(principal, null, principal.authorities()));

    TenantGuard guard = new TenantGuard();
    assertEquals(companyId, guard.companyId());
  }

  private AuthPrincipal principal(Set<UUID> branches, Set<String> permissions) {
    return new AuthPrincipal(
        UUID.randomUUID(),
        UUID.randomUUID(),
        branches.stream().findFirst().orElse(null),
        "user@taller360.local",
        "",
        branches,
        permissions.stream().map(SimpleGrantedAuthority::new).toList());
  }
}
