package com.taller360.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consecutivos")
public class Consecutive {
  @Id
  public UUID id;
  @ManyToOne
  @JoinColumn(name = "empresa_id")
  public Company empresa;
  @ManyToOne
  @JoinColumn(name = "sucursal_id")
  public Branch sucursal;
  public String documento;
  public String prefijo;
  @Column(name = "siguiente_numero")
  public long siguienteNumero;
  public int longitud;
  public boolean activo;
  @Column(name = "fecha_creacion")
  public Instant fechaCreacion;
  @Column(name = "fecha_modificacion")
  public Instant fechaModificacion;
}
