package com.taller360.diagnostics.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class DiagnosticDtos {
  public record SymptomItem(String descripcion, String severidad, String condicion) {}

  public record InspectionItem(String sistema, String resultado, String observacion) {}

  public record DtcItem(String codigo, String descripcion, String modulo, String estado) {}

  public record TestItem(String nombre, String resultado, String unidad, String observacion) {}

  public record FindingItem(String descripcion, String evidencia, String impacto) {}

  public record DiagnosticRequest(UUID sucursalId, UUID ordenTrabajoId, UUID recepcionId, UUID clienteId,
      UUID vehiculoId, UUID tecnicoId, List<SymptomItem> sintomas, List<InspectionItem> inspecciones,
      List<DtcItem> dtc, List<TestItem> pruebas, List<FindingItem> hallazgos, String causaProbable,
      String solucionRecomendada, String prioridad, String estado, int tiempoEstimadoMinutos) {}

  public record DiagnosticResponse(UUID id, UUID empresaId, UUID sucursalId, UUID ordenTrabajoId, UUID recepcionId,
      UUID clienteId, String cliente, UUID vehiculoId, String placa, UUID tecnicoId, List<SymptomItem> sintomas,
      List<InspectionItem> inspecciones, List<DtcItem> dtc, List<TestItem> pruebas, List<FindingItem> hallazgos,
      String causaProbable, String solucionRecomendada, String prioridad, String estado, int tiempoEstimadoMinutos,
      AiSuggestionResponse iaSugerencia, UUID iaConfirmadaPor, Instant iaConfirmadaEn, Instant fechaCreacion,
      Instant fechaModificacion) {}

  public record DiagnosticDetailResponse(DiagnosticResponse diagnostico, List<EvidenceResponse> evidencias,
      List<TaskResponse> tareas, List<PartResponse> repuestos) {}

  public record EvidenceRequest(String tipo, String url, String nombreArchivo, String mimeType, long tamanoBytes,
      String metadatosJson) {}

  public record EvidenceResponse(UUID id, String tipo, String url, String nombreArchivo, String mimeType,
      long tamanoBytes, String metadatosJson, Instant fechaCreacion) {}

  public record TaskRequest(String descripcion, String prioridad, String estado, int tiempoEstimadoMinutos) {}

  public record TaskResponse(UUID id, String descripcion, String prioridad, String estado, int tiempoEstimadoMinutos,
      Instant fechaCreacion) {}

  public record PartRequest(String codigo, String nombre, BigDecimal cantidad, boolean requerido, String notas) {}

  public record PartResponse(UUID id, String codigo, String nombre, BigDecimal cantidad, boolean requerido,
      String notas, Instant fechaCreacion) {}

  public record AiAssistRequest(String pregunta, boolean incluirDtc, boolean incluirHallazgos) {}

  public record AiSuggestionResponse(List<String> causasProbables, List<String> pruebasSugeridas,
      List<String> recomendaciones, List<String> repuestosPosibles, String prioridadSugerida,
      boolean requiereConfirmacionTecnica, String aviso) {}
}
