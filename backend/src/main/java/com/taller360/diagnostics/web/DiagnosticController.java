package com.taller360.diagnostics.web;

import com.taller360.diagnostics.dto.DiagnosticDtos.*;
import com.taller360.diagnostics.service.DiagnosticService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/diagnostics")
public class DiagnosticController {
  private final DiagnosticService diagnostics;

  public DiagnosticController(DiagnosticService diagnostics) {
    this.diagnostics = diagnostics;
  }

  @GetMapping
  @PreAuthorize("@perm.has(authentication, 'DIAGNOSTICOS:VER')")
  List<DiagnosticResponse> search(@RequestParam(required = false) UUID branchId,
      @RequestParam(required = false) String q) {
    return diagnostics.search(branchId, q);
  }

  @GetMapping("/{id}")
  @PreAuthorize("@perm.has(authentication, 'DIAGNOSTICOS:VER')")
  DiagnosticDetailResponse detail(@PathVariable UUID id) {
    return diagnostics.detail(id);
  }

  @PostMapping
  @PreAuthorize("@perm.has(authentication, 'DIAGNOSTICOS:CREAR')")
  DiagnosticResponse create(@RequestBody DiagnosticRequest request) {
    return diagnostics.create(request);
  }

  @PutMapping("/{id}")
  @PreAuthorize("@perm.has(authentication, 'DIAGNOSTICOS:EDITAR')")
  DiagnosticResponse update(@PathVariable UUID id, @RequestBody DiagnosticRequest request) {
    return diagnostics.update(id, request);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("@perm.has(authentication, 'DIAGNOSTICOS:ANULAR')")
  ResponseEntity<Void> annul(@PathVariable UUID id) {
    diagnostics.annul(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/evidences")
  @PreAuthorize("@perm.has(authentication, 'DIAGNOSTICOS:EDITAR')")
  EvidenceResponse addEvidence(@PathVariable UUID id, @RequestBody EvidenceRequest request) {
    return diagnostics.addEvidence(id, request);
  }

  @PostMapping("/{id}/tasks")
  @PreAuthorize("@perm.has(authentication, 'DIAGNOSTICOS:EDITAR')")
  TaskResponse addTask(@PathVariable UUID id, @RequestBody TaskRequest request) {
    return diagnostics.addTask(id, request);
  }

  @PostMapping("/{id}/parts")
  @PreAuthorize("@perm.has(authentication, 'DIAGNOSTICOS:EDITAR')")
  PartResponse addPart(@PathVariable UUID id, @RequestBody PartRequest request) {
    return diagnostics.addPart(id, request);
  }

  @PostMapping("/{id}/ai-assist")
  @PreAuthorize("@perm.has(authentication, 'DIAGNOSTICOS:APROBAR')")
  AiSuggestionResponse assist(@PathVariable UUID id, @RequestBody AiAssistRequest request) {
    return diagnostics.assist(id, request);
  }

  @PostMapping("/{id}/ai-confirm")
  @PreAuthorize("@perm.has(authentication, 'DIAGNOSTICOS:APROBAR')")
  DiagnosticResponse confirmAi(@PathVariable UUID id) {
    return diagnostics.confirmAi(id);
  }
}
