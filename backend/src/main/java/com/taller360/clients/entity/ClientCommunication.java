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
@Table(name = "cliente_comunicaciones")
public class ClientCommunication {
  @Id
  public UUID id;
  @ManyToOne
  @JoinColumn(name = "empresa_id")
  public Company empresa;
  @ManyToOne
  @JoinColumn(name = "cliente_id")
  public Client cliente;
  public String canal;
  public String direccion;
  public String asunto;
  public String contenido;
  public String estado;
  @Column(name = "fecha_hora")
  public Instant fechaHora;
}
