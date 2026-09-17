package com.taller360.common.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class PermissionEvaluatorTest {
  @Test
  void validatesDynamicPermissionAuthority() {
    PermissionEvaluator evaluator = new PermissionEvaluator();
    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
        "user",
        null,
        List.of(new SimpleGrantedAuthority("USUARIOS:VER")));

    assertTrue(evaluator.has(authentication, "USUARIOS:VER"));
    assertFalse(evaluator.has(authentication, "USUARIOS:ELIMINAR"));
  }
}
