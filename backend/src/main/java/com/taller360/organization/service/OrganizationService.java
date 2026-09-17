package com.taller360.organization.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller360.audit.AuditPort;
import com.taller360.common.tenant.TenantGuard;
import com.taller360.organization.dto.OrganizationDtos.*;
import com.taller360.organization.entity.Branch;
import com.taller360.organization.entity.Company;
import com.taller360.organization.entity.CompanyTax;
import com.taller360.organization.entity.Consecutive;
import com.taller360.organization.entity.OperationalParameter;
import com.taller360.organization.repository.BranchRepository;
import com.taller360.organization.repository.CompanyRepository;
import com.taller360.organization.repository.CompanyTaxRepository;
import com.taller360.organization.repository.ConsecutiveRepository;
import com.taller360.organization.repository.OperationalParameterRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganizationService {
  private final CompanyRepository companies;
  private final BranchRepository branches;
  private final CompanyTaxRepository taxes;
  private final ConsecutiveRepository consecutives;
  private final OperationalParameterRepository parameters;
  private final TenantGuard tenant;
  private final AuditPort audit;
  private final ObjectMapper objectMapper;

  public OrganizationService(CompanyRepository companies, BranchRepository branches, CompanyTaxRepository taxes,
      ConsecutiveRepository consecutives, OperationalParameterRepository parameters, TenantGuard tenant,
      AuditPort audit, ObjectMapper objectMapper) {
    this.companies = companies;
    this.branches = branches;
    this.taxes = taxes;
    this.consecutives = consecutives;
    this.parameters = parameters;
    this.tenant = tenant;
    this.audit = audit;
    this.objectMapper = objectMapper;
  }

  public List<CompanyResponse> companies() {
    return companies.findById(tenant.companyId()).stream().map(this::toCompany).toList();
  }

  @Transactional
  public CompanyResponse createCompany(CompanyRequest request) {
    Company company = new Company();
    company.id = UUID.randomUUID();
    company.nombre = required(request.nombre(), "nombre");
    company.nit = request.nit();
    company.estado = defaultValue(request.estado(), "ACTIVA");
    company.moneda = currency(request.moneda());
    company.zonaHoraria = defaultValue(request.zonaHoraria(), "America/Bogota");
    company.email = request.email();
    company.telefono = request.telefono();
    company.direccion = request.direccion();
    company.fechaCreacion = Instant.now();
    company.fechaModificacion = Instant.now();
    Company saved = companies.save(company);
    audit.record("EMPRESAS", "CREAR", "empresas", saved.id, null, json(toCompany(saved)));
    return toCompany(saved);
  }

  @Transactional
  public CompanyResponse updateCompany(UUID id, CompanyRequest request) {
    if (!id.equals(tenant.companyId())) {
      throw new AccessDeniedException("Consulta cruzada entre empresas bloqueada");
    }
    Company company = companies.findById(id).orElseThrow();
    String before = json(toCompany(company));
    company.nombre = required(request.nombre(), "nombre");
    company.nit = request.nit();
    company.estado = defaultValue(request.estado(), "ACTIVA");
    company.moneda = currency(request.moneda());
    company.zonaHoraria = defaultValue(request.zonaHoraria(), "America/Bogota");
    company.email = request.email();
    company.telefono = request.telefono();
    company.direccion = request.direccion();
    company.fechaModificacion = Instant.now();
    Company saved = companies.save(company);
    audit.record("EMPRESAS", "EDITAR", "empresas", saved.id, before, json(toCompany(saved)));
    return toCompany(saved);
  }

  @Transactional
  public void deleteCompany(UUID id) {
    if (!id.equals(tenant.companyId())) {
      throw new AccessDeniedException("Consulta cruzada entre empresas bloqueada");
    }
    Company company = companies.findById(id).orElseThrow();
    String before = json(toCompany(company));
    company.estado = "INACTIVA";
    company.fechaModificacion = Instant.now();
    companies.save(company);
    audit.record("EMPRESAS", "ELIMINAR", "empresas", id, before, "{\"estado\":\"INACTIVA\"}");
  }

  public List<BranchResponse> branches() {
    return branches.findByEmpresaId(tenant.companyId()).stream().map(this::toBranch).toList();
  }

  @Transactional
  public BranchResponse createBranch(BranchRequest request) {
    Company company = companies.findById(tenant.companyId()).orElseThrow();
    Branch branch = new Branch();
    branch.id = UUID.randomUUID();
    branch.empresa = company;
    applyBranch(branch, request);
    branch.fechaCreacion = Instant.now();
    branch.fechaModificacion = Instant.now();
    Branch saved = branches.save(branch);
    audit.record("SUCURSALES", "CREAR", "sucursales", saved.id, null, json(toBranch(saved)));
    return toBranch(saved);
  }

  @Transactional
  public BranchResponse updateBranch(UUID id, BranchRequest request) {
    Branch branch = branchInTenant(id);
    String before = json(toBranch(branch));
    applyBranch(branch, request);
    branch.fechaModificacion = Instant.now();
    Branch saved = branches.save(branch);
    audit.record("SUCURSALES", "EDITAR", "sucursales", saved.id, before, json(toBranch(saved)));
    return toBranch(saved);
  }

  @Transactional
  public void deleteBranch(UUID id) {
    Branch branch = branchInTenant(id);
    branch.estado = "INACTIVA";
    branch.permiteOperacion = false;
    branch.fechaModificacion = Instant.now();
    branches.save(branch);
    audit.record("SUCURSALES", "ELIMINAR", "sucursales", id, null, "{\"estado\":\"INACTIVA\"}");
  }

  public List<TaxResponse> taxes() {
    return taxes.findByEmpresaIdOrderByCodigoAsc(tenant.companyId()).stream().map(this::toTax).toList();
  }

  @Transactional
  public TaxResponse createTax(TaxRequest request) {
    CompanyTax tax = new CompanyTax();
    tax.id = UUID.randomUUID();
    tax.empresa = companies.findById(tenant.companyId()).orElseThrow();
    applyTax(tax, request);
    tax.fechaCreacion = Instant.now();
    tax.fechaModificacion = Instant.now();
    CompanyTax saved = taxes.save(tax);
    audit.record("IMPUESTOS", "CREAR", "empresa_impuestos", saved.id, null, json(toTax(saved)));
    return toTax(saved);
  }

  @Transactional
  public TaxResponse updateTax(UUID id, TaxRequest request) {
    CompanyTax tax = taxes.findByIdAndEmpresaId(id, tenant.companyId()).orElseThrow();
    String before = json(toTax(tax));
    applyTax(tax, request);
    tax.fechaModificacion = Instant.now();
    CompanyTax saved = taxes.save(tax);
    audit.record("IMPUESTOS", "EDITAR", "empresa_impuestos", saved.id, before, json(toTax(saved)));
    return toTax(saved);
  }

  @Transactional
  public void deleteTax(UUID id) {
    CompanyTax tax = taxes.findByIdAndEmpresaId(id, tenant.companyId()).orElseThrow();
    String before = json(toTax(tax));
    tax.activo = false;
    tax.fechaModificacion = Instant.now();
    taxes.save(tax);
    audit.record("IMPUESTOS", "ELIMINAR", "empresa_impuestos", id, before, "{\"activo\":false}");
  }

  public List<ConsecutiveResponse> consecutives() {
    return consecutives.findByEmpresaIdOrderByDocumentoAsc(tenant.companyId()).stream().map(this::toConsecutive).toList();
  }

  @Transactional
  public ConsecutiveResponse createConsecutive(ConsecutiveRequest request) {
    Consecutive consecutive = new Consecutive();
    consecutive.id = UUID.randomUUID();
    consecutive.empresa = companies.findById(tenant.companyId()).orElseThrow();
    applyConsecutive(consecutive, request);
    consecutive.fechaCreacion = Instant.now();
    consecutive.fechaModificacion = Instant.now();
    Consecutive saved = consecutives.save(consecutive);
    audit.record("CONSECUTIVOS", "CREAR", "consecutivos", saved.id, null, json(toConsecutive(saved)));
    return toConsecutive(saved);
  }

  @Transactional
  public ConsecutiveResponse updateConsecutive(UUID id, ConsecutiveRequest request) {
    Consecutive consecutive = consecutives.findByIdAndEmpresaId(id, tenant.companyId()).orElseThrow();
    String before = json(toConsecutive(consecutive));
    applyConsecutive(consecutive, request);
    consecutive.fechaModificacion = Instant.now();
    Consecutive saved = consecutives.save(consecutive);
    audit.record("CONSECUTIVOS", "EDITAR", "consecutivos", saved.id, before, json(toConsecutive(saved)));
    return toConsecutive(saved);
  }

  @Transactional
  public void deleteConsecutive(UUID id) {
    Consecutive consecutive = consecutives.findByIdAndEmpresaId(id, tenant.companyId()).orElseThrow();
    String before = json(toConsecutive(consecutive));
    consecutive.activo = false;
    consecutive.fechaModificacion = Instant.now();
    consecutives.save(consecutive);
    audit.record("CONSECUTIVOS", "ELIMINAR", "consecutivos", id, before, "{\"activo\":false}");
  }

  public List<ParameterResponse> parameters() {
    return parameters.findByEmpresaIdOrderByClaveAsc(tenant.companyId()).stream().map(this::toParameter).toList();
  }

  @Transactional
  public ParameterResponse createParameter(ParameterRequest request) {
    OperationalParameter parameter = new OperationalParameter();
    parameter.id = UUID.randomUUID();
    parameter.empresa = companies.findById(tenant.companyId()).orElseThrow();
    applyParameter(parameter, request);
    parameter.fechaCreacion = Instant.now();
    parameter.fechaModificacion = Instant.now();
    OperationalParameter saved = parameters.save(parameter);
    audit.record("PARAMETROS", "CREAR", "parametros_operativos", saved.id, null, json(toParameter(saved)));
    return toParameter(saved);
  }

  @Transactional
  public ParameterResponse updateParameter(UUID id, ParameterRequest request) {
    OperationalParameter parameter = parameters.findByIdAndEmpresaId(id, tenant.companyId()).orElseThrow();
    String before = json(toParameter(parameter));
    applyParameter(parameter, request);
    parameter.fechaModificacion = Instant.now();
    OperationalParameter saved = parameters.save(parameter);
    audit.record("PARAMETROS", "EDITAR", "parametros_operativos", saved.id, before, json(toParameter(saved)));
    return toParameter(saved);
  }

  @Transactional
  public void deleteParameter(UUID id) {
    OperationalParameter parameter = parameters.findByIdAndEmpresaId(id, tenant.companyId()).orElseThrow();
    String before = json(toParameter(parameter));
    parameters.delete(parameter);
    audit.record("PARAMETROS", "ELIMINAR", "parametros_operativos", id, before, null);
  }

  private void applyBranch(Branch branch, BranchRequest request) {
    branch.nombre = required(request.nombre(), "nombre");
    branch.codigo = request.codigo() == null ? null : request.codigo().trim().toUpperCase();
    branch.direccion = request.direccion();
    branch.telefono = request.telefono();
    branch.email = request.email();
    branch.moneda = request.moneda() == null ? branch.empresa.moneda : currency(request.moneda());
    branch.zonaHoraria = defaultValue(request.zonaHoraria(), branch.empresa.zonaHoraria);
    branch.permiteOperacion = request.permiteOperacion() == null || request.permiteOperacion();
    branch.estado = defaultValue(request.estado(), "ACTIVA");
  }

  private void applyTax(CompanyTax tax, TaxRequest request) {
    tax.nombre = required(request.nombre(), "nombre");
    tax.codigo = required(request.codigo(), "codigo").toUpperCase();
    if (request.porcentaje() == null || request.porcentaje().signum() < 0) {
      throw new IllegalArgumentException("El porcentaje del impuesto debe ser mayor o igual a cero");
    }
    tax.porcentaje = request.porcentaje();
    tax.incluido = request.incluido();
    tax.activo = request.activo();
  }

  private void applyConsecutive(Consecutive consecutive, ConsecutiveRequest request) {
    consecutive.documento = required(request.documento(), "documento").toUpperCase();
    consecutive.prefijo = request.prefijo() == null ? "" : request.prefijo().toUpperCase();
    consecutive.siguienteNumero = request.siguienteNumero() <= 0 ? 1 : request.siguienteNumero();
    consecutive.longitud = request.longitud() <= 0 ? 6 : request.longitud();
    consecutive.activo = request.activo();
    consecutive.sucursal = request.sucursalId() == null ? null : branchInTenant(request.sucursalId());
  }

  private void applyParameter(OperationalParameter parameter, ParameterRequest request) {
    parameter.clave = required(request.clave(), "clave").toUpperCase();
    parameter.valor = request.valor() == null ? "" : request.valor();
    parameter.tipo = defaultValue(request.tipo(), "TEXTO").toUpperCase();
    parameter.descripcion = request.descripcion();
    parameter.sucursal = request.sucursalId() == null ? null : branchInTenant(request.sucursalId());
  }

  private Branch branchInTenant(UUID id) {
    return branches.findByIdAndEmpresaId(id, tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Sucursal no pertenece a la empresa actual"));
  }

  private CompanyResponse toCompany(Company company) {
    return new CompanyResponse(company.id, company.nombre, company.nit, company.estado, company.moneda,
        company.zonaHoraria, company.email, company.telefono, company.direccion, company.fechaCreacion,
        company.fechaModificacion);
  }

  private BranchResponse toBranch(Branch branch) {
    return new BranchResponse(branch.id, branch.empresa.id, branch.nombre, branch.codigo, branch.direccion,
        branch.telefono, branch.email, branch.moneda, branch.zonaHoraria, branch.permiteOperacion, branch.estado);
  }

  private TaxResponse toTax(CompanyTax tax) {
    return new TaxResponse(tax.id, tax.nombre, tax.codigo, tax.porcentaje, tax.incluido, tax.activo);
  }

  private ConsecutiveResponse toConsecutive(Consecutive consecutive) {
    String number = String.format("%0" + consecutive.longitud + "d", consecutive.siguienteNumero);
    return new ConsecutiveResponse(consecutive.id, consecutive.sucursal == null ? null : consecutive.sucursal.id,
        consecutive.documento, consecutive.prefijo, consecutive.siguienteNumero, consecutive.longitud,
        consecutive.activo, consecutive.prefijo + number);
  }

  private ParameterResponse toParameter(OperationalParameter parameter) {
    return new ParameterResponse(parameter.id, parameter.sucursal == null ? null : parameter.sucursal.id,
        parameter.clave, parameter.valor, parameter.tipo, parameter.descripcion);
  }

  private String json(Object value) {
    if (value == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(e);
    }
  }

  private String required(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("El campo " + field + " es obligatorio");
    }
    return value.trim();
  }

  private String defaultValue(String value, String fallback) {
    return value == null || value.isBlank() ? fallback : value.trim();
  }

  private String currency(String value) {
    String currency = defaultValue(value, "COP").toUpperCase();
    if (currency.length() != 3) {
      throw new IllegalArgumentException("La moneda debe usar codigo ISO de 3 letras");
    }
    return currency;
  }
}
