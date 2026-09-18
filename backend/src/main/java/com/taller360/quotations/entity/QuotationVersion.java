package com.taller360.quotations.entity;

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
@Table(name = "cotizacion_versiones")
public class QuotationVersion {
  @Id public UUID id;
  @ManyToOne @JoinColumn(name = "empresa_id") public Company empresa;
  @ManyToOne @JoinColumn(name = "cotizacion_id") public Quotation cotizacion;
  public int version;
  @Column(name = "snapshot_json") public String snapshotJson;
  public BigDecimal subtotal;
  @Column(name = "descuento_total") public BigDecimal descuentoTotal;
  @Column(name = "impuesto_total") public BigDecimal impuestoTotal;
  public BigDecimal total;
  @Column(name = "fecha_creacion") public Instant fechaCreacion;
}
