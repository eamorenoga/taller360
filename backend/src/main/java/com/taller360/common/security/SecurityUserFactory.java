package com.taller360.common.security;

import com.taller360.iam.entity.User;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class SecurityUserFactory {
  public AuthPrincipal from(User user, UUID branchId) {
    Set<SimpleGrantedAuthority> authorities =
        user.roles.stream()
            .flatMap(role -> role.permisos.stream())
            .map(permission -> new SimpleGrantedAuthority(permission.code()))
            .collect(Collectors.toSet());
    Set<UUID> branches = user.sucursales.stream().map(branch -> branch.id).collect(Collectors.toSet());
    return new AuthPrincipal(user.id, user.empresa.id, branchId, user.email, user.passwordHash, branches, authorities);
  }
}
