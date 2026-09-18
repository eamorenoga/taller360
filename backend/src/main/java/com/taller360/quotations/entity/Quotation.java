package com.taller360.quotations.entity;

import com.taller360.clients.entity.Client;
import com.taller360.organization.entity.Branch;
import com.taller360.organization.entity.Company;
import com.taller360.vehicles.entity.Vehicle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cotizaciones")
public class Quotation {
  @Id public UUID id;
  @ManyToOne @JoinColumn(name = "empresa_id") public Company empresa;
  @ManyToOne @JoinColumn(name = "sucursal_id") public Branch sucursal;
  @Column(name = "orden_trabajo_id") public UUID ordenTrabajoId;
  @ManyToOne @JoinColumn(name = "cliente_id") public Client cliente;
  @ManyToOne @JoinColumn(name = "vehiculo_id") public Vehicle vehiculo;
  @Column(name = "version_actual") public int versionActual;
  public String estado;
  @Column(name = "vence_en") public LocalDate venceEn;
  public String moneda;
  public BigDecimal subtotal;
  @Column(name = "descuento_total") public BigDecimal descuentoTotal;
  @Column(name = "impuesto_total") public BigDecimal impuestoTotal;
  public BigDecimal total;
  @Column(name = "public_token") public String publicToken;
  @Column(name = "pdf_url") public String pdfUrl;
  @Column(name = "trabajo_generado_id") public UUID trabajoGeneradoId;
  @Column(name = "fecha_creacion") public Instant fechaCreacion;
  @Column(name = "fecha_modificacion") public Instant fechaModificacion;
}
