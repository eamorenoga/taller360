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
@Table(name = "diagnostico_evidencias")
public class DiagnosticEvidence {
  @Id public UUID id;
  @ManyToOne @JoinColumn(name = "empresa_id") public Company empresa;
  @ManyToOne @JoinColumn(name = "diagnostico_id") public Diagnostic diagnostico;
  public String tipo;
  public String url;
  @Column(name = "nombre_archivo") public String nombreArchivo;
  @Column(name = "mime_type") public String mimeType;
  @Column(name = "tamano_bytes") public long tamanoBytes;
  @Column(name = "metadatos_json") public String metadatosJson;
  @Column(name = "fecha_creacion") public Instant fechaCreacion;
}
