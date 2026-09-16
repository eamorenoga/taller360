package com.taller360.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final SecretKey key;
  private final long accessMinutes;

  public JwtService(@Value("${app.jwt.secret}") String secret, @Value("${app.jwt.access-token-minutes}") long accessMinutes) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    this.accessMinutes = accessMinutes;
  }

  public String createAccessToken(AuthPrincipal principal) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(principal.userId().toString())
        .claim("email", principal.email())
        .claim("empresa_id", principal.companyId().toString())
        .claim("sucursal_id", principal.branchId() == null ? null : principal.branchId().toString())
        .claim("permisos", principal.authorities().stream().map(Object::toString).toList())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(accessMinutes * 60)))
        .signWith(key)
        .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
  }

  public UUID userId(Claims claims) {
    return UUID.fromString(claims.getSubject());
  }

  public UUID companyId(Claims claims) {
    return UUID.fromString(claims.get("empresa_id", String.class));
  }

  public UUID branchId(Claims claims) {
    String value = claims.get("sucursal_id", String.class);
    return value == null ? null : UUID.fromString(value);
  }

  @SuppressWarnings("unchecked")
  public List<String> permissions(Claims claims) {
    return (List<String>) claims.get("permisos", List.class);
  }
}
