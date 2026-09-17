package com.taller360.appointments.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller360.appointments.dto.AppointmentDtos.*;
import com.taller360.appointments.entity.Appointment;
import com.taller360.appointments.entity.AppointmentReminder;
import com.taller360.appointments.entity.ScheduleCapacity;
import com.taller360.appointments.entity.ServiceBay;
import com.taller360.appointments.entity.Technician;
import com.taller360.appointments.port.ReceptionPort;
import com.taller360.appointments.repository.AppointmentReminderRepository;
import com.taller360.appointments.repository.AppointmentRepository;
import com.taller360.appointments.repository.ScheduleCapacityRepository;
import com.taller360.appointments.repository.ServiceBayRepository;
import com.taller360.appointments.repository.TechnicianRepository;
import com.taller360.audit.AuditPort;
import com.taller360.clients.entity.Client;
import com.taller360.clients.repository.ClientRepository;
import com.taller360.common.tenant.TenantGuard;
import com.taller360.iam.repository.UserRepository;
import com.taller360.organization.entity.Branch;
import com.taller360.organization.entity.Company;
import com.taller360.organization.repository.BranchRepository;
import com.taller360.organization.repository.CompanyRepository;
import com.taller360.vehicles.entity.Vehicle;
import com.taller360.vehicles.repository.VehicleRepository;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {
  private static final Set<String> STATES = Set.of("PROGRAMADA", "CONFIRMADA", "LLEGO", "CANCELADA", "NO_ASISTIO");

  private final AppointmentRepository appointments;
  private final AppointmentReminderRepository reminders;
  private final ScheduleCapacityRepository capacities;
  private final ClientRepository clients;
  private final VehicleRepository vehicles;
  private final UserRepository users;
  private final TechnicianRepository technicians;
  private final ServiceBayRepository bays;
  private final CompanyRepository companies;
  private final BranchRepository branches;
  private final TenantGuard tenant;
  private final AuditPort audit;
  private final ReceptionPort receptionPort;
  private final ObjectMapper objectMapper;

  public AppointmentService(AppointmentRepository appointments, AppointmentReminderRepository reminders,
      ScheduleCapacityRepository capacities, ClientRepository clients, VehicleRepository vehicles,
      UserRepository users, TechnicianRepository technicians, ServiceBayRepository bays, CompanyRepository companies,
      BranchRepository branches, TenantGuard tenant, AuditPort audit, ReceptionPort receptionPort,
      ObjectMapper objectMapper) {
    this.appointments = appointments;
    this.reminders = reminders;
    this.capacities = capacities;
    this.clients = clients;
    this.vehicles = vehicles;
    this.users = users;
    this.technicians = technicians;
    this.bays = bays;
    this.companies = companies;
    this.branches = branches;
    this.tenant = tenant;
    this.audit = audit;
    this.receptionPort = receptionPort;
    this.objectMapper = objectMapper;
  }

  public CalendarResponse calendar(Instant start, Instant end, UUID sucursalId, String view) {
    if (start == null || end == null || !end.isAfter(start)) {
      throw new IllegalArgumentException("El rango de agenda no es valido");
    }
    if (sucursalId != null) {
      tenant.assertBranchAllowed(sucursalId);
    }
    return new CalendarResponse(start, end, view == null ? "SEMANA" : view.toUpperCase(),
        appointments.calendar(tenant.companyId(), sucursalId, start, end).stream().map(this::toAppointment).toList());
  }

  @Transactional
  public AppointmentResponse create(AppointmentRequest request) {
    Company company = companies.findById(tenant.companyId()).orElseThrow();
    Appointment appointment = new Appointment();
    appointment.id = UUID.randomUUID();
    appointment.empresa = company;
    apply(appointment, request);
    appointment.fechaCreacion = Instant.now();
    appointment.fechaModificacion = Instant.now();
    appointment.sobrecapacidad = capacity(appointment.sucursal.id, appointment.fechaInicio, appointment.fechaFin, null).sobrecapacidad();
    Appointment saved = appointments.save(appointment);
    audit.record("CITAS", "CREAR", "citas", saved.id, null, json(toAppointment(saved)));
    return toAppointment(saved);
  }

  @Transactional
  public AppointmentResponse update(UUID id, AppointmentRequest request) {
    Appointment appointment = appointmentInTenant(id);
    String before = json(toAppointment(appointment));
    apply(appointment, request);
    appointment.fechaModificacion = Instant.now();
    appointment.sobrecapacidad = capacity(appointment.sucursal.id, appointment.fechaInicio, appointment.fechaFin, appointment.id).sobrecapacidad();
    Appointment saved = appointments.save(appointment);
    audit.record("CITAS", "EDITAR", "citas", saved.id, before, json(toAppointment(saved)));
    return toAppointment(saved);
  }

  @Transactional
  public AppointmentResponse changeState(UUID id, String state) {
    Appointment appointment = appointmentInTenant(id);
    String before = json(toAppointment(appointment));
    appointment.estado = state(state);
    appointment.fechaModificacion = Instant.now();
    Appointment saved = appointments.save(appointment);
    audit.record("CITAS", "EDITAR", "citas", saved.id, before, json(toAppointment(saved)));
    return toAppointment(saved);
  }

  @Transactional
  public void cancel(UUID id) {
    changeState(id, "CANCELADA");
  }

  @Transactional
  public AppointmentReminderResponse addReminder(UUID appointmentId, AppointmentReminderRequest request) {
    Appointment appointment = appointmentInTenant(appointmentId);
    AppointmentReminder reminder = new AppointmentReminder();
    reminder.id = UUID.randomUUID();
    reminder.empresa = appointment.empresa;
    reminder.cita = appointment;
    reminder.canal = required(request.canal(), "canal").toUpperCase();
    if (request.programadoPara() == null) {
      throw new IllegalArgumentException("El campo programadoPara es obligatorio");
    }
    reminder.programadoPara = request.programadoPara();
    reminder.estado = "PENDIENTE";
    reminder.mensaje = request.mensaje();
    reminder.fechaCreacion = Instant.now();
    AppointmentReminder saved = reminders.save(reminder);
    audit.record("CITAS", "CREAR", "cita_recordatorios", saved.id, null, json(toReminder(saved)));
    return toReminder(saved);
  }

  public List<AppointmentReminderResponse> reminders(UUID appointmentId) {
    appointmentInTenant(appointmentId);
    return reminders.findByCita_IdAndEmpresa_IdOrderByProgramadoParaAsc(appointmentId, tenant.companyId()).stream().map(this::toReminder).toList();
  }

  public List<TechnicianResponse> technicians() {
    return technicians.findByEmpresa_IdOrderByNombreAsc(tenant.companyId()).stream()
        .filter(item -> item.activo)
        .map(this::toTechnician)
        .toList();
  }

  public List<ServiceBayResponse> bays(UUID sucursalId) {
    Branch branch = branchInTenant(sucursalId);
    return bays.findByEmpresa_IdAndSucursal_IdAndActivaTrueOrderByNombreAsc(tenant.companyId(), branch.id).stream()
        .map(this::toBay)
        .toList();
  }

  public List<ScheduleCapacityResponse> capacityRules(UUID sucursalId) {
    Branch branch = branchInTenant(sucursalId);
    return capacities.findByEmpresa_IdAndSucursal_IdOrderByDiaSemanaAscHoraInicioAsc(tenant.companyId(), branch.id)
        .stream()
        .map(this::toCapacityRule)
        .toList();
  }

  public CapacityResponse capacity(UUID sucursalId, Instant start, Instant end) {
    return capacity(sucursalId, start, end, null);
  }

  private CapacityResponse capacity(UUID sucursalId, Instant start, Instant end, UUID excludeAppointmentId) {
    if (start == null || end == null || !end.isAfter(start)) {
      throw new IllegalArgumentException("El rango de agenda no es valido");
    }
    Branch branch = branchInTenant(sucursalId);
    DayOfWeek day = start.atZone(ZoneId.of("America/Bogota")).getDayOfWeek();
    int dayValue = day.getValue();
    int configured = capacities.findByEmpresa_IdAndSucursal_IdAndDiaSemanaAndActivoTrue(tenant.companyId(), branch.id, dayValue)
        .stream()
        .mapToInt(item -> item.capacidadMaxima)
        .max()
        .orElse(1);
    long count = appointments.overlappingCount(tenant.companyId(), branch.id, start, end, excludeAppointmentId);
    return new CapacityResponse(branch.id, start, end, count, configured, count >= configured);
  }

  @Transactional
  public ReceptionConversionResponse convertToReception(UUID id) {
    Appointment appointment = appointmentInTenant(id);
    String before = json(toAppointment(appointment));
    UUID receptionId = receptionPort.convertAppointment(id);
    appointment.recepcionId = receptionId;
    appointment.estado = "LLEGO";
    appointment.fechaModificacion = Instant.now();
    appointments.save(appointment);
    audit.record("CITAS", "APROBAR", "citas", id, before, json(toAppointment(appointment)));
    return new ReceptionConversionResponse(id, receptionId, appointment.estado, "Recepcion reservada por puerto M07 pendiente");
  }

  private void apply(Appointment appointment, AppointmentRequest request) {
    Branch branch = branchInTenant(request.sucursalId());
    Client client = clients.findByIdAndEmpresa_Id(request.clienteId(), tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Cliente no pertenece a la empresa actual"));
    Vehicle vehicle = vehicles.findByIdAndEmpresa_Id(request.vehiculoId(), tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Vehiculo no pertenece a la empresa actual"));
    if (!vehicle.cliente.id.equals(client.id)) {
      throw new IllegalArgumentException("El vehiculo no pertenece al cliente seleccionado");
    }
    appointment.sucursal = branch;
    appointment.cliente = client;
    appointment.vehiculo = vehicle;
    appointment.asesor = request.asesorId() == null ? null : users.findByIdAndEmpresaId(request.asesorId(), tenant.companyId()).orElseThrow();
    appointment.tecnico = request.tecnicoId() == null ? null : technicians.findByIdAndEmpresa_Id(request.tecnicoId(), tenant.companyId()).orElseThrow();
    appointment.bahia = request.bahiaId() == null ? null : bays.findByIdAndEmpresa_Id(request.bahiaId(), tenant.companyId()).orElseThrow();
    appointment.servicio = required(request.servicio(), "servicio");
    if (request.fechaInicio() == null) {
      throw new IllegalArgumentException("El campo fechaInicio es obligatorio");
    }
    appointment.fechaInicio = request.fechaInicio();
    appointment.duracionMinutos = request.duracionMinutos() <= 0 ? 60 : request.duracionMinutos();
    appointment.fechaFin = appointment.fechaInicio.plusSeconds(appointment.duracionMinutos * 60L);
    appointment.estado = state(request.estado() == null ? "PROGRAMADA" : request.estado());
    appointment.notas = request.notas();
  }

  private Appointment appointmentInTenant(UUID id) {
    return appointments.findByIdAndEmpresa_Id(id, tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Cita no pertenece a la empresa actual"));
  }

  private Branch branchInTenant(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("El campo sucursalId es obligatorio");
    }
    tenant.assertBranchAllowed(id);
    return branches.findByIdAndEmpresaId(id, tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Sucursal no pertenece a la empresa actual"));
  }

  private String state(String value) {
    String normalized = value.toUpperCase();
    if (!STATES.contains(normalized)) {
      throw new IllegalArgumentException("Estado de cita no valido");
    }
    return normalized;
  }

  private AppointmentResponse toAppointment(Appointment appointment) {
    return new AppointmentResponse(appointment.id, appointment.empresa.id, appointment.sucursal.id,
        appointment.cliente.id, appointment.cliente.nombre, appointment.vehiculo.id, appointment.vehiculo.placa,
        appointment.asesor == null ? null : appointment.asesor.id,
        appointment.tecnico == null ? null : appointment.tecnico.id,
        appointment.bahia == null ? null : appointment.bahia.id,
        appointment.servicio, appointment.fechaInicio, appointment.fechaFin, appointment.duracionMinutos,
        appointment.estado, appointment.notas, appointment.sobrecapacidad, appointment.recepcionId);
  }

  private AppointmentReminderResponse toReminder(AppointmentReminder reminder) {
    return new AppointmentReminderResponse(reminder.id, reminder.canal, reminder.programadoPara, reminder.enviadoEn,
        reminder.estado, reminder.mensaje);
  }

  private TechnicianResponse toTechnician(Technician technician) {
    return new TechnicianResponse(technician.id, technician.sucursal == null ? null : technician.sucursal.id,
        technician.usuario == null ? null : technician.usuario.id, technician.nombre, technician.especialidad,
        technician.activo);
  }

  private ServiceBayResponse toBay(ServiceBay bay) {
    return new ServiceBayResponse(bay.id, bay.sucursal.id, bay.nombre, bay.tipo, bay.activa);
  }

  private ScheduleCapacityResponse toCapacityRule(ScheduleCapacity rule) {
    return new ScheduleCapacityResponse(rule.id, rule.sucursal.id, rule.diaSemana, rule.horaInicio, rule.horaFin,
        rule.capacidadMaxima, rule.activo);
  }

  private String json(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private String required(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("El campo " + field + " es obligatorio");
    }
    return value.trim();
  }
}
