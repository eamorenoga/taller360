package com.taller360.iam.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taller360.audit.AuditPort;
import com.taller360.common.tenant.TenantGuard;
import com.taller360.iam.dto.IamDtos.*;
import com.taller360.iam.entity.Permission;
import com.taller360.iam.entity.Role;
import com.taller360.iam.entity.User;
import com.taller360.iam.entity.UserRoleBranch;
import com.taller360.iam.entity.UserRoleBranchId;
import com.taller360.iam.repository.PermissionRepository;
import com.taller360.iam.repository.RoleRepository;
import com.taller360.iam.repository.UserRepository;
import com.taller360.iam.repository.UserRoleBranchRepository;
import com.taller360.organization.entity.Branch;
import com.taller360.organization.entity.Company;
import com.taller360.organization.repository.BranchRepository;
import com.taller360.organization.repository.CompanyRepository;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IamService {
  private static final List<String> ACTIONS = List.of("VER", "CREAR", "EDITAR", "ELIMINAR", "APROBAR", "ASIGNAR", "EXPORTAR", "ANULAR");

  private final UserRepository users;
  private final RoleRepository roles;
  private final PermissionRepository permissions;
  private final UserRoleBranchRepository scopedRoles;
  private final CompanyRepository companies;
  private final BranchRepository branches;
  private final PasswordEncoder passwordEncoder;
  private final TenantGuard tenant;
  private final AuditPort audit;
  private final ObjectMapper objectMapper;

  public IamService(UserRepository users, RoleRepository roles, PermissionRepository permissions,
      UserRoleBranchRepository scopedRoles, CompanyRepository companies, BranchRepository branches,
      PasswordEncoder passwordEncoder, TenantGuard tenant, AuditPort audit, ObjectMapper objectMapper) {
    this.users = users;
    this.roles = roles;
    this.permissions = permissions;
    this.scopedRoles = scopedRoles;
    this.companies = companies;
    this.branches = branches;
    this.passwordEncoder = passwordEncoder;
    this.tenant = tenant;
    this.audit = audit;
    this.objectMapper = objectMapper;
  }

  public List<UserResponse> users() {
    return users.findByEmpresaId(tenant.companyId()).stream().map(this::toUser).toList();
  }

  @Transactional
  public UserResponse createUser(UserRequest request) {
    Company company = companies.findById(tenant.companyId()).orElseThrow();
    User user = new User();
    user.id = UUID.randomUUID();
    user.empresa = company;
    applyUser(user, request, true);
    user.fechaCreacion = Instant.now();
    user.fechaModificacion = Instant.now();
    User saved = users.save(user);
    replaceRoleAssignments(saved, request.roles());
    audit.record("USUARIOS", "CREAR", "usuarios", saved.id, null, json(toUser(saved)));
    return toUser(saved);
  }

  @Transactional
  public UserResponse updateUser(UUID id, UserRequest request) {
    User user = userInTenant(id);
    String before = json(toUser(user));
    applyUser(user, request, false);
    user.fechaModificacion = Instant.now();
    User saved = users.save(user);
    if (request.roles() != null) {
      replaceRoleAssignments(saved, request.roles());
    }
    audit.record("USUARIOS", "EDITAR", "usuarios", saved.id, before, json(toUser(saved)));
    return toUser(saved);
  }

  @Transactional
  public void deleteUser(UUID id) {
    User user = userInTenant(id);
    String before = json(toUser(user));
    user.estado = "INACTIVO";
    user.fechaModificacion = Instant.now();
    users.save(user);
    audit.record("USUARIOS", "ELIMINAR", "usuarios", id, before, "{\"estado\":\"INACTIVO\"}");
  }

  public List<RoleResponse> roles() {
    return roles.findByEmpresaIdOrEmpresaIdIsNull(tenant.companyId()).stream().map(this::toRole).toList();
  }

  @Transactional
  public RoleResponse createRole(RoleRequest request) {
    Role role = new Role();
    role.id = UUID.randomUUID();
    role.empresa = companies.findById(tenant.companyId()).orElseThrow();
    applyRole(role, request);
    role.fechaCreacion = Instant.now();
    role.fechaModificacion = Instant.now();
    Role saved = roles.save(role);
    audit.record("ROLES", "CREAR", "roles", saved.id, null, json(toRole(saved)));
    return toRole(saved);
  }

  @Transactional
  public RoleResponse updateRole(UUID id, RoleRequest request) {
    Role role = roleInTenant(id);
    String before = json(toRole(role));
    applyRole(role, request);
    role.fechaModificacion = Instant.now();
    Role saved = roles.save(role);
    audit.record("ROLES", "EDITAR", "roles", saved.id, before, json(toRole(saved)));
    return toRole(saved);
  }

  @Transactional
  public void deleteRole(UUID id) {
    Role role = roleInTenant(id);
    String before = json(toRole(role));
    role.estado = "INACTIVO";
    role.fechaModificacion = Instant.now();
    roles.save(role);
    audit.record("ROLES", "ELIMINAR", "roles", id, before, "{\"estado\":\"INACTIVO\"}");
  }

  public List<PermissionResponse> permissions() {
    return permissions.findAllByOrderByModuloAscAccionAsc().stream().map(this::toPermission).toList();
  }

  public PermissionMatrixResponse matrix() {
    List<Permission> all = permissions.findAllByOrderByModuloAscAccionAsc();
    List<String> resources = all.stream().map(p -> p.modulo).distinct().toList();
    List<RoleMatrixResponse> roleRows = roles.findByEmpresaIdOrEmpresaIdIsNull(tenant.companyId()).stream()
        .map(role -> new RoleMatrixResponse(role.id, role.nombre, role.alcance,
            role.permisos.stream().map(Permission::code).sorted().toList()))
        .toList();
    return new PermissionMatrixResponse(resources, ACTIONS, roleRows);
  }

  private void applyUser(User user, UserRequest request, boolean requirePassword) {
    user.nombre = required(request.nombre(), "nombre");
    user.apellido = required(request.apellido(), "apellido");
    user.email = required(request.email(), "email").toLowerCase();
    user.telefono = request.telefono();
    user.estado = defaultValue(request.estado(), "ACTIVO");
    if (request.password() != null && !request.password().isBlank()) {
      user.passwordHash = passwordEncoder.encode(request.password());
    } else if (requirePassword) {
      throw new IllegalArgumentException("El password es obligatorio");
    }
    if (request.branchIds() != null) {
      request.branchIds().forEach(tenant::assertBranchAllowed);
      user.sucursales = new HashSet<>(branches.findAllById(request.branchIds()));
      if (user.sucursales.size() != request.branchIds().size()) {
        throw new AccessDeniedException("Una o varias sucursales no pertenecen al tenant");
      }
    }
  }

  private void replaceRoleAssignments(User user, List<RoleAssignmentRequest> assignments) {
    user.roles.clear();
    scopedRoles.deleteByUsuario_Id(user.id);
    if (assignments == null) {
      return;
    }
    for (RoleAssignmentRequest assignment : assignments) {
      Role role = roleInTenant(assignment.roleId());
      user.roles.add(role);
      if (assignment.branchId() != null) {
        Branch branch = branchInTenant(assignment.branchId());
        UserRoleBranch scoped = new UserRoleBranch();
        scoped.id = new UserRoleBranchId(user.id, role.id, branch.id);
        scoped.usuario = user;
        scoped.rol = role;
        scoped.sucursal = branch;
        scoped.fechaCreacion = Instant.now();
        scopedRoles.save(scoped);
      }
    }
  }

  private void applyRole(Role role, RoleRequest request) {
    role.nombre = required(request.nombre(), "nombre").toUpperCase();
    role.descripcion = request.descripcion();
    role.estado = defaultValue(request.estado(), "ACTIVO");
    role.alcance = defaultValue(request.alcance(), "EMPRESA").toUpperCase();
    role.configurable = request.configurable();
    role.permisos = request.permissionIds() == null ? new HashSet<>() : new HashSet<>(permissions.findAllById(request.permissionIds()));
    if (request.permissionIds() != null && role.permisos.size() != request.permissionIds().size()) {
      throw new IllegalArgumentException("Uno o varios permisos no existen");
    }
  }

  private User userInTenant(UUID id) {
    return users.findByIdAndEmpresaId(id, tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Usuario no pertenece a la empresa actual"));
  }

  private Role roleInTenant(UUID id) {
    Role role = roles.findById(id).orElseThrow();
    if (role.empresa != null && !role.empresa.id.equals(tenant.companyId())) {
      throw new AccessDeniedException("Rol no pertenece a la empresa actual");
    }
    return role;
  }

  private Branch branchInTenant(UUID id) {
    return branches.findByIdAndEmpresaId(id, tenant.companyId())
        .orElseThrow(() -> new AccessDeniedException("Sucursal no pertenece a la empresa actual"));
  }

  private UserResponse toUser(User user) {
    if (!user.empresa.id.equals(tenant.companyId())) {
      throw new AccessDeniedException("Consulta cruzada entre empresas bloqueada");
    }
    List<UserRoleBranch> scoped = scopedRoles.findByUsuario_Id(user.id);
    List<RoleAssignmentResponse> assigned = user.roles.stream()
        .map(role -> scoped.stream()
            .filter(item -> item.rol.id.equals(role.id))
            .findFirst()
            .map(item -> new RoleAssignmentResponse(role.id, role.nombre, item.sucursal.id, item.sucursal.nombre))
            .orElseGet(() -> new RoleAssignmentResponse(role.id, role.nombre, null, "Todas")))
        .toList();
    return new UserResponse(user.id, user.empresa.id, user.nombre, user.apellido, user.email, user.telefono,
        user.estado, assigned, user.sucursales.stream().map(b -> b.id).toList());
  }

  private RoleResponse toRole(Role role) {
    return new RoleResponse(role.id, role.empresa == null ? null : role.empresa.id, role.nombre, role.descripcion,
        role.estado, role.alcance, role.configurable, role.permisos.stream().map(Permission::code).sorted().toList());
  }

  private PermissionResponse toPermission(Permission permission) {
    return new PermissionResponse(permission.id, permission.modulo, permission.accion, permission.code(), permission.descripcion);
  }

  private String json(Object value) {
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
}
