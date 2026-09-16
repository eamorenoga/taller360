package com.taller360.iam.web;

import com.taller360.common.tenant.TenantGuard;
import com.taller360.iam.entity.Role;
import com.taller360.iam.entity.User;
import com.taller360.iam.repository.PermissionRepository;
import com.taller360.iam.repository.RoleRepository;
import com.taller360.iam.repository.UserRepository;
import com.taller360.organization.entity.Company;
import com.taller360.organization.repository.BranchRepository;
import com.taller360.organization.repository.CompanyRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class IamController {
  private final UserRepository users;
  private final RoleRepository roles;
  private final PermissionRepository permissions;
  private final CompanyRepository companies;
  private final BranchRepository branches;
  private final PasswordEncoder passwordEncoder;
  private final TenantGuard tenant;

  public IamController(UserRepository users, RoleRepository roles, PermissionRepository permissions, CompanyRepository companies,
      BranchRepository branches, PasswordEncoder passwordEncoder, TenantGuard tenant) {
    this.users = users;
    this.roles = roles;
    this.permissions = permissions;
    this.companies = companies;
    this.branches = branches;
    this.passwordEncoder = passwordEncoder;
    this.tenant = tenant;
  }

  public record UserResponse(UUID id, UUID empresaId, String nombre, String apellido, String email, String telefono, String estado,
      List<String> roles, List<UUID> sucursales) {}
  public record CreateUserRequest(String nombre, String apellido, String email, String telefono, String password, Set<UUID> roleIds,
      Set<UUID> branchIds) {}
  public record RoleResponse(UUID id, UUID empresaId, String nombre, String descripcion, List<String> permisos) {}
  public record CreateRoleRequest(String nombre, String descripcion, Set<UUID> permissionIds) {}
  public record PermissionResponse(UUID id, String modulo, String accion, String code) {}

  @GetMapping("/users")
  @PreAuthorize("@perm.has(authentication, 'USUARIOS:VER')")
  List<UserResponse> users() {
    return users.findByEmpresaId(tenant.companyId()).stream().map(this::toUser).toList();
  }

  @PostMapping("/users")
  @PreAuthorize("@perm.has(authentication, 'USUARIOS:CREAR')")
  UserResponse createUser(@RequestBody CreateUserRequest request) {
    Company company = companies.findById(tenant.companyId()).orElseThrow();
    User user = new User();
    user.id = UUID.randomUUID();
    user.empresa = company;
    user.nombre = request.nombre();
    user.apellido = request.apellido();
    user.email = request.email();
    user.telefono = request.telefono();
    user.passwordHash = passwordEncoder.encode(request.password());
    user.estado = "ACTIVO";
    if (request.branchIds() != null) {
      request.branchIds().forEach(tenant::assertBranchAllowed);
      user.sucursales = new HashSet<>(branches.findAllById(request.branchIds()));
    }
    if (request.roleIds() != null) {
      user.roles = new HashSet<>(roles.findAllById(request.roleIds()));
    }
    return toUser(users.save(user));
  }

  @GetMapping("/roles")
  @PreAuthorize("@perm.has(authentication, 'ROLES:VER')")
  List<RoleResponse> roles() {
    return roles.findByEmpresaIdOrEmpresaIdIsNull(tenant.companyId()).stream().map(this::toRole).toList();
  }

  @PostMapping("/roles")
  @PreAuthorize("@perm.has(authentication, 'ROLES:CREAR')")
  RoleResponse createRole(@RequestBody CreateRoleRequest request) {
    Role role = new Role();
    role.id = UUID.randomUUID();
    role.empresa = companies.findById(tenant.companyId()).orElseThrow();
    role.nombre = request.nombre();
    role.descripcion = request.descripcion();
    role.configurable = true;
    if (request.permissionIds() != null) {
      role.permisos = new HashSet<>(permissions.findAllById(request.permissionIds()));
    }
    return toRole(roles.save(role));
  }

  @GetMapping("/permissions")
  @PreAuthorize("@perm.has(authentication, 'ROLES:VER') or @perm.has(authentication, 'CONFIGURACION:VER')")
  List<PermissionResponse> permissions() {
    return permissions.findAllByOrderByModuloAscAccionAsc().stream()
        .map(p -> new PermissionResponse(p.id, p.modulo, p.accion, p.code()))
        .toList();
  }

  private UserResponse toUser(User user) {
    if (!user.empresa.id.equals(tenant.companyId())) {
      throw new AccessDeniedException("Consulta cruzada entre empresas bloqueada");
    }
    return new UserResponse(user.id, user.empresa.id, user.nombre, user.apellido, user.email, user.telefono, user.estado,
        user.roles.stream().map(r -> r.nombre).toList(), user.sucursales.stream().map(b -> b.id).toList());
  }

  private RoleResponse toRole(Role role) {
    return new RoleResponse(role.id, role.empresa == null ? null : role.empresa.id, role.nombre, role.descripcion,
        role.permisos.stream().map(p -> p.code()).sorted().toList());
  }
}
