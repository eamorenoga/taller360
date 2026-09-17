package com.taller360.vehicles.entity;

import com.taller360.organization.entity.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vehiculo_garantias")
public class VehicleWarranty {
  @Id
  public UUID id;
  @ManyToOne
  @JoinColumn(name = "empresa_id")
  public Company empresa;
  @ManyToOne
  @JoinColumn(name = "vehiculo_id")
  public Vehicle vehiculo;
  public String tipo;
  public String descripcion;
  public String proveedor;
  @Column(name = "inicia_en")
  public LocalDate iniciaEn;
  @Column(name = "vence_en")
  public LocalDate venceEn;
  @Column(name = "kilometraje_limite")
  public Long kilometrajeLimite;
  public String estado;
  @Column(name = "fecha_creacion")
  public Instant fechaCreacion;
}
