package com.taller360.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("perm")
public class PermissionEvaluator {
  public boolean has(Authentication authentication, String permission) {
    return authentication != null
        && authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(permission));
  }
}
