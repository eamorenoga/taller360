package com.taller360.organization.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "empresa_impuestos")
public class CompanyTax {
  @Id
  public UUID id;
  @ManyToOne
  @JoinColumn(name = "empresa_id")
  public Company empresa;
  public String nombre;
  public String codigo;
  public BigDecimal porcentaje;
  public boolean incluido;
  public boolean activo;
  @Column(name = "fecha_creacion")
  public Instant fechaCreacion;
  @Column(name = "fecha_modificacion")
  public Instant fechaModificacion;
}
