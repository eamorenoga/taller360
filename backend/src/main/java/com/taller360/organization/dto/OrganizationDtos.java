package com.taller360.organization.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class OrganizationDtos {
  public record CompanyResponse(UUID id, String nombre, String nit, String estado, String moneda, String zonaHoraria,
      String email, String telefono, String direccion, Instant fechaCreacion, Instant fechaModificacion) {}

  public record CompanyRequest(String nombre, String nit, String estado, String moneda, String zonaHoraria,
      String email, String telefono, String direccion) {}

  public record BranchResponse(UUID id, UUID empresaId, String nombre, String codigo, String direccion, String telefono,
      String email, String moneda, String zonaHoraria, boolean permiteOperacion, String estado) {}

  public record BranchRequest(String nombre, String codigo, String direccion, String telefono, String email,
      String moneda, String zonaHoraria, Boolean permiteOperacion, String estado) {}

  public record TaxResponse(UUID id, String nombre, String codigo, BigDecimal porcentaje, boolean incluido, boolean activo) {}

  public record TaxRequest(String nombre, String codigo, BigDecimal porcentaje, boolean incluido, boolean activo) {}

  public record ConsecutiveResponse(UUID id, UUID sucursalId, String documento, String prefijo, long siguienteNumero,
      int longitud, boolean activo, String vistaPrevia) {}

  public record ConsecutiveRequest(UUID sucursalId, String documento, String prefijo, long siguienteNumero,
      int longitud, boolean activo) {}

  public record ParameterResponse(UUID id, UUID sucursalId, String clave, String valor, String tipo, String descripcion) {}

  public record ParameterRequest(UUID sucursalId, String clave, String valor, String tipo, String descripcion) {}
}
