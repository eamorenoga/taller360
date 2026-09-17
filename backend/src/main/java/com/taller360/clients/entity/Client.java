package com.taller360.clients.entity;

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
@Table(name = "clientes")
public class Client {
  @Id
  public UUID id;
  @ManyToOne
  @JoinColumn(name = "empresa_id")
  public Company empresa;
  @ManyToOne
  @JoinColumn(name = "sucursal_id")
  public Branch sucursal;
  @Column(name = "tipo_persona")
  public String tipoPersona;
  @Column(name = "tipo_identificacion")
  public String tipoIdentificacion;
  public String identificacion;
  public String nombre;
  @Column(name = "razon_social")
  public String razonSocial;
  @Column(name = "telefono_principal")
  public String telefonoPrincipal;
  @Column(name = "telefono_secundario")
  public String telefonoSecundario;
  public String whatsapp;
  public String email;
  public String direccion;
  public String notas;
  public String preferencias;
  public String estado;
  @Column(name = "fecha_creacion")
  public Instant fechaCreacion;
  @Column(name = "fecha_modificacion")
  public Instant fechaModificacion;
}
