package com.taller360.auth.service;

import com.taller360.auth.dto.AuthDtos.*;
import com.taller360.auth.entity.PasswordResetToken;
import com.taller360.auth.entity.RefreshToken;
import com.taller360.auth.entity.Session;
import com.taller360.auth.repository.PasswordResetTokenRepository;
import com.taller360.auth.repository.RefreshTokenRepository;
import com.taller360.auth.repository.SessionRepository;
import com.taller360.common.security.AuthPrincipal;
import com.taller360.common.security.JwtService;
import com.taller360.common.security.SecurityUserFactory;
import com.taller360.iam.entity.User;
import com.taller360.iam.repository.UserRepository;
import com.taller360.organization.entity.Branch;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final UserRepository users;
  private final RefreshTokenRepository refreshTokens;
  private final SessionRepository sessions;
  private final PasswordResetTokenRepository resetTokens;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final SecurityUserFactory securityUserFactory;
  private final TokenHashing tokenHashing;
  private final long refreshDays;
  private final int maxFailedAttempts;
  private final int lockMinutes;

  public AuthService(
      UserRepository users,
      RefreshTokenRepository refreshTokens,
      SessionRepository sessions,
      PasswordResetTokenRepository resetTokens,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      SecurityUserFactory securityUserFactory,
      TokenHashing tokenHashing,
      @Value("${app.jwt.refresh-token-days}") long refreshDays,
      @Value("${app.security.max-failed-attempts}") int maxFailedAttempts,
      @Value("${app.security.lock-minutes}") int lockMinutes) {
    this.users = users;
    this.refreshTokens = refreshTokens;
    this.sessions = sessions;
    this.resetTokens = resetTokens;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.securityUserFactory = securityUserFactory;
    this.tokenHashing = tokenHashing;
    this.refreshDays = refreshDays;
    this.maxFailedAttempts = maxFailedAttempts;
    this.lockMinutes = lockMinutes;
  }

  @Transactional
  public TokenResponse login(LoginRequest request, HttpServletRequest http) {
    User user = users.findFirstByEmailIgnoreCase(request.email()).orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));
    if (!user.isActive()) {
      throw new AccessDeniedException("Usuario inactivo");
    }
    if (user.isLocked()) {
      throw new AccessDeniedException("Cuenta bloqueada temporalmente");
    }
    if (!passwordEncoder.matches(request.password(), user.passwordHash)) {
      user.failedAttempts++;
      if (user.failedAttempts >= maxFailedAttempts) {
        user.lockedUntil = Instant.now().plus(lockMinutes, ChronoUnit.MINUTES);
      }
      users.save(user);
      throw new BadCredentialsException("Credenciales incorrectas");
    }
    user.failedAttempts = 0;
    user.lockedUntil = null;
    user.ultimoLogin = Instant.now();
    users.save(user);
    UUID branchId = chooseBranch(user, request.branchId());
    return issueTokens(user, branchId, http);
  }

  @Transactional
  public TokenResponse refresh(RefreshRequest request, HttpServletRequest http) {
    RefreshToken token = refreshTokens.findByTokenHash(tokenHashing.sha256(request.refreshToken()))
        .filter(RefreshToken::active)
        .orElseThrow(() -> new AccessDeniedException("Refresh token invalido o revocado"));
    UUID branchId = chooseBranch(token.usuario, request.branchId());
    return issueTokens(token.usuario, branchId, http);
  }

  @Transactional
  public void logout(LogoutRequest request) {
    refreshTokens.findByTokenHash(tokenHashing.sha256(request.refreshToken())).ifPresent(token -> {
      token.revokedAt = Instant.now();
      refreshTokens.save(token);
    });
  }

  @Transactional
  public String forgotPassword(ForgotPasswordRequest request) {
    return users.findFirstByEmailIgnoreCase(request.email()).map(user -> {
      String raw = UUID.randomUUID() + "." + UUID.randomUUID();
      PasswordResetToken token = new PasswordResetToken();
      token.id = UUID.randomUUID();
      token.usuario = user;
      token.tokenHash = tokenHashing.sha256(raw);
      token.expiresAt = Instant.now().plus(30, ChronoUnit.MINUTES);
      token.createdAt = Instant.now();
      resetTokens.save(token);
      return raw;
    }).orElse("Si el correo existe, recibira instrucciones para recuperar la cuenta");
  }

  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    PasswordResetToken token = resetTokens.findByTokenHash(tokenHashing.sha256(request.token()))
        .filter(t -> t.usedAt == null && t.expiresAt.isAfter(Instant.now()))
        .orElseThrow(() -> new AccessDeniedException("Token de recuperacion invalido"));
    token.usuario.passwordHash = passwordEncoder.encode(request.newPassword());
    token.usedAt = Instant.now();
    users.save(token.usuario);
    resetTokens.save(token);
  }

  public MeResponse me(AuthPrincipal principal) {
    User user = users.findByIdAndEmpresaId(principal.userId(), principal.companyId()).orElseThrow();
    return toMe(user, principal.branchId());
  }

  public List<SessionResponse> activeSessions(UUID userId) {
    return sessions.findByUsuarioIdAndActivaTrue(userId).stream()
        .map(s -> new SessionResponse(s.id, s.sucursal == null ? null : s.sucursal.id, s.ip, s.userAgent, s.fechaInicio, s.fechaUltimoUso))
        .toList();
  }

  @Transactional
  public void revokeSession(UUID sessionId, UUID userId) {
    Session session = sessions.findByIdAndUsuarioId(sessionId, userId).orElseThrow(() -> new AccessDeniedException("Sesion no encontrada"));
    session.activa = false;
    session.fechaRevocacion = Instant.now();
    if (session.refreshToken != null) {
      session.refreshToken.revokedAt = Instant.now();
    }
    sessions.save(session);
  }

  private TokenResponse issueTokens(User user, UUID branchId, HttpServletRequest http) {
    AuthPrincipal principal = securityUserFactory.from(user, branchId);
    String refreshRaw = UUID.randomUUID() + "." + UUID.randomUUID();
    RefreshToken refresh = new RefreshToken();
    refresh.id = UUID.randomUUID();
    refresh.usuario = user;
    refresh.tokenHash = tokenHashing.sha256(refreshRaw);
    refresh.expiresAt = Instant.now().plus(refreshDays, ChronoUnit.DAYS);
    refresh.createdAt = Instant.now();
    refresh.createdByIp = clientIp(http);
    refreshTokens.save(refresh);

    Session session = new Session();
    session.id = UUID.randomUUID();
    session.usuario = user;
    session.empresa = user.empresa;
    session.sucursal = user.sucursales.stream().filter(b -> b.id.equals(branchId)).findFirst().orElse(null);
    session.refreshToken = refresh;
    session.ip = clientIp(http);
    session.userAgent = http.getHeader("User-Agent");
    session.activa = true;
    session.fechaInicio = Instant.now();
    session.fechaUltimoUso = Instant.now();
    sessions.save(session);
    return new TokenResponse(jwtService.createAccessToken(principal), refreshRaw, refresh.expiresAt, toMe(user, branchId));
  }

  private UUID chooseBranch(User user, UUID requestedBranchId) {
    if (requestedBranchId == null) {
      return user.sucursales.size() == 1 ? user.sucursales.iterator().next().id : null;
    }
    boolean allowed = user.sucursales.stream().anyMatch(branch -> branch.id.equals(requestedBranchId) && branch.empresa.id.equals(user.empresa.id));
    if (!allowed) {
      throw new AccessDeniedException("Sucursal no autorizada");
    }
    return requestedBranchId;
  }

  private MeResponse toMe(User user, UUID branchId) {
    return new MeResponse(
        user.id,
        user.empresa.id,
        user.empresa.nombre,
        branchId,
        user.nombre,
        user.apellido,
        user.email,
        user.sucursales.stream().map(b -> new BranchOption(b.id, b.nombre)).toList(),
        user.roles.stream().map(r -> r.nombre).toList(),
        user.roles.stream().flatMap(r -> r.permisos.stream()).map(p -> p.code()).distinct().sorted().toList());
  }

  private String clientIp(HttpServletRequest http) {
    String forwarded = http.getHeader("X-Forwarded-For");
    return forwarded == null ? http.getRemoteAddr() : forwarded.split(",")[0].trim();
  }
}
