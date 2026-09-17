package com.taller360.diagnostics.entity;

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
@Table(name = "diagnostico_tareas")
public class DiagnosticTask {
  @Id public UUID id;
  @ManyToOne @JoinColumn(name = "empresa_id") public Company empresa;
  @ManyToOne @JoinColumn(name = "diagnostico_id") public Diagnostic diagnostico;
  public String descripcion;
  public String prioridad;
  public String estado;
  @Column(name = "tiempo_estimado_minutos") public int tiempoEstimadoMinutos;
  @Column(name = "fecha_creacion") public Instant fechaCreacion;
}
