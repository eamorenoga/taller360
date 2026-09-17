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
@Table(name = "vehiculo_timeline")
public class VehicleTimelineEvent {
  @Id
  public UUID id;
  @ManyToOne
  @JoinColumn(name = "empresa_id")
  public Company empresa;
  @ManyToOne
  @JoinColumn(name = "vehiculo_id")
  public Vehicle vehiculo;
  public String modulo;
  public String titulo;
  public String descripcion;
  @Column(name = "referencia_id")
  public UUID referenciaId;
  public String estado;
  @Column(name = "fecha_hora")
  public Instant fechaHora;
}
