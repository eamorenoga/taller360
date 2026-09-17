package com.taller360.reception.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ReceptionDtos {
  public record AccessoryItem(String nombre, boolean presente, String observacion) {}

  public record ChecklistItem(String codigo, String etiqueta, boolean ok, String observacion) {}

  public record DamageItem(String zona, int x, int y, String severidad, String descripcion) {}

  public record ReceptionRequest(UUID sucursalId, UUID citaId, UUID clienteId, UUID vehiculoId, UUID asesorId,
      long kilometraje, int combustiblePorcentaje, String motivo, List<AccessoryItem> accesorios,
      List<ChecklistItem> checklist, List<DamageItem> danos, String observaciones) {}

  public record ReceptionResponse(UUID id, UUID empresaId, UUID sucursalId, UUID citaId, UUID clienteId,
      String cliente, UUID vehiculoId, String placa, UUID asesorId, long kilometraje, int combustiblePorcentaje,
      String motivo, List<AccessoryItem> accesorios, List<ChecklistItem> checklist, List<DamageItem> danos,
      String observaciones, String estado, Integer versionFirmada, Instant firmadoEn, String firmadoPor,
      String firmaUrl, String pdfUrl, UUID ordenTrabajoId, Instant fechaCreacion, Instant fechaModificacion) {}

  public record ReceptionDetailResponse(ReceptionResponse recepcion, List<ReceptionPhotoResponse> fotos,
      List<ReceptionSignatureResponse> firmas, ReceptionPdfResponse pdf) {}

  public record ReceptionPhotoRequest(String url, String nombreArchivo, String tipo, long tamanoOriginalBytes,
      long tamanoComprimidoBytes, Integer ancho, Integer alto, String metadatosJson) {}

  public record ReceptionPhotoResponse(UUID id, String url, String nombreArchivo, String tipo,
      long tamanoOriginalBytes, long tamanoComprimidoBytes, Integer ancho, Integer alto, String metadatosJson,
      Instant fechaCreacion) {}

  public record ReceptionSignatureRequest(String firmante, String firmaUrl) {}

  public record ReceptionSignatureResponse(UUID id, String firmante, String firmaUrl, String hashContenido,
      int version, Instant fechaCreacion) {}

  public record ReceptionPdfResponse(UUID id, int version, String url, String contenidoBase64, String hashContenido,
      Instant fechaCreacion) {}

  public record WorkOrderConversionResponse(UUID recepcionId, UUID ordenTrabajoId, String estado, String message) {}
}
