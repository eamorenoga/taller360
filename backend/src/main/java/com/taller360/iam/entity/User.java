package com.taller360.iam.entity;

import com.taller360.organization.entity.Branch;
import com.taller360.organization.entity.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class User {
  @Id
  public UUID id;
  @ManyToOne
  @JoinColumn(name = "empresa_id")
  public Company empresa;
  public String nombre;
  public String apellido;
  public String email;
  public String telefono;
  @Column(name = "password_hash")
  public String passwordHash;
  public String estado;
  @Column(name = "ultimo_login")
  public Instant ultimoLogin;
  @Column(name = "failed_attempts")
  public int failedAttempts;
  @Column(name = "locked_until")
  public Instant lockedUntil;
  @Column(name = "fecha_creacion")
  public Instant fechaCreacion;
  @Column(name = "fecha_modificacion")
  public Instant fechaModificacion;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "usuario_sucursal",
      joinColumns = @JoinColumn(name = "usuario_id"),
      inverseJoinColumns = @JoinColumn(name = "sucursal_id"))
  public Set<Branch> sucursales = new HashSet<>();

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "usuario_roles",
      joinColumns = @JoinColumn(name = "usuario_id"),
      inverseJoinColumns = @JoinColumn(name = "rol_id"))
  public Set<Role> roles = new HashSet<>();

  public boolean isActive() {
    return "ACTIVO".equalsIgnoreCase(estado);
  }

  public boolean isLocked() {
    return lockedUntil != null && lockedUntil.isAfter(Instant.now());
  }
}
