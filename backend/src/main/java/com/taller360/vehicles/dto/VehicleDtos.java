package com.taller360.vehicles.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class VehicleDtos {
  public record VehicleRequest(UUID clienteId, UUID sucursalId, String placa, String vin, String marca, String modelo,
      String version, Integer anio, String motor, String combustible, String transmision, String color,
      long kilometraje, String estado, String notas) {}

  public record VehicleResponse(UUID id, UUID empresaId, UUID clienteId, String cliente, UUID sucursalId, String placa,
      String vin, String marca, String modelo, String version, Integer anio, String motor, String combustible,
      String transmision, String color, long kilometraje, String estado, String notas, Instant fechaCreacion,
      Instant fechaModificacion) {}

  public record VehicleDocumentRequest(String tipo, String nombreArchivo, String url, LocalDate venceEn,
      String estado, String notas) {}

  public record VehicleDocumentResponse(UUID id, String tipo, String nombreArchivo, String url, LocalDate venceEn,
      String estado, String notas, Instant fechaCreacion) {}

  public record VehiclePhotoRequest(String url, String descripcion, boolean principal) {}

  public record VehiclePhotoResponse(UUID id, String url, String descripcion, boolean principal, Instant fechaCreacion) {}

  public record VehicleWarrantyRequest(String tipo, String descripcion, String proveedor, LocalDate iniciaEn,
      LocalDate venceEn, Long kilometrajeLimite, String estado) {}

  public record VehicleWarrantyResponse(UUID id, String tipo, String descripcion, String proveedor, LocalDate iniciaEn,
      LocalDate venceEn, Long kilometrajeLimite, String estado, Instant fechaCreacion) {}

  public record VehicleTimelineResponse(UUID id, String modulo, String titulo, String descripcion, UUID referenciaId,
      String estado, Instant fechaHora) {}

  public record Vehicle360Response(VehicleResponse vehiculo, List<VehicleDocumentResponse> documentos,
      List<VehiclePhotoResponse> fotos, List<VehicleWarrantyResponse> garantias, List<VehicleTimelineResponse> timeline) {}
}
