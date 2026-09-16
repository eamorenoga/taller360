package com.taller360.auth.entity;

import com.taller360.iam.entity.User;
import com.taller360.organization.entity.Branch;
import com.taller360.organization.entity.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "sesiones")
public class Session {
  @Id
  public UUID id;
  @ManyToOne
  @JoinColumn(name = "usuario_id")
  public User usuario;
  @ManyToOne
  @JoinColumn(name = "empresa_id")
  public Company empresa;
  @ManyToOne
  @JoinColumn(name = "sucursal_id")
  public Branch sucursal;
  @ManyToOne
  @JoinColumn(name = "refresh_token_id")
  public RefreshToken refreshToken;
  public String ip;
  @Column(name = "user_agent")
  public String userAgent;
  public boolean activa;
  @Column(name = "fecha_inicio")
  public Instant fechaInicio;
  @Column(name = "fecha_ultimo_uso")
  public Instant fechaUltimoUso;
  @Column(name = "fecha_revocacion")
  public Instant fechaRevocacion;
}
