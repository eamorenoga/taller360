package com.taller360.vehicles.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller360.audit.AuditPort;
import com.taller360.clients.entity.Client;
import com.taller360.clients.repository.ClientRepository;
import com.taller360.common.tenant.TenantGuard;
import com.taller360.organization.entity.Branch;
import com.taller360.organization.repository.BranchRepository;
import com.taller360.organization.repository.CompanyRepository;
import com.taller360.vehicles.dto.VehicleDtos.*;
import com.taller360.vehicles.entity.Vehicle;
import com.taller360.vehicles.entity.VehicleDocument;
import com.taller360.vehicles.entity.VehiclePhoto;
import com.taller360.vehicles.entity.VehicleTimelineEvent;
import com.taller360.vehicles.entity.VehicleWarranty;
import com.taller360.vehicles.repository.VehicleDocumentRepository;
import com.taller360.vehicles.repository.VehiclePhotoRepository;
import com.taller360.vehicles.repository.VehicleRepository;
import com.taller360.vehicles.repository.VehicleTimelineRepository;
import com.taller360.vehicles.repository.VehicleWarrantyRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VehicleService {
  private final VehicleRepository vehicles;
  private final VehicleDocumentRepository documents;
  private final VehiclePhotoRepository photos;
  private final VehicleWarrantyRepository warranties;
  private final VehicleTimelineRepository timeline;
  private final ClientRepository clients;
  private final CompanyRepository companies;
  private final BranchRepository branches;
  private final TenantGuard tenant;
  private final AuditPort audit;
  private final ObjectMapper objectMapper;

  public VehicleService(VehicleRepository vehicles, VehicleDocumentRepository documents, VehiclePhotoRepository photos,
      VehicleWarrantyRepository warranties, VehicleTimelineRepository timeline, ClientRepository clients,
      CompanyRepository companies, BranchRepository branches, TenantGuard tenant, AuditPort audit,
      ObjectMapper objectMapper) {
    this.vehicles = vehicles;
    this.documents = documents;
    this.photos = photos;
    this.warranties = warranties;
    this.timeline = timeline;
    this.clients = clients;
    this.companies = companies;
    this.branches = branches;
    this.tenant = tenant;
    this.audit = audit;
    this.objectMapper = objectMapper;
  }

  public List<VehicleResponse> search(String q) {
    return vehicles.search(tenant.companyId(), q == null ? "" : q.trim()).stream().map(this::toVehicle).toList();
  }

  public Vehicle360Response detail(UUID id) {
    Vehicle vehicle = vehicleInTenant(id);
    return new Vehicle360Response(
        toVehicle(vehicle),
        documents.findByVehiculo_IdAndEmpresa_IdOrderByFechaCreacionDesc(id, tenant.companyId()).stream().map(this::toDocument).toList(),
        photos.findByVehiculo_IdAndEmpresa_IdOrderByPrincipalDescFechaCreacionDesc(id, tenant.companyId()).stream().map(this::toPhoto).toList(),
        warranties.findByVehiculo_IdAndEmpresa_IdOrderByFechaCreacionDesc(id, tenant.companyId()).stream().map(this::toWarranty).toList(),
        timeline.findByVehiculo_IdAndEmpresa_IdOrderByFechaHoraDesc(id, tenant.companyId()).stream().map(this::toTimeline).toList());
  }

  @Transactional
  public VehicleResponse create(VehicleRequest request) {
    Vehicle vehicle = new Vehicle();
    vehicle.id = UUID.randomUUID();
    vehicle.empresa = companies.findById(tenant.companyId()).orElseThrow();
    applyVehicle(vehicle, request);
    vehicle.fechaCreacion = Instant.now();
    vehicle.fechaModificacion = Instant.now();
    Vehicle saved = vehicles.save(vehicle);
    addTimeline(saved, "VEHICULOS", "Vehiculo creado", "Registro inicial", "ACTIVO");
    audit.record("VEHICULOS", "CREAR", "vehiculos", saved.id, null, json(toVehicle(saved)));
    return toVehicle(saved);
  }

  @Transactional
  public VehicleResponse update(UUID id, VehicleRequest request) {
    Vehicle vehicle = vehicleInTenant(id);
    String before = json(toVehicle(vehicle));
    applyVehicle(vehicle, request);
    vehicle.fechaModificacion = Instant.now();
    Vehicle saved = vehicles.save(vehicle);
    addTimeline(saved, "VEHICULOS", "Vehiculo actualizado", "Datos tecnicos actualizados", saved.estado);
    audit.record("VEHICULOS", "EDITAR", "vehiculos", saved.id, before, json(toVehicle(saved)));
    return toVehicle(saved);
  }

  @Transactional
  public void deactivate(UUID id) {
    Vehicle vehicle = vehicleInTenant(id);
    String before = json(toVehicle(vehicle));
    vehicle.estado = "INACTIVO";
    vehicle.fechaModificacion = Instant.now();
    vehicles.save(vehicle);
    addTimeline(vehicle, "VEHICULOS", "Vehiculo desactivado", null, "INACTIVO");
    audit.record("VEHICULOS", "ELIMINAR", "vehiculos", id, before, "{\"estado\":\"INACTIVO\"}");
  }

  @Transactional
  public VehicleDocumentResponse addDocument(UUID vehicleId, VehicleDocumentRequest request) {
    Vehicle vehicle = vehicleInTenant(vehicleId);
    VehicleDocument document = new VehicleDocument();
    document.id = UUID.randomUUID();
    document.empresa = vehicle.empresa;
    document.vehiculo = vehicle;
    document.tipo = required(request.tipo(), "tipo");
    document.nombreArchivo = required(request.nombreArchivo(), "nombreArchivo");
    document.url = required(request.url(), "url");
    document.venceEn = request.venceEn();
    document.estado = defaultValue(request.estado(), "ACTIVO");
    document.notas = request.notas();
    document.fechaCreacion = Instant.now();
    VehicleDocument saved = documents.save(document);
    addTimeline(vehicle, "DOCUMENTOS", "Documento agregado", saved.tipo, saved.estado);
    audit.record("VEHICULOS", "EDITAR", "vehiculo_documentos", saved.id, null, json(toDocument(saved)));
    return toDocument(saved);
  }

  @Transactional
  public VehiclePhotoResponse addPhoto(UUID vehicleId, VehiclePhotoRequest request) {
    Vehicle vehicle = vehicleInTenant(vehicleId);
    VehiclePhoto photo = new VehiclePhoto();
    photo.id = UUID.randomUUID();
    photo.empresa = vehicle.empresa;
    photo.vehiculo = vehicle;
    photo.url = required(request.url(), "url");
    photo.descripcion = request.descripcion();
    photo.principal = request.principal();
    photo.fechaCreacion = Instant.now();
    VehiclePhoto saved = photos.save(photo);
    addTimeline(vehicle, "FOTOS", "Foto agregada", saved.descripcion, "REGISTRADA");
    audit.record("VEHICULOS", "EDITAR", "vehiculo_fotos", saved.id, null, json(toPhoto(saved)));
    return toPhoto(saved);
  }

  @Transactional
  public VehicleWarrantyResponse addWarranty(UUID vehicleId, VehicleWarrantyRequest request) {
    Vehicle vehicle = vehicleInTenant(vehicleId);
    VehicleWarranty warranty = new VehicleWarranty();
    warranty.id = UUID.randomUUID();
    warranty.empresa = vehicle.empresa;
    warranty.vehiculo = vehicle;
    warranty.tipo = required(request.tipo(), "tipo");
    warranty.descripcion = request.descripcion();
    warranty.proveedor = request.proveedor();
    warranty.iniciaEn = request.iniciaEn();
    warranty.venceEn = request.venceEn();
    warranty.kilometrajeLimite = request.kilometrajeLimite();
    warranty.estado = defaultValue(request.estado(), "ACTIVA");
    warranty.fechaCreacion = Instant.now();
    VehicleWarranty saved = warranties.save(warranty);
    addTimeline(vehicle, "GARANTIAS", "Garantia agregada", saved.tipo, saved.estado);
    audit.record("VEHICULOS", "EDITAR", "vehiculo_garantias", saved.id, null, json(toWarranty(saved)));
    return toWarranty(saved);
  }

  private void applyVehicle(Vehicle vehicle, VehicleRequest request) {
    Client client = clients.findByIdAndEmpresa_Id(request.clienteId(), tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Cliente no pertenece a la empresa actual"));
    vehicle.cliente = client;
    vehicle.sucursal = request.sucursalId() == null ? null : branchInTenant(request.sucursalId());
    vehicle.placa = required(request.placa(), "placa").toUpperCase().replace(" ", "");
    vehicle.vin = request.vin() == null || request.vin().isBlank() ? null : request.vin().trim().toUpperCase();
    vehicle.marca = required(request.marca(), "marca");
    vehicle.modelo = required(request.modelo(), "modelo");
    vehicle.version = request.version();
    vehicle.anio = request.anio();
    vehicle.motor = request.motor();
    vehicle.combustible = request.combustible();
    vehicle.transmision = request.transmision();
    vehicle.color = request.color();
    if (request.kilometraje() < 0) {
      throw new IllegalArgumentException("El kilometraje no puede ser negativo");
    }
    vehicle.kilometraje = request.kilometraje();
    vehicle.estado = defaultValue(request.estado(), "ACTIVO");
    vehicle.notas = request.notas();
  }

  private Vehicle vehicleInTenant(UUID id) {
    return vehicles.findByIdAndEmpresa_Id(id, tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Vehiculo no pertenece a la empresa actual"));
  }

  private Branch branchInTenant(UUID id) {
    tenant.assertBranchAllowed(id);
    return branches.findByIdAndEmpresaId(id, tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Sucursal no pertenece a la empresa actual"));
  }

  private void addTimeline(Vehicle vehicle, String module, String title, String description, String status) {
    VehicleTimelineEvent event = new VehicleTimelineEvent();
    event.id = UUID.randomUUID();
    event.empresa = vehicle.empresa;
    event.vehiculo = vehicle;
    event.modulo = module;
    event.titulo = title;
    event.descripcion = description;
    event.estado = status;
    event.fechaHora = Instant.now();
    timeline.save(event);
  }

  private VehicleResponse toVehicle(Vehicle vehicle) {
    return new VehicleResponse(vehicle.id, vehicle.empresa.id, vehicle.cliente.id, vehicle.cliente.nombre,
        vehicle.sucursal == null ? null : vehicle.sucursal.id, vehicle.placa, vehicle.vin, vehicle.marca,
        vehicle.modelo, vehicle.version, vehicle.anio, vehicle.motor, vehicle.combustible, vehicle.transmision,
        vehicle.color, vehicle.kilometraje, vehicle.estado, vehicle.notas, vehicle.fechaCreacion, vehicle.fechaModificacion);
  }

  private VehicleDocumentResponse toDocument(VehicleDocument document) {
    return new VehicleDocumentResponse(document.id, document.tipo, document.nombreArchivo, document.url,
        document.venceEn, document.estado, document.notas, document.fechaCreacion);
  }

  private VehiclePhotoResponse toPhoto(VehiclePhoto photo) {
    return new VehiclePhotoResponse(photo.id, photo.url, photo.descripcion, photo.principal, photo.fechaCreacion);
  }

  private VehicleWarrantyResponse toWarranty(VehicleWarranty warranty) {
    return new VehicleWarrantyResponse(warranty.id, warranty.tipo, warranty.descripcion, warranty.proveedor,
        warranty.iniciaEn, warranty.venceEn, warranty.kilometrajeLimite, warranty.estado, warranty.fechaCreacion);
  }

  private VehicleTimelineResponse toTimeline(VehicleTimelineEvent event) {
    return new VehicleTimelineResponse(event.id, event.modulo, event.titulo, event.descripcion, event.referenciaId,
        event.estado, event.fechaHora);
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

  private String defaultValue(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value.trim();
  }
}
