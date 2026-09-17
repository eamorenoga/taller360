package com.taller360.clients.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class ClientDtos {
  public record ClientRequest(UUID sucursalId, String tipoPersona, String tipoIdentificacion, String identificacion,
      String nombre, String razonSocial, String telefonoPrincipal, String telefonoSecundario, String whatsapp,
      String email, String direccion, String notas, String preferencias, String estado) {}

  public record ClientResponse(UUID id, UUID empresaId, UUID sucursalId, String tipoPersona, String tipoIdentificacion,
      String identificacion, String nombre, String razonSocial, String telefonoPrincipal, String telefonoSecundario,
      String whatsapp, String email, String direccion, String notas, String preferencias, String estado,
      Instant fechaCreacion, Instant fechaModificacion) {}

  public record ContactRequest(String nombre, String relacion, String telefono, String whatsapp, String email,
      boolean principal, String notas, String estado) {}

  public record ContactResponse(UUID id, String nombre, String relacion, String telefono, String whatsapp, String email,
      boolean principal, String notas, String estado) {}

  public record DocumentRequest(String tipo, String nombreArchivo, String url, String notas, String estado) {}

  public record DocumentResponse(UUID id, String tipo, String nombreArchivo, String url, String notas, String estado,
      Instant fechaCreacion) {}

  public record CommunicationRequest(String canal, String direccion, String asunto, String contenido, String estado) {}

  public record CommunicationResponse(UUID id, String canal, String direccion, String asunto, String contenido,
      String estado, Instant fechaHora) {}

  public record Client360Response(ClientResponse cliente, List<ContactResponse> contactos,
      List<DocumentResponse> documentos, List<CommunicationResponse> comunicaciones, List<RelatedItem> vehiculos,
      List<RelatedItem> citas, List<RelatedItem> ordenesTrabajo, List<RelatedItem> facturas, List<RelatedItem> pagos,
      PortfolioSummary cartera) {}

  public record RelatedItem(UUID id, String titulo, String estado, String referencia, Instant fecha) {}

  public record PortfolioSummary(String estado, long saldoPendiente, String moneda, List<RelatedItem> documentos) {}
}
