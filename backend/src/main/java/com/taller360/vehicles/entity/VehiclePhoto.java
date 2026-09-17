package com.taller360.vehicles.entity;

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
@Table(name = "vehiculo_fotos")
public class VehiclePhoto {
  @Id
  public UUID id;
  @ManyToOne
  @JoinColumn(name = "empresa_id")
  public Company empresa;
  @ManyToOne
  @JoinColumn(name = "vehiculo_id")
  public Vehicle vehiculo;
  public String url;
  public String descripcion;
  public boolean principal;
  @Column(name = "fecha_creacion")
  public Instant fechaCreacion;
}
