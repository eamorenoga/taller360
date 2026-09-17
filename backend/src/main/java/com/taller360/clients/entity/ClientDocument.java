package com.taller360.clients.entity;

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
@Table(name = "cliente_documentos")
public class ClientDocument {
  @Id
  public UUID id;
  @ManyToOne
  @JoinColumn(name = "empresa_id")
  public Company empresa;
  @ManyToOne
  @JoinColumn(name = "cliente_id")
  public Client cliente;
  public String tipo;
  @Column(name = "nombre_archivo")
  public String nombreArchivo;
  public String url;
  public String notas;
  public String estado;
  @Column(name = "fecha_creacion")
  public Instant fechaCreacion;
  @Column(name = "fecha_modificacion")
  public Instant fechaModificacion;
}
