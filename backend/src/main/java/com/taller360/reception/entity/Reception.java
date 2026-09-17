package com.taller360.reception.entity;

import com.taller360.appointments.entity.Appointment;
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
@Table(name = "recepciones")
public class Reception {
  @Id public UUID id;
  @ManyToOne @JoinColumn(name = "empresa_id") public Company empresa;
  @ManyToOne @JoinColumn(name = "sucursal_id") public Branch sucursal;
  @ManyToOne @JoinColumn(name = "cita_id") public Appointment cita;
  @ManyToOne @JoinColumn(name = "cliente_id") public Client cliente;
  @ManyToOne @JoinColumn(name = "vehiculo_id") public Vehicle vehiculo;
  @ManyToOne @JoinColumn(name = "asesor_id") public User asesor;
  public long kilometraje;
  @Column(name = "combustible_porcentaje") public int combustiblePorcentaje;
  public String motivo;
  @Column(name = "accesorios_json") public String accesoriosJson;
  @Column(name = "checklist_json") public String checklistJson;
  @Column(name = "danos_json") public String danosJson;
  public String observaciones;
  public String estado;
  @Column(name = "version_firmada") public Integer versionFirmada;
  @Column(name = "firmado_en") public Instant firmadoEn;
  @Column(name = "firmado_por") public String firmadoPor;
  @Column(name = "firma_url") public String firmaUrl;
  @Column(name = "pdf_url") public String pdfUrl;
  @Column(name = "orden_trabajo_id") public UUID ordenTrabajoId;
  @Column(name = "fecha_creacion") public Instant fechaCreacion;
  @Column(name = "fecha_modificacion") public Instant fechaModificacion;
}
