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
@Table(name = "recepcion_fotos")
public class ReceptionPhoto {
  @Id public UUID id;
  @ManyToOne @JoinColumn(name = "empresa_id") public Company empresa;
  @ManyToOne @JoinColumn(name = "recepcion_id") public Reception recepcion;
  public String url;
  @Column(name = "nombre_archivo") public String nombreArchivo;
  public String tipo;
  @Column(name = "tamano_original_bytes") public long tamanoOriginalBytes;
  @Column(name = "tamano_comprimido_bytes") public long tamanoComprimidoBytes;
  public Integer ancho;
  public Integer alto;
  @Column(name = "metadatos_json") public String metadatosJson;
  @Column(name = "fecha_creacion") public Instant fechaCreacion;
}
