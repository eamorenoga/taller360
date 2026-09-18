package com.taller360.quotations.entity;

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
@Table(name = "cotizacion_aprobaciones")
public class QuotationApproval {
  @Id public UUID id;
  @ManyToOne @JoinColumn(name = "empresa_id") public Company empresa;
  @ManyToOne @JoinColumn(name = "cotizacion_id") public Quotation cotizacion;
  @ManyToOne @JoinColumn(name = "item_id") public QuotationItem item;
  public String accion;
  @Column(name = "aprobador_nombre") public String aprobadorNombre;
  @Column(name = "aprobador_email") public String aprobadorEmail;
  @Column(name = "evidencia_json") public String evidenciaJson;
  @Column(name = "fecha_creacion") public Instant fechaCreacion;
}
