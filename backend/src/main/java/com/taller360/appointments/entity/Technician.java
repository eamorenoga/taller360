package com.taller360.appointments.entity;

import com.taller360.iam.entity.User;
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
@Table(name = "tecnicos")
public class Technician {
  @Id public UUID id;
  @ManyToOne @JoinColumn(name = "empresa_id") public Company empresa;
  @ManyToOne @JoinColumn(name = "sucursal_id") public Branch sucursal;
  @ManyToOne @JoinColumn(name = "usuario_id") public User usuario;
  public String nombre;
  public String especialidad;
  public boolean activo;
  @Column(name = "fecha_creacion") public Instant fechaCreacion;
  @Column(name = "fecha_modificacion") public Instant fechaModificacion;
}
