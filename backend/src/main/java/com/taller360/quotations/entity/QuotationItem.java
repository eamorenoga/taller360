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
@Table(name = "cotizacion_items")
public class QuotationItem {
  @Id public UUID id;
  @ManyToOne @JoinColumn(name = "empresa_id") public Company empresa;
  @ManyToOne @JoinColumn(name = "cotizacion_id") public Quotation cotizacion;
  public int version;
  public String tipo;
  public String codigo;
  public String descripcion;
  public BigDecimal cantidad;
  @Column(name = "valor_unitario") public BigDecimal valorUnitario;
  @Column(name = "impuesto_porcentaje") public BigDecimal impuestoPorcentaje;
  @Column(name = "descuento_porcentaje") public BigDecimal descuentoPorcentaje;
  public BigDecimal subtotal;
  public BigDecimal descuento;
  public BigDecimal impuesto;
  public BigDecimal total;
  @Column(name = "estado_aprobacion") public String estadoAprobacion;
  @Column(name = "fecha_creacion") public Instant fechaCreacion;
}
