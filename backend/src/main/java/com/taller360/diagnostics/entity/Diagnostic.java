package com.taller360.diagnostics.entity;

import com.taller360.appointments.entity.Technician;
import com.taller360.clients.entity.Client;
import com.taller360.iam.entity.User;
import com.taller360.organization.entity.Branch;
import com.taller360.organization.entity.Company;
import com.taller360.reception.entity.Reception;
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
@Table(name = "diagnosticos")
public class Diagnostic {
  @Id public UUID id;
  @ManyToOne @JoinColumn(name = "empresa_id") public Company empresa;
  @ManyToOne @JoinColumn(name = "sucursal_id") public Branch sucursal;
  @Column(name = "orden_trabajo_id") public UUID ordenTrabajoId;
  @ManyToOne @JoinColumn(name = "recepcion_id") public Reception recepcion;
  @ManyToOne @JoinColumn(name = "cliente_id") public Client cliente;
  @ManyToOne @JoinColumn(name = "vehiculo_id") public Vehicle vehiculo;
  @ManyToOne @JoinColumn(name = "tecnico_id") public Technician tecnico;
  @Column(name = "sintomas_json") public String sintomasJson;
  @Column(name = "inspecciones_json") public String inspeccionesJson;
  @Column(name = "dtc_json") public String dtcJson;
  @Column(name = "pruebas_json") public String pruebasJson;
  @Column(name = "hallazgos_json") public String hallazgosJson;
  @Column(name = "causa_probable") public String causaProbable;
  @Column(name = "solucion_recomendada") public String solucionRecomendada;
  public String prioridad;
  public String estado;
  @Column(name = "tiempo_estimado_minutos") public int tiempoEstimadoMinutos;
  @Column(name = "ia_sugerencia_json") public String iaSugerenciaJson;
  @ManyToOne @JoinColumn(name = "ia_confirmada_por") public User iaConfirmadaPor;
  @Column(name = "ia_confirmada_en") public Instant iaConfirmadaEn;
  @Column(name = "fecha_creacion") public Instant fechaCreacion;
  @Column(name = "fecha_modificacion") public Instant fechaModificacion;
}
