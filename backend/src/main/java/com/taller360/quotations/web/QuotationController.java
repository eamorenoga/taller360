package com.taller360.quotations.web;

import com.taller360.quotations.dto.QuotationDtos.*;
import com.taller360.quotations.service.QuotationService;
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
@RequestMapping("/api/v1/quotations")
public class QuotationController {
  private final QuotationService quotations;

  public QuotationController(QuotationService quotations) {
    this.quotations = quotations;
  }

  @GetMapping
  @PreAuthorize("@perm.has(authentication, 'COTIZACIONES:VER')")
  List<QuotationResponse> search(@RequestParam(required = false) UUID branchId,
      @RequestParam(required = false) String q) {
    return quotations.search(branchId, q);
  }

  @GetMapping("/{id}")
  @PreAuthorize("@perm.has(authentication, 'COTIZACIONES:VER')")
  QuotationDetailResponse detail(@PathVariable UUID id) {
    return quotations.detail(id);
  }

  @GetMapping("/public/{token}")
  QuotationDetailResponse publicDetail(@PathVariable String token) {
    return quotations.publicDetail(token);
  }

  @PostMapping
  @PreAuthorize("@perm.has(authentication, 'COTIZACIONES:CREAR')")
  QuotationResponse create(@RequestBody QuotationRequest request) {
    return quotations.create(request);
  }

  @PutMapping("/{id}")
  @PreAuthorize("@perm.has(authentication, 'COTIZACIONES:EDITAR')")
  QuotationResponse update(@PathVariable UUID id, @RequestBody QuotationRequest request) {
    return quotations.update(id, request);
  }

  @PostMapping("/{id}/send")
  @PreAuthorize("@perm.has(authentication, 'COTIZACIONES:EDITAR')")
  QuotationResponse send(@PathVariable UUID id) {
    return quotations.send(id);
  }

  @PostMapping("/{id}/approve")
  @PreAuthorize("@perm.has(authentication, 'COTIZACIONES:APROBAR')")
  QuotationDetailResponse approve(@PathVariable UUID id, @RequestBody ApprovalRequest request) {
    return quotations.approve(id, request);
  }

  @PostMapping("/public/{token}/approve")
  QuotationDetailResponse publicApprove(@PathVariable String token, @RequestBody ApprovalRequest request) {
    return quotations.publicApprove(token, request);
  }

  @PostMapping("/{id}/generate-work")
  @PreAuthorize("@perm.has(authentication, 'COTIZACIONES:APROBAR')")
  GenerateWorkResponse generateWork(@PathVariable UUID id) {
    return quotations.generateWork(id);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("@perm.has(authentication, 'COTIZACIONES:ANULAR')")
  ResponseEntity<Void> annul(@PathVariable UUID id) {
    quotations.annul(id);
    return ResponseEntity.noContent().build();
  }
}
