package com.taller360.quotations.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class QuotationDtos {
  public record QuotationItemRequest(String tipo, String codigo, String descripcion, BigDecimal cantidad,
      BigDecimal valorUnitario, BigDecimal impuestoPorcentaje, BigDecimal descuentoPorcentaje) {}

  public record QuotationRequest(UUID sucursalId, UUID ordenTrabajoId, UUID clienteId, UUID vehiculoId,
      LocalDate venceEn, String moneda, List<QuotationItemRequest> items) {}

  public record QuotationResponse(UUID id, UUID empresaId, UUID sucursalId, UUID ordenTrabajoId, UUID clienteId,
      String cliente, UUID vehiculoId, String placa, int versionActual, String estado, LocalDate venceEn,
      String moneda, BigDecimal subtotal, BigDecimal descuentoTotal, BigDecimal impuestoTotal, BigDecimal total,
      String publicToken, String pdfUrl, UUID trabajoGeneradoId, Instant fechaCreacion, Instant fechaModificacion) {}

  public record QuotationItemResponse(UUID id, int version, String tipo, String codigo, String descripcion,
      BigDecimal cantidad, BigDecimal valorUnitario, BigDecimal impuestoPorcentaje, BigDecimal descuentoPorcentaje,
      BigDecimal subtotal, BigDecimal descuento, BigDecimal impuesto, BigDecimal total, String estadoAprobacion,
      Instant fechaCreacion) {}

  public record QuotationDetailResponse(QuotationResponse cotizacion, List<QuotationItemResponse> items,
      List<QuotationVersionResponse> versiones, List<QuotationApprovalResponse> aprobaciones, QuotationPdfResponse pdf) {}

  public record QuotationVersionResponse(UUID id, int version, String snapshotJson, BigDecimal subtotal,
      BigDecimal descuentoTotal, BigDecimal impuestoTotal, BigDecimal total, Instant fechaCreacion) {}

  public record ApprovalRequest(String accion, String aprobadorNombre, String aprobadorEmail, List<UUID> itemIds,
      String evidenciaJson) {}

  public record QuotationApprovalResponse(UUID id, UUID itemId, String accion, String aprobadorNombre,
      String aprobadorEmail, String evidenciaJson, Instant fechaCreacion) {}

  public record QuotationPdfResponse(UUID id, int version, String url, String contenidoBase64, String hashContenido,
      Instant fechaCreacion) {}

  public record GenerateWorkResponse(UUID cotizacionId, UUID trabajoGeneradoId, List<UUID> itemIds, String message) {}
}
