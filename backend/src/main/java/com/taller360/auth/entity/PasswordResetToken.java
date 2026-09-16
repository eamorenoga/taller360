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
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
  @Id
  public UUID id;
  @ManyToOne
  @JoinColumn(name = "usuario_id")
  public User usuario;
  @Column(name = "token_hash")
  public String tokenHash;
  @Column(name = "expires_at")
  public Instant expiresAt;
  @Column(name = "used_at")
  public Instant usedAt;
  @Column(name = "created_at")
  public Instant createdAt;
}
