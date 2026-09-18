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
@Table(name = "cotizacion_pdfs")
public class QuotationPdf {
  @Id public UUID id;
  @ManyToOne @JoinColumn(name = "empresa_id") public Company empresa;
  @ManyToOne @JoinColumn(name = "cotizacion_id") public Quotation cotizacion;
  public int version;
  public String url;
  @Column(name = "contenido_base64") public String contenidoBase64;
  @Column(name = "hash_contenido") public String hashContenido;
  @Column(name = "fecha_creacion") public Instant fechaCreacion;
}
