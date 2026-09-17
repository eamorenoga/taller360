package com.taller360.reception.web;

import com.taller360.reception.dto.ReceptionDtos.*;
import com.taller360.reception.service.ReceptionService;
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
@RequestMapping("/api/v1/receptions")
public class ReceptionController {
  private final ReceptionService receptions;

  public ReceptionController(ReceptionService receptions) {
    this.receptions = receptions;
  }

  @GetMapping
  @PreAuthorize("@perm.has(authentication, 'RECEPCION:VER')")
  List<ReceptionResponse> search(@RequestParam(required = false) UUID branchId,
      @RequestParam(required = false) String q) {
    return receptions.search(branchId, q);
  }

  @GetMapping("/{id}")
  @PreAuthorize("@perm.has(authentication, 'RECEPCION:VER')")
  ReceptionDetailResponse detail(@PathVariable UUID id) {
    return receptions.detail(id);
  }

  @PostMapping
  @PreAuthorize("@perm.has(authentication, 'RECEPCION:CREAR')")
  ReceptionResponse create(@RequestBody ReceptionRequest request) {
    return receptions.create(request);
  }

  @PutMapping("/{id}")
  @PreAuthorize("@perm.has(authentication, 'RECEPCION:EDITAR')")
  ReceptionResponse update(@PathVariable UUID id, @RequestBody ReceptionRequest request) {
    return receptions.update(id, request);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("@perm.has(authentication, 'RECEPCION:ANULAR')")
  ResponseEntity<Void> annul(@PathVariable UUID id) {
    receptions.annul(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/photos")
  @PreAuthorize("@perm.has(authentication, 'RECEPCION:EDITAR')")
  ReceptionPhotoResponse addPhoto(@PathVariable UUID id, @RequestBody ReceptionPhotoRequest request) {
    return receptions.addPhoto(id, request);
  }

  @PostMapping("/{id}/sign")
  @PreAuthorize("@perm.has(authentication, 'RECEPCION:APROBAR')")
  ReceptionDetailResponse sign(@PathVariable UUID id, @RequestBody ReceptionSignatureRequest request) {
    return receptions.sign(id, request);
  }

  @PostMapping("/{id}/work-order")
  @PreAuthorize("@perm.has(authentication, 'RECEPCION:APROBAR')")
  WorkOrderConversionResponse createWorkOrder(@PathVariable UUID id) {
    return receptions.createWorkOrder(id);
  }
}
