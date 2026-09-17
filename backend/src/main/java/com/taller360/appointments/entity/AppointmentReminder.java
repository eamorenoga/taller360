package com.taller360.appointments.entity;

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
@Table(name = "cita_recordatorios")
public class AppointmentReminder {
  @Id public UUID id;
  @ManyToOne @JoinColumn(name = "empresa_id") public Company empresa;
  @ManyToOne @JoinColumn(name = "cita_id") public Appointment cita;
  public String canal;
  @Column(name = "programado_para") public Instant programadoPara;
  @Column(name = "enviado_en") public Instant enviadoEn;
  public String estado;
  public String mensaje;
  @Column(name = "fecha_creacion") public Instant fechaCreacion;
}
