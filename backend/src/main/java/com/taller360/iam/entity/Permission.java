package com.taller360.iam.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "permisos")
public class Permission {
  @Id
  public UUID id;
  public String modulo;
  public String accion;
  public String descripcion;

  public String code() {
    return modulo + ":" + accion;
  }
}
