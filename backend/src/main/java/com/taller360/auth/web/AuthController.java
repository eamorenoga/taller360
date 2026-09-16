package com.taller360.auth.web;

import com.taller360.auth.dto.AuthDtos.*;
import com.taller360.auth.service.AuthService;
import com.taller360.common.security.AuthPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
  private final AuthService auth;

  public AuthController(AuthService auth) {
    this.auth = auth;
  }

  @GetMapping("/login")
  MessageResponse loginInfo() {
    return new MessageResponse("Use POST /api/v1/auth/login con email y password en JSON");
  }

  @PostMapping("/login")
  TokenResponse login(@RequestBody LoginRequest request, HttpServletRequest http) {
    return auth.login(request, http);
  }

  @PostMapping("/refresh")
  TokenResponse refresh(@RequestBody RefreshRequest request, HttpServletRequest http) {
    return auth.refresh(request, http);
  }

  @PostMapping("/logout")
  MessageResponse logout(@RequestBody LogoutRequest request) {
    auth.logout(request);
    return new MessageResponse("Sesion cerrada");
  }

  @PostMapping("/forgot-password")
  MessageResponse forgotPassword(@RequestBody ForgotPasswordRequest request) {
    return new MessageResponse(auth.forgotPassword(request));
  }

  @PostMapping("/reset-password")
  MessageResponse resetPassword(@RequestBody ResetPasswordRequest request) {
    auth.resetPassword(request);
    return new MessageResponse("Contrasena actualizada");
  }

  @GetMapping("/me")
  MeResponse me(@AuthenticationPrincipal AuthPrincipal principal) {
    return auth.me(principal);
  }

  @GetMapping("/sessions")
  List<SessionResponse> sessions(@AuthenticationPrincipal AuthPrincipal principal) {
    return auth.activeSessions(principal.userId());
  }

  @DeleteMapping("/sessions/{id}")
  ResponseEntity<Void> revoke(@PathVariable UUID id, @AuthenticationPrincipal AuthPrincipal principal) {
    auth.revokeSession(id, principal.userId());
    return ResponseEntity.noContent().build();
  }
}
