package com.taller360.diagnostics.entity;

import com.taller360.organization.entity.Company;
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
@Table(name = "diagnostico_repuestos")
public class DiagnosticPart {
  @Id public UUID id;
  @ManyToOne @JoinColumn(name = "empresa_id") public Company empresa;
  @ManyToOne @JoinColumn(name = "diagnostico_id") public Diagnostic diagnostico;
  public String codigo;
  public String nombre;
  public BigDecimal cantidad;
  public boolean requerido;
  public String notas;
  @Column(name = "fecha_creacion") public Instant fechaCreacion;
}
