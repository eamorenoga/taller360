package com.taller360.common.security;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public record AuthPrincipal(
    UUID userId,
    UUID companyId,
    UUID branchId,
    String email,
    String password,
    Set<UUID> allowedBranchIds,
    Collection<? extends GrantedAuthority> authorities)
    implements UserDetails {
  @Override
  public String getUsername() {
    return email;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
