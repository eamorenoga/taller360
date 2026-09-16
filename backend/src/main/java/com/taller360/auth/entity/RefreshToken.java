package com.taller360.auth.entity;

import com.taller360.iam.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
  @Id
  public UUID id;
  @ManyToOne
  @JoinColumn(name = "usuario_id")
  public User usuario;
  @Column(name = "token_hash")
  public String tokenHash;
  @Column(name = "expires_at")
  public Instant expiresAt;
  @Column(name = "revoked_at")
  public Instant revokedAt;
  @Column(name = "created_at")
  public Instant createdAt;
  @Column(name = "created_by_ip")
  public String createdByIp;

  public boolean active() {
    return revokedAt == null && expiresAt.isAfter(Instant.now());
  }
}
