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
@Table(name = "vehiculo_documentos")
public class VehicleDocument {
  @Id
  public UUID id;
  @ManyToOne
  @JoinColumn(name = "empresa_id")
  public Company empresa;
  @ManyToOne
  @JoinColumn(name = "vehiculo_id")
  public Vehicle vehiculo;
  public String tipo;
  @Column(name = "nombre_archivo")
  public String nombreArchivo;
  public String url;
  @Column(name = "vence_en")
  public LocalDate venceEn;
  public String estado;
  public String notas;
  @Column(name = "fecha_creacion")
  public Instant fechaCreacion;
}
