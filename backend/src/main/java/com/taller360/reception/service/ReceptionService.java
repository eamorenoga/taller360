package com.taller360.reception.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller360.appointments.entity.Appointment;
import com.taller360.appointments.port.ReceptionPort;
import com.taller360.appointments.repository.AppointmentRepository;
import com.taller360.audit.AuditPort;
import com.taller360.clients.entity.Client;
import com.taller360.clients.repository.ClientRepository;
import com.taller360.common.tenant.TenantGuard;
import com.taller360.iam.repository.UserRepository;
import com.taller360.organization.entity.Branch;
import com.taller360.organization.entity.Company;
import com.taller360.organization.repository.BranchRepository;
import com.taller360.organization.repository.CompanyRepository;
import com.taller360.reception.dto.ReceptionDtos.*;
import com.taller360.reception.entity.Reception;
import com.taller360.reception.entity.ReceptionPdf;
import com.taller360.reception.entity.ReceptionPhoto;
import com.taller360.reception.entity.ReceptionSignature;
import com.taller360.reception.port.WorkOrderPort;
import com.taller360.reception.repository.ReceptionPdfRepository;
import com.taller360.reception.repository.ReceptionPhotoRepository;
import com.taller360.reception.repository.ReceptionRepository;
import com.taller360.reception.repository.ReceptionSignatureRepository;
import com.taller360.vehicles.entity.Vehicle;
import com.taller360.vehicles.repository.VehicleRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Primary
public class ReceptionService implements ReceptionPort {
  private static final List<AccessoryItem> DEFAULT_ACCESSORIES = List.of(
      new AccessoryItem("Llaves", true, null),
      new AccessoryItem("Documentos", false, null),
      new AccessoryItem("Herramientas", false, null),
      new AccessoryItem("Llanta de repuesto", false, null));
  private static final List<ChecklistItem> DEFAULT_CHECKLIST = List.of(
      new ChecklistItem("LUCES", "Luces exteriores", true, null),
      new ChecklistItem("FRENOS", "Frenos", true, null),
      new ChecklistItem("NIVELES", "Niveles de fluidos", true, null),
      new ChecklistItem("TESTIGOS", "Testigos en tablero", true, null));

  private final ReceptionRepository receptions;
  private final ReceptionPhotoRepository photos;
  private final ReceptionSignatureRepository signatures;
  private final ReceptionPdfRepository pdfs;
  private final AppointmentRepository appointments;
  private final ClientRepository clients;
  private final VehicleRepository vehicles;
  private final UserRepository users;
  private final CompanyRepository companies;
  private final BranchRepository branches;
  private final TenantGuard tenant;
  private final AuditPort audit;
  private final WorkOrderPort workOrders;
  private final ObjectMapper objectMapper;
  private final SimplePdfGenerator pdfGenerator = new SimplePdfGenerator();

  public ReceptionService(ReceptionRepository receptions, ReceptionPhotoRepository photos,
      ReceptionSignatureRepository signatures, ReceptionPdfRepository pdfs, AppointmentRepository appointments,
      ClientRepository clients, VehicleRepository vehicles, UserRepository users, CompanyRepository companies,
      BranchRepository branches, TenantGuard tenant, AuditPort audit, WorkOrderPort workOrders,
      ObjectMapper objectMapper) {
    this.receptions = receptions;
    this.photos = photos;
    this.signatures = signatures;
    this.pdfs = pdfs;
    this.appointments = appointments;
    this.clients = clients;
    this.vehicles = vehicles;
    this.users = users;
    this.companies = companies;
    this.branches = branches;
    this.tenant = tenant;
    this.audit = audit;
    this.workOrders = workOrders;
    this.objectMapper = objectMapper;
  }

  public List<ReceptionResponse> search(UUID sucursalId, String q) {
    if (sucursalId != null) {
      tenant.assertBranchAllowed(sucursalId);
    }
    return receptions.search(tenant.companyId(), sucursalId, q == null ? "" : q.trim()).stream()
        .map(this::toReception)
        .toList();
  }

  public ReceptionDetailResponse detail(UUID id) {
    Reception reception = receptionInTenant(id);
    return new ReceptionDetailResponse(toReception(reception),
        photos.findByRecepcion_IdAndEmpresa_IdOrderByFechaCreacionDesc(id, tenant.companyId()).stream().map(this::toPhoto).toList(),
        signatures.findByRecepcion_IdAndEmpresa_IdOrderByVersionDesc(id, tenant.companyId()).stream().map(this::toSignature).toList(),
        pdfs.findFirstByRecepcion_IdAndEmpresa_IdOrderByVersionDesc(id, tenant.companyId()).map(this::toPdf).orElse(null));
  }

  @Transactional
  public ReceptionResponse create(ReceptionRequest request) {
    Company company = companies.findById(tenant.companyId()).orElseThrow();
    Reception reception = new Reception();
    reception.id = UUID.randomUUID();
    reception.empresa = company;
    apply(reception, request);
    reception.estado = "BORRADOR";
    reception.fechaCreacion = Instant.now();
    reception.fechaModificacion = Instant.now();
    Reception saved = receptions.save(reception);
    audit.record("RECEPCION", "CREAR", "recepciones", saved.id, null, json(toReception(saved)));
    return toReception(saved);
  }

  @Transactional
  public ReceptionResponse update(UUID id, ReceptionRequest request) {
    Reception reception = receptionInTenant(id);
    assertDraft(reception);
    String before = json(toReception(reception));
    apply(reception, request);
    reception.fechaModificacion = Instant.now();
    Reception saved = receptions.save(reception);
    audit.record("RECEPCION", "EDITAR", "recepciones", saved.id, before, json(toReception(saved)));
    return toReception(saved);
  }

  @Transactional
  public void annul(UUID id) {
    Reception reception = receptionInTenant(id);
    String before = json(toReception(reception));
    reception.estado = "ANULADA";
    reception.fechaModificacion = Instant.now();
    receptions.save(reception);
    audit.record("RECEPCION", "ANULAR", "recepciones", id, before, json(toReception(reception)));
  }

  @Transactional
  public ReceptionPhotoResponse addPhoto(UUID receptionId, ReceptionPhotoRequest request) {
    Reception reception = receptionInTenant(receptionId);
    assertDraft(reception);
    ReceptionPhoto photo = new ReceptionPhoto();
    photo.id = UUID.randomUUID();
    photo.empresa = reception.empresa;
    photo.recepcion = reception;
    photo.url = required(request.url(), "url");
    photo.nombreArchivo = required(request.nombreArchivo(), "nombreArchivo");
    photo.tipo = required(request.tipo(), "tipo");
    if (request.tamanoOriginalBytes() < 0 || request.tamanoComprimidoBytes() < 0) {
      throw new IllegalArgumentException("Los tamanos de foto no pueden ser negativos");
    }
    photo.tamanoOriginalBytes = request.tamanoOriginalBytes();
    photo.tamanoComprimidoBytes = request.tamanoComprimidoBytes();
    photo.ancho = request.ancho();
    photo.alto = request.alto();
    photo.metadatosJson = request.metadatosJson() == null || request.metadatosJson().isBlank() ? "{}" : request.metadatosJson();
    photo.fechaCreacion = Instant.now();
    ReceptionPhoto saved = photos.save(photo);
    audit.record("RECEPCION", "EDITAR", "recepcion_fotos", saved.id, null, json(toPhoto(saved)));
    return toPhoto(saved);
  }

  @Transactional
  public ReceptionDetailResponse sign(UUID id, ReceptionSignatureRequest request) {
    Reception reception = receptionInTenant(id);
    assertDraft(reception);
    int version = reception.versionFirmada == null ? 1 : reception.versionFirmada + 1;
    String hash = sha256(json(toReception(reception)) + "|" + request.firmaUrl());

    ReceptionSignature signature = new ReceptionSignature();
    signature.id = UUID.randomUUID();
    signature.empresa = reception.empresa;
    signature.recepcion = reception;
    signature.firmante = required(request.firmante(), "firmante");
    signature.firmaUrl = required(request.firmaUrl(), "firmaUrl");
    signature.hashContenido = hash;
    signature.version = version;
    signature.fechaCreacion = Instant.now();
    signatures.save(signature);

    byte[] pdfBytes = pdfGenerator.generate("Recepcion digital Taller 360", pdfLines(reception, signature));
    ReceptionPdf pdf = new ReceptionPdf();
    pdf.id = UUID.randomUUID();
    pdf.empresa = reception.empresa;
    pdf.recepcion = reception;
    pdf.version = version;
    pdf.contenidoBase64 = Base64.getEncoder().encodeToString(pdfBytes);
    pdf.hashContenido = sha256(new String(pdfBytes, StandardCharsets.UTF_8));
    pdf.url = "reception://" + reception.id + "/pdf/" + version;
    pdf.fechaCreacion = Instant.now();
    pdfs.save(pdf);

    reception.estado = "FIRMADA";
    reception.versionFirmada = version;
    reception.firmadoEn = signature.fechaCreacion;
    reception.firmadoPor = signature.firmante;
    reception.firmaUrl = signature.firmaUrl;
    reception.pdfUrl = pdf.url;
    reception.fechaModificacion = Instant.now();
    receptions.save(reception);
    audit.record("RECEPCION", "APROBAR", "recepciones", reception.id, null, json(toReception(reception)));
    return detail(reception.id);
  }

  @Transactional
  public WorkOrderConversionResponse createWorkOrder(UUID id) {
    Reception reception = receptionInTenant(id);
    if (!"FIRMADA".equals(reception.estado) && !"CONVERTIDA_OT".equals(reception.estado)) {
      throw new IllegalArgumentException("La recepcion debe estar firmada para crear OT");
    }
    if (reception.ordenTrabajoId == null) {
      String before = json(toReception(reception));
      reception.ordenTrabajoId = workOrders.createFromReception(id);
      reception.estado = "CONVERTIDA_OT";
      reception.fechaModificacion = Instant.now();
      receptions.save(reception);
      audit.record("RECEPCION", "APROBAR", "recepciones", reception.id, before, json(toReception(reception)));
    }
    return new WorkOrderConversionResponse(id, reception.ordenTrabajoId, reception.estado,
        "Orden de trabajo reservada por puerto M08 pendiente");
  }

  @Override
  @Transactional
  public UUID convertAppointment(UUID appointmentId) {
    return receptions.findByCita_IdAndEmpresa_Id(appointmentId, tenant.companyId())
        .map(item -> item.id)
        .orElseGet(() -> createFromAppointment(appointmentId).id());
  }

  private ReceptionResponse createFromAppointment(UUID appointmentId) {
    Appointment appointment = appointments.findByIdAndEmpresa_Id(appointmentId, tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Cita no pertenece a la empresa actual"));
    ReceptionRequest request = new ReceptionRequest(appointment.sucursal.id, appointment.id, appointment.cliente.id,
        appointment.vehiculo.id, appointment.asesor == null ? null : appointment.asesor.id,
        appointment.vehiculo.kilometraje, 0, appointment.servicio, DEFAULT_ACCESSORIES, DEFAULT_CHECKLIST,
        List.of(), appointment.notas);
    return create(request);
  }

  private void apply(Reception reception, ReceptionRequest request) {
    Branch branch = branchInTenant(request.sucursalId());
    Client client = clients.findByIdAndEmpresa_Id(request.clienteId(), tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Cliente no pertenece a la empresa actual"));
    Vehicle vehicle = vehicles.findByIdAndEmpresa_Id(request.vehiculoId(), tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Vehiculo no pertenece a la empresa actual"));
    if (!vehicle.cliente.id.equals(client.id)) {
      throw new IllegalArgumentException("El vehiculo no pertenece al cliente seleccionado");
    }
    reception.sucursal = branch;
    reception.cita = request.citaId() == null ? null : appointments.findByIdAndEmpresa_Id(request.citaId(), tenant.companyId()).orElseThrow();
    reception.cliente = client;
    reception.vehiculo = vehicle;
    reception.asesor = request.asesorId() == null ? null : users.findByIdAndEmpresaId(request.asesorId(), tenant.companyId()).orElseThrow();
    if (request.kilometraje() < vehicle.kilometraje) {
      throw new IllegalArgumentException("El kilometraje no puede ser menor al registrado del vehiculo");
    }
    if (request.combustiblePorcentaje() < 0 || request.combustiblePorcentaje() > 100) {
      throw new IllegalArgumentException("El combustible debe estar entre 0 y 100");
    }
    reception.kilometraje = request.kilometraje();
    reception.combustiblePorcentaje = request.combustiblePorcentaje();
    reception.motivo = required(request.motivo(), "motivo");
    reception.accesoriosJson = json(request.accesorios() == null ? List.of() : request.accesorios());
    reception.checklistJson = json(request.checklist() == null ? List.of() : request.checklist());
    reception.danosJson = json(request.danos() == null ? List.of() : request.danos());
    reception.observaciones = request.observaciones();
  }

  private Reception receptionInTenant(UUID id) {
    return receptions.findByIdAndEmpresa_Id(id, tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Recepcion no pertenece a la empresa actual"));
  }

  private Branch branchInTenant(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("El campo sucursalId es obligatorio");
    }
    tenant.assertBranchAllowed(id);
    return branches.findByIdAndEmpresaId(id, tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Sucursal no pertenece a la empresa actual"));
  }

  private void assertDraft(Reception reception) {
    if (!"BORRADOR".equals(reception.estado)) {
      throw new IllegalArgumentException("La recepcion firmada o anulada no se puede modificar");
    }
  }

  private ReceptionResponse toReception(Reception reception) {
    return new ReceptionResponse(reception.id, reception.empresa.id, reception.sucursal.id,
        reception.cita == null ? null : reception.cita.id, reception.cliente.id, reception.cliente.nombre,
        reception.vehiculo.id, reception.vehiculo.placa, reception.asesor == null ? null : reception.asesor.id,
        reception.kilometraje, reception.combustiblePorcentaje, reception.motivo,
        list(reception.accesoriosJson, new TypeReference<List<AccessoryItem>>() {}),
        list(reception.checklistJson, new TypeReference<List<ChecklistItem>>() {}),
        list(reception.danosJson, new TypeReference<List<DamageItem>>() {}),
        reception.observaciones, reception.estado, reception.versionFirmada, reception.firmadoEn, reception.firmadoPor,
        reception.firmaUrl, reception.pdfUrl, reception.ordenTrabajoId, reception.fechaCreacion, reception.fechaModificacion);
  }

  private ReceptionPhotoResponse toPhoto(ReceptionPhoto photo) {
    return new ReceptionPhotoResponse(photo.id, photo.url, photo.nombreArchivo, photo.tipo, photo.tamanoOriginalBytes,
        photo.tamanoComprimidoBytes, photo.ancho, photo.alto, photo.metadatosJson, photo.fechaCreacion);
  }

  private ReceptionSignatureResponse toSignature(ReceptionSignature signature) {
    return new ReceptionSignatureResponse(signature.id, signature.firmante, signature.firmaUrl, signature.hashContenido,
        signature.version, signature.fechaCreacion);
  }

  private ReceptionPdfResponse toPdf(ReceptionPdf pdf) {
    return new ReceptionPdfResponse(pdf.id, pdf.version, pdf.url, pdf.contenidoBase64, pdf.hashContenido,
        pdf.fechaCreacion);
  }

  private List<String> pdfLines(Reception reception, ReceptionSignature signature) {
    return List.of(
        "Recepcion: " + reception.id,
        "Cliente: " + reception.cliente.nombre,
        "Vehiculo: " + reception.vehiculo.placa,
        "Kilometraje: " + reception.kilometraje,
        "Combustible: " + reception.combustiblePorcentaje + "%",
        "Motivo: " + reception.motivo,
        "Observaciones: " + (reception.observaciones == null ? "" : reception.observaciones),
        "Firmante: " + signature.firmante,
        "Hash: " + signature.hashContenido);
  }

  private <T> List<T> list(String value, TypeReference<List<T>> type) {
    try {
      return objectMapper.readValue(value == null || value.isBlank() ? "[]" : value, type);
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

  private String required(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("El campo " + field + " es obligatorio");
    }
    return value.trim();
  }

  private String sha256(String value) {
    try {
      byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder();
      for (byte item : digest) {
        hex.append(String.format("%02x", item));
      }
      return hex.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
