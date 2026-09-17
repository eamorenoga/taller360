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
@Table(name = "recepcion_firmas")
public class ReceptionSignature {
  @Id public UUID id;
  @ManyToOne @JoinColumn(name = "empresa_id") public Company empresa;
  @ManyToOne @JoinColumn(name = "recepcion_id") public Reception recepcion;
  public String firmante;
  @Column(name = "firma_url") public String firmaUrl;
  @Column(name = "hash_contenido") public String hashContenido;
  public int version;
  @Column(name = "fecha_creacion") public Instant fechaCreacion;
}
