package com.taller360.organization.web;

import com.taller360.common.tenant.TenantGuard;
import com.taller360.organization.repository.BranchRepository;
import com.taller360.organization.repository.CompanyRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class OrganizationController {
  private final CompanyRepository companies;
  private final BranchRepository branches;
  private final TenantGuard tenant;

  public OrganizationController(CompanyRepository companies, BranchRepository branches, TenantGuard tenant) {
    this.companies = companies;
    this.branches = branches;
    this.tenant = tenant;
  }

  public record CompanyResponse(UUID id, String nombre, String nit, String estado) {}
  public record BranchResponse(UUID id, UUID empresaId, String nombre, String direccion, String telefono, String estado) {}

  @GetMapping("/companies")
  @PreAuthorize("@perm.has(authentication, 'CONFIGURACION:VER')")
  List<CompanyResponse> companies() {
    return companies.findById(tenant.companyId())
        .stream()
        .map(c -> new CompanyResponse(c.id, c.nombre, c.nit, c.estado))
        .toList();
  }

  @GetMapping("/branches")
  @PreAuthorize("@perm.has(authentication, 'SUCURSALES:VER') or @perm.has(authentication, 'CONFIGURACION:VER')")
  List<BranchResponse> branches() {
    return branches.findByEmpresaId(tenant.companyId()).stream()
        .map(b -> new BranchResponse(b.id, b.empresa.id, b.nombre, b.direccion, b.telefono, b.estado))
        .toList();
  }
}
