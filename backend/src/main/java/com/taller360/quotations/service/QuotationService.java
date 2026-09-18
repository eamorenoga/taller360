package com.taller360.quotations.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller360.audit.AuditPort;
import com.taller360.clients.entity.Client;
import com.taller360.clients.repository.ClientRepository;
import com.taller360.common.tenant.TenantGuard;
import com.taller360.organization.entity.Branch;
import com.taller360.organization.entity.Company;
import com.taller360.organization.repository.BranchRepository;
import com.taller360.organization.repository.CompanyRepository;
import com.taller360.quotations.dto.QuotationDtos.*;
import com.taller360.quotations.entity.Quotation;
import com.taller360.quotations.entity.QuotationApproval;
import com.taller360.quotations.entity.QuotationItem;
import com.taller360.quotations.entity.QuotationPdf;
import com.taller360.quotations.entity.QuotationVersion;
import com.taller360.quotations.port.QuoteWorkOrderPort;
import com.taller360.quotations.repository.QuotationApprovalRepository;
import com.taller360.quotations.repository.QuotationItemRepository;
import com.taller360.quotations.repository.QuotationPdfRepository;
import com.taller360.quotations.repository.QuotationRepository;
import com.taller360.quotations.repository.QuotationVersionRepository;
import com.taller360.reception.service.SimplePdfGenerator;
import com.taller360.vehicles.entity.Vehicle;
import com.taller360.vehicles.repository.VehicleRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuotationService {
  private static final Set<String> ITEM_TYPES = Set.of("MANO_OBRA", "SERVICIO", "REPUESTO");
  private static final Set<String> ACTIONS = Set.of("APROBAR", "RECHAZAR");
  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

  private final QuotationRepository quotations;
  private final QuotationItemRepository items;
  private final QuotationVersionRepository versions;
  private final QuotationApprovalRepository approvals;
  private final QuotationPdfRepository pdfs;
  private final ClientRepository clients;
  private final VehicleRepository vehicles;
  private final CompanyRepository companies;
  private final BranchRepository branches;
  private final QuoteWorkOrderPort workOrders;
  private final TenantGuard tenant;
  private final AuditPort audit;
  private final ObjectMapper objectMapper;
  private final SimplePdfGenerator pdfGenerator = new SimplePdfGenerator();

  public QuotationService(QuotationRepository quotations, QuotationItemRepository items,
      QuotationVersionRepository versions, QuotationApprovalRepository approvals, QuotationPdfRepository pdfs,
      ClientRepository clients, VehicleRepository vehicles, CompanyRepository companies, BranchRepository branches,
      QuoteWorkOrderPort workOrders, TenantGuard tenant, AuditPort audit, ObjectMapper objectMapper) {
    this.quotations = quotations;
    this.items = items;
    this.versions = versions;
    this.approvals = approvals;
    this.pdfs = pdfs;
    this.clients = clients;
    this.vehicles = vehicles;
    this.companies = companies;
    this.branches = branches;
    this.workOrders = workOrders;
    this.tenant = tenant;
    this.audit = audit;
    this.objectMapper = objectMapper;
  }

  public List<QuotationResponse> search(UUID sucursalId, String q) {
    if (sucursalId != null) {
      tenant.assertBranchAllowed(sucursalId);
    }
    return quotations.search(tenant.companyId(), sucursalId, q == null ? "" : q.trim()).stream()
        .map(this::toQuotation)
        .toList();
  }

  public QuotationDetailResponse detail(UUID id) {
    Quotation quotation = quotationInTenant(id);
    return detailOf(quotation);
  }

  public QuotationDetailResponse publicDetail(String token) {
    Quotation quotation = quotations.findByPublicToken(token).orElseThrow(() -> new AccessDeniedException("Cotizacion no disponible"));
    return detailOf(quotation);
  }

  @Transactional
  public QuotationResponse create(QuotationRequest request) {
    Company company = companies.findById(tenant.companyId()).orElseThrow();
    Quotation quotation = new Quotation();
    quotation.id = UUID.randomUUID();
    quotation.empresa = company;
    quotation.versionActual = 1;
    quotation.estado = "BORRADOR";
    quotation.publicToken = UUID.randomUUID().toString().replace("-", "");
    applyHeader(quotation, request);
    quotation.fechaCreacion = Instant.now();
    quotation.fechaModificacion = Instant.now();
    Quotation saved = quotations.save(quotation);
    replaceItems(saved, request.items());
    recalculate(saved);
    quotations.save(saved);
    saveVersion(saved);
    generatePdf(saved);
    quotations.save(saved);
    audit.record("COTIZACIONES", "CREAR", "cotizaciones", saved.id, null, json(toQuotation(saved)));
    return toQuotation(saved);
  }

  @Transactional
  public QuotationResponse update(UUID id, QuotationRequest request) {
    Quotation quotation = quotationInTenant(id);
    String before = json(toQuotation(quotation));
    quotation.versionActual += 1;
    quotation.estado = "BORRADOR";
    applyHeader(quotation, request);
    quotation.fechaModificacion = Instant.now();
    items.deleteByCotizacion_IdAndEmpresa_Id(quotation.id, tenant.companyId());
    replaceItems(quotation, request.items());
    recalculate(quotation);
    quotations.save(quotation);
    saveVersion(quotation);
    generatePdf(quotation);
    quotations.save(quotation);
    audit.record("COTIZACIONES", "EDITAR", "cotizaciones", quotation.id, before, json(toQuotation(quotation)));
    return toQuotation(quotation);
  }

  @Transactional
  public QuotationResponse send(UUID id) {
    Quotation quotation = quotationInTenant(id);
    String before = json(toQuotation(quotation));
    quotation.estado = "ENVIADA";
    quotation.fechaModificacion = Instant.now();
    quotations.save(quotation);
    audit.record("COTIZACIONES", "EDITAR", "cotizaciones", id, before, json(toQuotation(quotation)));
    return toQuotation(quotation);
  }

  @Transactional
  public QuotationDetailResponse approve(UUID id, ApprovalRequest request) {
    Quotation quotation = quotationInTenant(id);
    applyApproval(quotation, request, true);
    return detailOf(quotation);
  }

  @Transactional
  public QuotationDetailResponse publicApprove(String token, ApprovalRequest request) {
    Quotation quotation = quotations.findByPublicToken(token).orElseThrow(() -> new AccessDeniedException("Cotizacion no disponible"));
    applyApproval(quotation, request, false);
    return detailOf(quotation);
  }

  @Transactional
  public GenerateWorkResponse generateWork(UUID id) {
    Quotation quotation = quotationInTenant(id);
    List<UUID> approved = items.findByCotizacion_IdAndEmpresa_IdOrderByFechaCreacionAsc(id, tenant.companyId()).stream()
        .filter(item -> "APROBADO".equals(item.estadoAprobacion))
        .map(item -> item.id)
        .toList();
    if (approved.isEmpty()) {
      throw new IllegalArgumentException("No hay items aprobados para generar trabajo");
    }
    if (quotation.trabajoGeneradoId == null) {
      quotation.trabajoGeneradoId = workOrders.generateApprovedWork(id, approved);
      quotation.estado = "TRABAJO_GENERADO";
      quotation.fechaModificacion = Instant.now();
      quotations.save(quotation);
      audit.record("COTIZACIONES", "APROBAR", "cotizaciones", id, null, json(toQuotation(quotation)));
    }
    return new GenerateWorkResponse(id, quotation.trabajoGeneradoId, approved,
        "Trabajo reservado solo con items aprobados por puerto M10 pendiente");
  }

  @Transactional
  public void annul(UUID id) {
    Quotation quotation = quotationInTenant(id);
    String before = json(toQuotation(quotation));
    quotation.estado = "ANULADA";
    quotation.fechaModificacion = Instant.now();
    quotations.save(quotation);
    audit.record("COTIZACIONES", "ANULAR", "cotizaciones", id, before, json(toQuotation(quotation)));
  }

  private void applyHeader(Quotation quotation, QuotationRequest request) {
    Branch branch = branchInTenant(request.sucursalId());
    Client client = clients.findByIdAndEmpresa_Id(request.clienteId(), tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Cliente no pertenece a la empresa actual"));
    Vehicle vehicle = vehicles.findByIdAndEmpresa_Id(request.vehiculoId(), tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Vehiculo no pertenece a la empresa actual"));
    if (!vehicle.cliente.id.equals(client.id)) {
      throw new IllegalArgumentException("El vehiculo no pertenece al cliente seleccionado");
    }
    if (request.ordenTrabajoId() == null) {
      throw new IllegalArgumentException("El campo ordenTrabajoId es obligatorio");
    }
    quotation.sucursal = branch;
    quotation.ordenTrabajoId = request.ordenTrabajoId();
    quotation.cliente = client;
    quotation.vehiculo = vehicle;
    quotation.venceEn = request.venceEn();
    quotation.moneda = request.moneda() == null || request.moneda().isBlank() ? "COP" : request.moneda().trim().toUpperCase();
  }

  private void replaceItems(Quotation quotation, List<QuotationItemRequest> requests) {
    if (requests == null || requests.isEmpty()) {
      throw new IllegalArgumentException("La cotizacion requiere al menos un item");
    }
    requests.forEach(request -> items.save(toItem(quotation, request)));
  }

  private QuotationItem toItem(Quotation quotation, QuotationItemRequest request) {
    QuotationItem item = new QuotationItem();
    item.id = UUID.randomUUID();
    item.empresa = quotation.empresa;
    item.cotizacion = quotation;
    item.version = quotation.versionActual;
    item.tipo = itemType(request.tipo());
    item.codigo = request.codigo();
    item.descripcion = required(request.descripcion(), "descripcion");
    item.cantidad = positive(request.cantidad(), "cantidad");
    item.valorUnitario = nonNegative(request.valorUnitario(), "valorUnitario");
    item.impuestoPorcentaje = nonNegative(request.impuestoPorcentaje(), "impuestoPorcentaje");
    item.descuentoPorcentaje = nonNegative(request.descuentoPorcentaje(), "descuentoPorcentaje");
    BigDecimal subtotal = money(item.cantidad.multiply(item.valorUnitario));
    BigDecimal discount = money(subtotal.multiply(item.descuentoPorcentaje).divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP));
    BigDecimal taxable = subtotal.subtract(discount);
    BigDecimal tax = money(taxable.multiply(item.impuestoPorcentaje).divide(ONE_HUNDRED, 6, RoundingMode.HALF_UP));
    item.subtotal = subtotal;
    item.descuento = discount;
    item.impuesto = tax;
    item.total = money(taxable.add(tax));
    item.estadoAprobacion = "PENDIENTE";
    item.fechaCreacion = Instant.now();
    return item;
  }

  private void recalculate(Quotation quotation) {
    List<QuotationItem> rows = items.findByCotizacion_IdAndEmpresa_IdOrderByFechaCreacionAsc(quotation.id, tenant.companyId());
    quotation.subtotal = rows.stream().map(item -> item.subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    quotation.descuentoTotal = rows.stream().map(item -> item.descuento).reduce(BigDecimal.ZERO, BigDecimal::add);
    quotation.impuestoTotal = rows.stream().map(item -> item.impuesto).reduce(BigDecimal.ZERO, BigDecimal::add);
    quotation.total = rows.stream().map(item -> item.total).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private void applyApproval(Quotation quotation, ApprovalRequest request, boolean recordAudit) {
    if (quotation.venceEn != null && quotation.venceEn.isBefore(LocalDate.now())) {
      throw new IllegalArgumentException("La cotizacion esta vencida y no admite aprobaciones");
    }
    String action = action(request.accion());
    List<QuotationItem> rows = items.findByCotizacion_IdAndEmpresa_IdOrderByFechaCreacionAsc(quotation.id, quotation.empresa.id);
    List<UUID> targetIds = request.itemIds() == null || request.itemIds().isEmpty()
        ? rows.stream().map(item -> item.id).toList()
        : request.itemIds();
    for (UUID itemId : targetIds) {
      QuotationItem item = items.findByIdAndCotizacion_IdAndEmpresa_Id(itemId, quotation.id, quotation.empresa.id)
          .orElseThrow(() -> new AccessDeniedException("Item no pertenece a la cotizacion"));
      item.estadoAprobacion = "APROBAR".equals(action) ? "APROBADO" : "RECHAZADO";
      items.save(item);
      saveApproval(quotation, item, action, request);
    }
    List<QuotationItem> updated = items.findByCotizacion_IdAndEmpresa_IdOrderByFechaCreacionAsc(quotation.id, quotation.empresa.id);
    long approved = updated.stream().filter(item -> "APROBADO".equals(item.estadoAprobacion)).count();
    long rejected = updated.stream().filter(item -> "RECHAZADO".equals(item.estadoAprobacion)).count();
    if (approved == updated.size()) {
      quotation.estado = "APROBADA";
    } else if (rejected == updated.size()) {
      quotation.estado = "RECHAZADA";
    } else if (approved > 0 || rejected > 0) {
      quotation.estado = "APROBADA_PARCIAL";
    }
    quotation.fechaModificacion = Instant.now();
    quotations.save(quotation);
    if (recordAudit) {
      audit.record("COTIZACIONES", "APROBAR", "cotizaciones", quotation.id, null, json(toQuotation(quotation)));
    }
  }

  private void saveApproval(Quotation quotation, QuotationItem item, String action, ApprovalRequest request) {
    QuotationApproval approval = new QuotationApproval();
    approval.id = UUID.randomUUID();
    approval.empresa = quotation.empresa;
    approval.cotizacion = quotation;
    approval.item = item;
    approval.accion = action;
    approval.aprobadorNombre = required(request.aprobadorNombre(), "aprobadorNombre");
    approval.aprobadorEmail = request.aprobadorEmail();
    approval.evidenciaJson = request.evidenciaJson() == null || request.evidenciaJson().isBlank() ? "{}" : request.evidenciaJson();
    approval.fechaCreacion = Instant.now();
    approvals.save(approval);
  }

  private void saveVersion(Quotation quotation) {
    QuotationVersion version = new QuotationVersion();
    version.id = UUID.randomUUID();
    version.empresa = quotation.empresa;
    version.cotizacion = quotation;
    version.version = quotation.versionActual;
    version.snapshotJson = json(detailOf(quotation));
    version.subtotal = quotation.subtotal;
    version.descuentoTotal = quotation.descuentoTotal;
    version.impuestoTotal = quotation.impuestoTotal;
    version.total = quotation.total;
    version.fechaCreacion = Instant.now();
    versions.save(version);
  }

  private void generatePdf(Quotation quotation) {
    List<String> lines = List.of(
        "Cotizacion: " + quotation.id,
        "Cliente: " + quotation.cliente.nombre,
        "Vehiculo: " + quotation.vehiculo.placa,
        "Version: " + quotation.versionActual,
        "Total: " + quotation.total + " " + quotation.moneda,
        "Enlace: /q/" + quotation.publicToken);
    byte[] pdfBytes = pdfGenerator.generate("Cotizacion Taller 360", lines);
    QuotationPdf pdf = new QuotationPdf();
    pdf.id = UUID.randomUUID();
    pdf.empresa = quotation.empresa;
    pdf.cotizacion = quotation;
    pdf.version = quotation.versionActual;
    pdf.url = "quotation://" + quotation.id + "/pdf/" + quotation.versionActual;
    pdf.contenidoBase64 = Base64.getEncoder().encodeToString(pdfBytes);
    pdf.hashContenido = sha256(new String(pdfBytes, StandardCharsets.UTF_8));
    pdf.fechaCreacion = Instant.now();
    pdfs.save(pdf);
    quotation.pdfUrl = pdf.url;
  }

  private QuotationDetailResponse detailOf(Quotation quotation) {
    return new QuotationDetailResponse(toQuotation(quotation),
        items.findByCotizacion_IdAndEmpresa_IdOrderByFechaCreacionAsc(quotation.id, quotation.empresa.id).stream().map(this::toItem).toList(),
        versions.findByCotizacion_IdAndEmpresa_IdOrderByVersionDesc(quotation.id, quotation.empresa.id).stream().map(this::toVersion).toList(),
        approvals.findByCotizacion_IdAndEmpresa_IdOrderByFechaCreacionDesc(quotation.id, quotation.empresa.id).stream().map(this::toApproval).toList(),
        pdfs.findFirstByCotizacion_IdAndEmpresa_IdOrderByVersionDesc(quotation.id, quotation.empresa.id).map(this::toPdf).orElse(null));
  }

  private Quotation quotationInTenant(UUID id) {
    return quotations.findByIdAndEmpresa_Id(id, tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Cotizacion no pertenece a la empresa actual"));
  }

  private Branch branchInTenant(UUID id) {
    if (id == null) {
      throw new IllegalArgumentException("El campo sucursalId es obligatorio");
    }
    tenant.assertBranchAllowed(id);
    return branches.findByIdAndEmpresaId(id, tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Sucursal no pertenece a la empresa actual"));
  }

  private QuotationResponse toQuotation(Quotation quotation) {
    return new QuotationResponse(quotation.id, quotation.empresa.id, quotation.sucursal.id, quotation.ordenTrabajoId,
        quotation.cliente.id, quotation.cliente.nombre, quotation.vehiculo.id, quotation.vehiculo.placa,
        quotation.versionActual, quotation.estado, quotation.venceEn, quotation.moneda, quotation.subtotal,
        quotation.descuentoTotal, quotation.impuestoTotal, quotation.total, quotation.publicToken, quotation.pdfUrl,
        quotation.trabajoGeneradoId, quotation.fechaCreacion, quotation.fechaModificacion);
  }

  private QuotationItemResponse toItem(QuotationItem item) {
    return new QuotationItemResponse(item.id, item.version, item.tipo, item.codigo, item.descripcion, item.cantidad,
        item.valorUnitario, item.impuestoPorcentaje, item.descuentoPorcentaje, item.subtotal, item.descuento,
        item.impuesto, item.total, item.estadoAprobacion, item.fechaCreacion);
  }

  private QuotationVersionResponse toVersion(QuotationVersion version) {
    return new QuotationVersionResponse(version.id, version.version, version.snapshotJson, version.subtotal,
        version.descuentoTotal, version.impuestoTotal, version.total, version.fechaCreacion);
  }

  private QuotationApprovalResponse toApproval(QuotationApproval approval) {
    return new QuotationApprovalResponse(approval.id, approval.item == null ? null : approval.item.id,
        approval.accion, approval.aprobadorNombre, approval.aprobadorEmail, approval.evidenciaJson,
        approval.fechaCreacion);
  }

  private QuotationPdfResponse toPdf(QuotationPdf pdf) {
    return new QuotationPdfResponse(pdf.id, pdf.version, pdf.url, pdf.contenidoBase64, pdf.hashContenido,
        pdf.fechaCreacion);
  }

  private String itemType(String value) {
    String normalized = required(value, "tipo").toUpperCase();
    if (!ITEM_TYPES.contains(normalized)) {
      throw new IllegalArgumentException("Tipo de item de cotizacion no valido");
    }
    return normalized;
  }

  private String action(String value) {
    String normalized = required(value, "accion").toUpperCase();
    if (!ACTIONS.contains(normalized)) {
      throw new IllegalArgumentException("Accion de aprobacion no valida");
    }
    return normalized;
  }

  private BigDecimal positive(BigDecimal value, String field) {
    BigDecimal result = value == null ? BigDecimal.ONE : value;
    if (result.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("El campo " + field + " debe ser mayor que cero");
    }
    return money(result);
  }

  private BigDecimal nonNegative(BigDecimal value, String field) {
    BigDecimal result = value == null ? BigDecimal.ZERO : value;
    if (result.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("El campo " + field + " no puede ser negativo");
    }
    return result;
  }

  private BigDecimal money(BigDecimal value) {
    return value.setScale(2, RoundingMode.HALF_UP);
  }

  private String required(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("El campo " + field + " es obligatorio");
    }
    return value.trim();
  }

  private String json(Object value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
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
