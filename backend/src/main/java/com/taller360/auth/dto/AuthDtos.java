package com.taller360.auth.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class AuthDtos {
  public record LoginRequest(String email, String password, UUID branchId, boolean rememberMe) {}
  public record RefreshRequest(String refreshToken, UUID branchId) {}
  public record LogoutRequest(String refreshToken) {}
  public record ForgotPasswordRequest(String email) {}
  public record ResetPasswordRequest(String token, String newPassword) {}
  public record TokenResponse(String accessToken, String refreshToken, Instant expiresAt, MeResponse user) {}
  public record MeResponse(UUID id, UUID empresaId, String empresa, UUID sucursalId, String nombre, String apellido,
      String email, List<BranchOption> sucursales, List<String> roles, List<String> permisos) {}
  public record BranchOption(UUID id, String nombre) {}
  public record SessionResponse(UUID id, UUID sucursalId, String ip, String userAgent, Instant fechaInicio, Instant fechaUltimoUso) {}
  public record MessageResponse(String message) {}
}
