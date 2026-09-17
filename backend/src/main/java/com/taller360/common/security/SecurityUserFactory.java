package com.taller360.common.security;

import com.taller360.iam.entity.User;
import com.taller360.iam.entity.UserRoleBranch;
import com.taller360.iam.repository.UserRoleBranchRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class SecurityUserFactory {
  private final UserRoleBranchRepository scopedRoles;

  public SecurityUserFactory(UserRoleBranchRepository scopedRoles) {
    this.scopedRoles = scopedRoles;
  }

  public AuthPrincipal from(User user, UUID branchId) {
    List<UserRoleBranch> scoped = scopedRoles.findByUsuario_Id(user.id);
    Set<SimpleGrantedAuthority> authorities =
        user.roles.stream()
            .filter(role -> scoped.stream().noneMatch(item -> item.rol.id.equals(role.id))
                || (branchId != null && scoped.stream().anyMatch(item -> item.rol.id.equals(role.id) && item.sucursal.id.equals(branchId))))
            .flatMap(role -> role.permisos.stream())
            .map(permission -> new SimpleGrantedAuthority(permission.code()))
            .collect(Collectors.toSet());
    Set<UUID> branches = user.sucursales.stream().map(branch -> branch.id).collect(Collectors.toSet());
    return new AuthPrincipal(user.id, user.empresa.id, branchId, user.email, user.passwordHash, branches, authorities);
  }
}
