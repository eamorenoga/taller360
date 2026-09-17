package com.taller360.iam.entity;

import com.taller360.organization.entity.Branch;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "usuario_roles_sucursal")
public class UserRoleBranch {
  @EmbeddedId
  public UserRoleBranchId id;

  @ManyToOne
  @MapsId("usuarioId")
  @JoinColumn(name = "usuario_id")
  public User usuario;

  @ManyToOne
  @MapsId("rolId")
  @JoinColumn(name = "rol_id")
  public Role rol;

  @ManyToOne
  @MapsId("sucursalId")
  @JoinColumn(name = "sucursal_id")
  public Branch sucursal;

  @Column(name = "fecha_creacion")
  public Instant fechaCreacion;
}
