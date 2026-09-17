package com.taller360.reception.entity;

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
@Table(name = "recepcion_pdfs")
public class ReceptionPdf {
  @Id public UUID id;
  @ManyToOne @JoinColumn(name = "empresa_id") public Company empresa;
  @ManyToOne @JoinColumn(name = "recepcion_id") public Reception recepcion;
  public int version;
  public String url;
  @Column(name = "contenido_base64") public String contenidoBase64;
  @Column(name = "hash_contenido") public String hashContenido;
  @Column(name = "fecha_creacion") public Instant fechaCreacion;
}
