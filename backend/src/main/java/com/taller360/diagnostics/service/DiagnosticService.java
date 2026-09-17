package com.taller360.diagnostics.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller360.appointments.repository.TechnicianRepository;
import com.taller360.audit.AuditPort;
import com.taller360.clients.entity.Client;
import com.taller360.clients.repository.ClientRepository;
import com.taller360.common.tenant.TenantGuard;
import com.taller360.diagnostics.dto.DiagnosticDtos.*;
import com.taller360.diagnostics.entity.Diagnostic;
import com.taller360.diagnostics.entity.DiagnosticEvidence;
import com.taller360.diagnostics.entity.DiagnosticPart;
import com.taller360.diagnostics.entity.DiagnosticTask;
import com.taller360.diagnostics.port.WorkOrderSnapshotPort;
import com.taller360.diagnostics.repository.DiagnosticEvidenceRepository;
import com.taller360.diagnostics.repository.DiagnosticPartRepository;
import com.taller360.diagnostics.repository.DiagnosticRepository;
import com.taller360.diagnostics.repository.DiagnosticTaskRepository;
import com.taller360.iam.entity.User;
import com.taller360.iam.repository.UserRepository;
import com.taller360.organization.entity.Branch;
import com.taller360.organization.entity.Company;
import com.taller360.organization.repository.BranchRepository;
import com.taller360.organization.repository.CompanyRepository;
import com.taller360.reception.repository.ReceptionRepository;
import com.taller360.vehicles.entity.Vehicle;
import com.taller360.vehicles.repository.VehicleRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DiagnosticService {
  private static final Set<String> PRIORITIES = Set.of("BAJA", "MEDIA", "ALTA", "CRITICA");
  private static final Set<String> STATES = Set.of("BORRADOR", "EN_PROCESO", "PENDIENTE_CONFIRMACION", "CONFIRMADO", "ANULADO");
  private static final Set<String> EVIDENCE_TYPES = Set.of("FOTO", "VIDEO", "AUDIO", "DOCUMENTO");

  private final DiagnosticRepository diagnostics;
  private final DiagnosticEvidenceRepository evidences;
  private final DiagnosticTaskRepository tasks;
  private final DiagnosticPartRepository parts;
  private final ReceptionRepository receptions;
  private final ClientRepository clients;
  private final VehicleRepository vehicles;
  private final TechnicianRepository technicians;
  private final UserRepository users;
  private final CompanyRepository companies;
  private final BranchRepository branches;
  private final WorkOrderSnapshotPort workOrders;
  private final TenantGuard tenant;
  private final AuditPort audit;
  private final ObjectMapper objectMapper;

  public DiagnosticService(DiagnosticRepository diagnostics, DiagnosticEvidenceRepository evidences,
      DiagnosticTaskRepository tasks, DiagnosticPartRepository parts, ReceptionRepository receptions,
      ClientRepository clients, VehicleRepository vehicles, TechnicianRepository technicians, UserRepository users,
      CompanyRepository companies, BranchRepository branches, WorkOrderSnapshotPort workOrders, TenantGuard tenant,
      AuditPort audit, ObjectMapper objectMapper) {
    this.diagnostics = diagnostics;
    this.evidences = evidences;
    this.tasks = tasks;
    this.parts = parts;
    this.receptions = receptions;
    this.clients = clients;
    this.vehicles = vehicles;
    this.technicians = technicians;
    this.users = users;
    this.companies = companies;
    this.branches = branches;
    this.workOrders = workOrders;
    this.tenant = tenant;
    this.audit = audit;
    this.objectMapper = objectMapper;
  }

  public List<DiagnosticResponse> search(UUID sucursalId, String q) {
    if (sucursalId != null) {
      tenant.assertBranchAllowed(sucursalId);
    }
    return diagnostics.search(tenant.companyId(), sucursalId, q == null ? "" : q.trim()).stream()
        .map(this::toDiagnostic)
        .toList();
  }

  public DiagnosticDetailResponse detail(UUID id) {
    Diagnostic diagnostic = diagnosticInTenant(id);
    return new DiagnosticDetailResponse(toDiagnostic(diagnostic),
        evidences.findByDiagnostico_IdAndEmpresa_IdOrderByFechaCreacionDesc(id, tenant.companyId()).stream().map(this::toEvidence).toList(),
        tasks.findByDiagnostico_IdAndEmpresa_IdOrderByFechaCreacionDesc(id, tenant.companyId()).stream().map(this::toTask).toList(),
        parts.findByDiagnostico_IdAndEmpresa_IdOrderByFechaCreacionDesc(id, tenant.companyId()).stream().map(this::toPart).toList());
  }

  @Transactional
  public DiagnosticResponse create(DiagnosticRequest request) {
    Company company = companies.findById(tenant.companyId()).orElseThrow();
    Diagnostic diagnostic = new Diagnostic();
    diagnostic.id = UUID.randomUUID();
    diagnostic.empresa = company;
    apply(diagnostic, request);
    diagnostic.fechaCreacion = Instant.now();
    diagnostic.fechaModificacion = Instant.now();
    Diagnostic saved = diagnostics.save(diagnostic);
    audit.record("DIAGNOSTICOS", "CREAR", "diagnosticos", saved.id, null, json(toDiagnostic(saved)));
    return toDiagnostic(saved);
  }

  @Transactional
  public DiagnosticResponse update(UUID id, DiagnosticRequest request) {
    Diagnostic diagnostic = diagnosticInTenant(id);
    String before = json(toDiagnostic(diagnostic));
    apply(diagnostic, request);
    diagnostic.fechaModificacion = Instant.now();
    Diagnostic saved = diagnostics.save(diagnostic);
    audit.record("DIAGNOSTICOS", "EDITAR", "diagnosticos", saved.id, before, json(toDiagnostic(saved)));
    return toDiagnostic(saved);
  }

  @Transactional
  public void annul(UUID id) {
    Diagnostic diagnostic = diagnosticInTenant(id);
    String before = json(toDiagnostic(diagnostic));
    diagnostic.estado = "ANULADO";
    diagnostic.fechaModificacion = Instant.now();
    diagnostics.save(diagnostic);
    audit.record("DIAGNOSTICOS", "ANULAR", "diagnosticos", id, before, json(toDiagnostic(diagnostic)));
  }

  @Transactional
  public EvidenceResponse addEvidence(UUID diagnosticId, EvidenceRequest request) {
    Diagnostic diagnostic = diagnosticInTenant(diagnosticId);
    DiagnosticEvidence evidence = new DiagnosticEvidence();
    evidence.id = UUID.randomUUID();
    evidence.empresa = diagnostic.empresa;
    evidence.diagnostico = diagnostic;
    evidence.tipo = evidenceType(request.tipo());
    evidence.url = required(request.url(), "url");
    evidence.nombreArchivo = required(request.nombreArchivo(), "nombreArchivo");
    evidence.mimeType = request.mimeType();
    if (request.tamanoBytes() < 0) {
      throw new IllegalArgumentException("El tamano de evidencia no puede ser negativo");
    }
    evidence.tamanoBytes = request.tamanoBytes();
    evidence.metadatosJson = request.metadatosJson() == null || request.metadatosJson().isBlank() ? "{}" : request.metadatosJson();
    evidence.fechaCreacion = Instant.now();
    DiagnosticEvidence saved = evidences.save(evidence);
    audit.record("DIAGNOSTICOS", "EDITAR", "diagnostico_evidencias", saved.id, null, json(toEvidence(saved)));
    return toEvidence(saved);
  }

  @Transactional
  public TaskResponse addTask(UUID diagnosticId, TaskRequest request) {
    Diagnostic diagnostic = diagnosticInTenant(diagnosticId);
    DiagnosticTask task = new DiagnosticTask();
    task.id = UUID.randomUUID();
    task.empresa = diagnostic.empresa;
    task.diagnostico = diagnostic;
    task.descripcion = required(request.descripcion(), "descripcion");
    task.prioridad = priority(request.prioridad() == null ? diagnostic.prioridad : request.prioridad());
    task.estado = request.estado() == null || request.estado().isBlank() ? "PENDIENTE" : request.estado().trim().toUpperCase();
    if (request.tiempoEstimadoMinutos() < 0) {
      throw new IllegalArgumentException("El tiempo estimado no puede ser negativo");
    }
    task.tiempoEstimadoMinutos = request.tiempoEstimadoMinutos();
    task.fechaCreacion = Instant.now();
    DiagnosticTask saved = tasks.save(task);
    audit.record("DIAGNOSTICOS", "EDITAR", "diagnostico_tareas", saved.id, null, json(toTask(saved)));
    return toTask(saved);
  }

  @Transactional
  public PartResponse addPart(UUID diagnosticId, PartRequest request) {
    Diagnostic diagnostic = diagnosticInTenant(diagnosticId);
    DiagnosticPart part = new DiagnosticPart();
    part.id = UUID.randomUUID();
    part.empresa = diagnostic.empresa;
    part.diagnostico = diagnostic;
    part.codigo = request.codigo();
    part.nombre = required(request.nombre(), "nombre");
    BigDecimal quantity = request.cantidad() == null ? BigDecimal.ONE : request.cantidad();
    if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
    }
    part.cantidad = quantity;
    part.requerido = request.requerido();
    part.notas = request.notas();
    part.fechaCreacion = Instant.now();
    DiagnosticPart saved = parts.save(part);
    audit.record("DIAGNOSTICOS", "EDITAR", "diagnostico_repuestos", saved.id, null, json(toPart(saved)));
    return toPart(saved);
  }

  @Transactional
  public AiSuggestionResponse assist(UUID diagnosticId, AiAssistRequest request) {
    Diagnostic diagnostic = diagnosticInTenant(diagnosticId);
    AiSuggestionResponse suggestion = buildSuggestion(diagnostic, request);
    diagnostic.iaSugerenciaJson = json(suggestion);
    diagnostic.estado = "PENDIENTE_CONFIRMACION";
    diagnostic.fechaModificacion = Instant.now();
    diagnostics.save(diagnostic);
    audit.record("DIAGNOSTICOS", "APROBAR", "diagnosticos_ia", diagnostic.id, null, json(suggestion));
    return suggestion;
  }

  @Transactional
  public DiagnosticResponse confirmAi(UUID diagnosticId) {
    Diagnostic diagnostic = diagnosticInTenant(diagnosticId);
    if (diagnostic.iaSugerenciaJson == null || diagnostic.iaSugerenciaJson.isBlank()) {
      throw new IllegalArgumentException("No hay sugerencia IA para confirmar");
    }
    User user = users.findByIdAndEmpresaId(tenant.userId(), tenant.companyId()).orElseThrow();
    diagnostic.iaConfirmadaPor = user;
    diagnostic.iaConfirmadaEn = Instant.now();
    diagnostic.estado = "CONFIRMADO";
    diagnostic.fechaModificacion = Instant.now();
    diagnostics.save(diagnostic);
    audit.record("DIAGNOSTICOS", "APROBAR", "diagnosticos", diagnostic.id, null, json(toDiagnostic(diagnostic)));
    return toDiagnostic(diagnostic);
  }

  private void apply(Diagnostic diagnostic, DiagnosticRequest request) {
    Branch branch = branchInTenant(request.sucursalId());
    UUID workOrderId = requiredId(request.ordenTrabajoId(), "ordenTrabajoId");
    workOrders.find(workOrderId, tenant.companyId()).ifPresent(snapshot -> {
      if (!snapshot.branchId().equals(branch.id)) {
        throw new AccessDeniedException("La OT no pertenece a la sucursal actual");
      }
    });
    Client client = clients.findByIdAndEmpresa_Id(request.clienteId(), tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Cliente no pertenece a la empresa actual"));
    Vehicle vehicle = vehicles.findByIdAndEmpresa_Id(request.vehiculoId(), tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Vehiculo no pertenece a la empresa actual"));
    if (!vehicle.cliente.id.equals(client.id)) {
      throw new IllegalArgumentException("El vehiculo no pertenece al cliente seleccionado");
    }
    diagnostic.sucursal = branch;
    diagnostic.ordenTrabajoId = workOrderId;
    diagnostic.recepcion = request.recepcionId() == null ? null : receptions.findByIdAndEmpresa_Id(request.recepcionId(), tenant.companyId()).orElseThrow();
    diagnostic.cliente = client;
    diagnostic.vehiculo = vehicle;
    diagnostic.tecnico = request.tecnicoId() == null ? null : technicians.findByIdAndEmpresa_Id(request.tecnicoId(), tenant.companyId()).orElseThrow();
    diagnostic.sintomasJson = json(request.sintomas() == null ? List.of() : request.sintomas());
    diagnostic.inspeccionesJson = json(request.inspecciones() == null ? List.of() : request.inspecciones());
    diagnostic.dtcJson = json(request.dtc() == null ? List.of() : request.dtc());
    diagnostic.pruebasJson = json(request.pruebas() == null ? List.of() : request.pruebas());
    diagnostic.hallazgosJson = json(request.hallazgos() == null ? List.of() : request.hallazgos());
    diagnostic.causaProbable = request.causaProbable();
    diagnostic.solucionRecomendada = request.solucionRecomendada();
    diagnostic.prioridad = priority(request.prioridad() == null ? "MEDIA" : request.prioridad());
    diagnostic.estado = state(request.estado() == null ? "BORRADOR" : request.estado());
    if (request.tiempoEstimadoMinutos() < 0) {
      throw new IllegalArgumentException("El tiempo estimado no puede ser negativo");
    }
    diagnostic.tiempoEstimadoMinutos = request.tiempoEstimadoMinutos();
  }

  private AiSuggestionResponse buildSuggestion(Diagnostic diagnostic, AiAssistRequest request) {
    List<DtcItem> dtc = list(diagnostic.dtcJson, new TypeReference<List<DtcItem>>() {});
    List<SymptomItem> symptoms = list(diagnostic.sintomasJson, new TypeReference<List<SymptomItem>>() {});
    boolean hasDtc = request == null || request.incluirDtc();
    String probable = hasDtc && !dtc.isEmpty()
        ? "Validar circuito o componente asociado al DTC " + dtc.get(0).codigo()
        : symptoms.isEmpty() ? "Revisar sistemas relacionados con el motivo reportado" : "Correlacionar sintomas reportados con inspeccion visual";
    return new AiSuggestionResponse(
        List.of(probable, "Comparar valores reales contra especificacion del fabricante"),
        List.of("Escaneo completo", "Prueba de actuadores", "Inspeccion visual guiada", "Prueba de ruta controlada"),
        List.of("No reemplazar componentes sin prueba confirmatoria", "Registrar evidencia antes y despues de la reparacion"),
        List.of("Sensor relacionado", "Arnes o conector", "Consumibles segun inspeccion"),
        diagnostic.prioridad,
        true,
        "Sugerencia de apoyo. Requiere confirmacion tecnica antes de usarse en una OT.");
  }

  private Diagnostic diagnosticInTenant(UUID id) {
    return diagnostics.findByIdAndEmpresa_Id(id, tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Diagnostico no pertenece a la empresa actual"));
  }

  private Branch branchInTenant(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("El campo sucursalId es obligatorio");
    }
    tenant.assertBranchAllowed(id);
    return branches.findByIdAndEmpresaId(id, tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Sucursal no pertenece a la empresa actual"));
  }

  private DiagnosticResponse toDiagnostic(Diagnostic diagnostic) {
    return new DiagnosticResponse(diagnostic.id, diagnostic.empresa.id, diagnostic.sucursal.id,
        diagnostic.ordenTrabajoId, diagnostic.recepcion == null ? null : diagnostic.recepcion.id,
        diagnostic.cliente.id, diagnostic.cliente.nombre, diagnostic.vehiculo.id, diagnostic.vehiculo.placa,
        diagnostic.tecnico == null ? null : diagnostic.tecnico.id,
        list(diagnostic.sintomasJson, new TypeReference<List<SymptomItem>>() {}),
        list(diagnostic.inspeccionesJson, new TypeReference<List<InspectionItem>>() {}),
        list(diagnostic.dtcJson, new TypeReference<List<DtcItem>>() {}),
        list(diagnostic.pruebasJson, new TypeReference<List<TestItem>>() {}),
        list(diagnostic.hallazgosJson, new TypeReference<List<FindingItem>>() {}),
        diagnostic.causaProbable, diagnostic.solucionRecomendada, diagnostic.prioridad, diagnostic.estado,
        diagnostic.tiempoEstimadoMinutos,
        diagnostic.iaSugerenciaJson == null ? null : value(diagnostic.iaSugerenciaJson, AiSuggestionResponse.class),
        diagnostic.iaConfirmadaPor == null ? null : diagnostic.iaConfirmadaPor.id,
        diagnostic.iaConfirmadaEn, diagnostic.fechaCreacion, diagnostic.fechaModificacion);
  }

  private EvidenceResponse toEvidence(DiagnosticEvidence evidence) {
    return new EvidenceResponse(evidence.id, evidence.tipo, evidence.url, evidence.nombreArchivo, evidence.mimeType,
        evidence.tamanoBytes, evidence.metadatosJson, evidence.fechaCreacion);
  }

  private TaskResponse toTask(DiagnosticTask task) {
    return new TaskResponse(task.id, task.descripcion, task.prioridad, task.estado, task.tiempoEstimadoMinutos,
        task.fechaCreacion);
  }

  private PartResponse toPart(DiagnosticPart part) {
    return new PartResponse(part.id, part.codigo, part.nombre, part.cantidad, part.requerido, part.notas,
        part.fechaCreacion);
  }

  private String priority(String value) {
    String normalized = value.toUpperCase();
    if (!PRIORITIES.contains(normalized)) {
      throw new IllegalArgumentException("Prioridad de diagnostico no valida");
    }
    return normalized;
  }

  private String state(String value) {
    String normalized = value.toUpperCase();
    if (!STATES.contains(normalized)) {
      throw new IllegalArgumentException("Estado de diagnostico no valido");
    }
    return normalized;
  }

  private String evidenceType(String value) {
    String normalized = required(value, "tipo").toUpperCase();
    if (!EVIDENCE_TYPES.contains(normalized)) {
      throw new IllegalArgumentException("Tipo de evidencia no valido");
    }
    return normalized;
  }

  private UUID requiredId(UUID value, String field) {
    if (value == null) {
      throw new IllegalArgumentException("El campo " + field + " es obligatorio");
    }
    return value;
  }

  private String required(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("El campo " + field + " es obligatorio");
    }
    return value.trim();
  }

  private <T> List<T> list(String value, TypeReference<List<T>> type) {
    try {
      return objectMapper.readValue(value == null || value.isBlank() ? "[]" : value, type);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private <T> T value(String raw, Class<T> type) {
    try {
      return objectMapper.readValue(raw, type);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private String json(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }
}
