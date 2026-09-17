package com.taller360.appointments.entity;

import com.taller360.clients.entity.Client;
import com.taller360.iam.entity.User;
import com.taller360.organization.entity.Branch;
import com.taller360.organization.entity.Company;
import com.taller360.vehicles.entity.Vehicle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "citas")
public class Appointment {
  @Id public UUID id;
  @ManyToOne @JoinColumn(name = "empresa_id") public Company empresa;
  @ManyToOne @JoinColumn(name = "sucursal_id") public Branch sucursal;
  @ManyToOne @JoinColumn(name = "cliente_id") public Client cliente;
  @ManyToOne @JoinColumn(name = "vehiculo_id") public Vehicle vehiculo;
  @ManyToOne @JoinColumn(name = "asesor_id") public User asesor;
  @ManyToOne @JoinColumn(name = "tecnico_id") public Technician tecnico;
  @ManyToOne @JoinColumn(name = "bahia_id") public ServiceBay bahia;
  public String servicio;
  @Column(name = "fecha_inicio") public Instant fechaInicio;
  @Column(name = "fecha_fin") public Instant fechaFin;
  @Column(name = "duracion_minutos") public int duracionMinutos;
  public String estado;
  public String notas;
  public boolean sobrecapacidad;
  @Column(name = "recepcion_id") public UUID recepcionId;
  @Column(name = "fecha_creacion") public Instant fechaCreacion;
  @Column(name = "fecha_modificacion") public Instant fechaModificacion;
}
