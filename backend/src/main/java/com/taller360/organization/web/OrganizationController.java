package com.taller360.organization.web;

import com.taller360.organization.dto.OrganizationDtos.*;
import com.taller360.organization.service.OrganizationService;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class OrganizationController {
  private final OrganizationService organization;

  public OrganizationController(OrganizationService organization) {
    this.organization = organization;
  }

  @GetMapping("/companies")
  @PreAuthorize("@perm.has(authentication, 'EMPRESAS:VER') or @perm.has(authentication, 'CONFIGURACION:VER')")
  List<CompanyResponse> companies() {
    return organization.companies();
  }

  @PostMapping("/companies")
  @PreAuthorize("@perm.has(authentication, 'EMPRESAS:CREAR')")
  CompanyResponse createCompany(@RequestBody CompanyRequest request) {
    return organization.createCompany(request);
  }

  @PutMapping("/companies/{id}")
  @PreAuthorize("@perm.has(authentication, 'EMPRESAS:EDITAR') or @perm.has(authentication, 'CONFIGURACION:EDITAR')")
  CompanyResponse updateCompany(@PathVariable UUID id, @RequestBody CompanyRequest request) {
    return organization.updateCompany(id, request);
  }

  @DeleteMapping("/companies/{id}")
  @PreAuthorize("@perm.has(authentication, 'EMPRESAS:ELIMINAR')")
  ResponseEntity<Void> deleteCompany(@PathVariable UUID id) {
    organization.deleteCompany(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/branches")
  @PreAuthorize("@perm.has(authentication, 'SUCURSALES:VER') or @perm.has(authentication, 'CONFIGURACION:VER')")
  List<BranchResponse> branches() {
    return organization.branches();
  }

  @PostMapping("/branches")
  @PreAuthorize("@perm.has(authentication, 'SUCURSALES:CREAR')")
  BranchResponse createBranch(@RequestBody BranchRequest request) {
    return organization.createBranch(request);
  }

  @PutMapping("/branches/{id}")
  @PreAuthorize("@perm.has(authentication, 'SUCURSALES:EDITAR')")
  BranchResponse updateBranch(@PathVariable UUID id, @RequestBody BranchRequest request) {
    return organization.updateBranch(id, request);
  }

  @DeleteMapping("/branches/{id}")
  @PreAuthorize("@perm.has(authentication, 'SUCURSALES:ELIMINAR')")
  ResponseEntity<Void> deleteBranch(@PathVariable UUID id) {
    organization.deleteBranch(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/taxes")
  @PreAuthorize("@perm.has(authentication, 'IMPUESTOS:VER') or @perm.has(authentication, 'CONFIGURACION:VER')")
  List<TaxResponse> taxes() {
    return organization.taxes();
  }

  @PostMapping("/taxes")
  @PreAuthorize("@perm.has(authentication, 'IMPUESTOS:CREAR')")
  TaxResponse createTax(@RequestBody TaxRequest request) {
    return organization.createTax(request);
  }

  @PutMapping("/taxes/{id}")
  @PreAuthorize("@perm.has(authentication, 'IMPUESTOS:EDITAR')")
  TaxResponse updateTax(@PathVariable UUID id, @RequestBody TaxRequest request) {
    return organization.updateTax(id, request);
  }

  @DeleteMapping("/taxes/{id}")
  @PreAuthorize("@perm.has(authentication, 'IMPUESTOS:ELIMINAR')")
  ResponseEntity<Void> deleteTax(@PathVariable UUID id) {
    organization.deleteTax(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/consecutives")
  @PreAuthorize("@perm.has(authentication, 'CONSECUTIVOS:VER') or @perm.has(authentication, 'CONFIGURACION:VER')")
  List<ConsecutiveResponse> consecutives() {
    return organization.consecutives();
  }

  @PostMapping("/consecutives")
  @PreAuthorize("@perm.has(authentication, 'CONSECUTIVOS:CREAR')")
  ConsecutiveResponse createConsecutive(@RequestBody ConsecutiveRequest request) {
    return organization.createConsecutive(request);
  }

  @PutMapping("/consecutives/{id}")
  @PreAuthorize("@perm.has(authentication, 'CONSECUTIVOS:EDITAR')")
  ConsecutiveResponse updateConsecutive(@PathVariable UUID id, @RequestBody ConsecutiveRequest request) {
    return organization.updateConsecutive(id, request);
  }

  @DeleteMapping("/consecutives/{id}")
  @PreAuthorize("@perm.has(authentication, 'CONSECUTIVOS:ELIMINAR')")
  ResponseEntity<Void> deleteConsecutive(@PathVariable UUID id) {
    organization.deleteConsecutive(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/operational-parameters")
  @PreAuthorize("@perm.has(authentication, 'PARAMETROS:VER') or @perm.has(authentication, 'CONFIGURACION:VER')")
  List<ParameterResponse> parameters() {
    return organization.parameters();
  }

  @PostMapping("/operational-parameters")
  @PreAuthorize("@perm.has(authentication, 'PARAMETROS:CREAR')")
  ParameterResponse createParameter(@RequestBody ParameterRequest request) {
    return organization.createParameter(request);
  }

  @PutMapping("/operational-parameters/{id}")
  @PreAuthorize("@perm.has(authentication, 'PARAMETROS:EDITAR')")
  ParameterResponse updateParameter(@PathVariable UUID id, @RequestBody ParameterRequest request) {
    return organization.updateParameter(id, request);
  }

  @DeleteMapping("/operational-parameters/{id}")
  @PreAuthorize("@perm.has(authentication, 'PARAMETROS:ELIMINAR')")
  ResponseEntity<Void> deleteParameter(@PathVariable UUID id) {
    organization.deleteParameter(id);
    return ResponseEntity.noContent().build();
  }
}
