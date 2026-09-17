package com.taller360.vehicles.entity;

import com.taller360.clients.entity.Client;
import com.taller360.organization.entity.Branch;
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
@Table(name = "vehiculos")
public class Vehicle {
  @Id
  public UUID id;
  @ManyToOne
  @JoinColumn(name = "empresa_id")
  public Company empresa;
  @ManyToOne
  @JoinColumn(name = "sucursal_id")
  public Branch sucursal;
  @ManyToOne
  @JoinColumn(name = "cliente_id")
  public Client cliente;
  public String placa;
  public String vin;
  public String marca;
  public String modelo;
  public String version;
  public Integer anio;
  public String motor;
  public String combustible;
  public String transmision;
  public String color;
  public long kilometraje;
  public String estado;
  public String notas;
  @Column(name = "fecha_creacion")
  public Instant fechaCreacion;
  @Column(name = "fecha_modificacion")
  public Instant fechaModificacion;
}
