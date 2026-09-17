package com.taller360.appointments.entity;

import com.taller360.organization.entity.Branch;
import com.taller360.organization.entity.Company;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "capacidad_agenda")
public class ScheduleCapacity {
  @Id public UUID id;
  @ManyToOne @JoinColumn(name = "empresa_id") public Company empresa;
  @ManyToOne @JoinColumn(name = "sucursal_id") public Branch sucursal;
  @Column(name = "dia_semana") public int diaSemana;
  @Column(name = "hora_inicio") public LocalTime horaInicio;
  @Column(name = "hora_fin") public LocalTime horaFin;
  @Column(name = "capacidad_maxima") public int capacidadMaxima;
  public boolean activo;
  @Column(name = "fecha_creacion") public Instant fechaCreacion;
  @Column(name = "fecha_modificacion") public Instant fechaModificacion;
}
