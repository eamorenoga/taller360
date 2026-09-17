package com.taller360.iam.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class UserRoleBranchId implements Serializable {
  @Column(name = "usuario_id")
  public UUID usuarioId;
  @Column(name = "rol_id")
  public UUID rolId;
  @Column(name = "sucursal_id")
  public UUID sucursalId;

  public UserRoleBranchId() {}

  public UserRoleBranchId(UUID usuarioId, UUID rolId, UUID sucursalId) {
    this.usuarioId = usuarioId;
    this.rolId = rolId;
    this.sucursalId = sucursalId;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof UserRoleBranchId that)) {
      return false;
    }
    return Objects.equals(usuarioId, that.usuarioId)
        && Objects.equals(rolId, that.rolId)
        && Objects.equals(sucursalId, that.sucursalId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(usuarioId, rolId, sucursalId);
  }
}
