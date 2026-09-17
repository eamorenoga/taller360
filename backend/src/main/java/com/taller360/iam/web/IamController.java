package com.taller360.iam.web;

import com.taller360.iam.dto.IamDtos.*;
import com.taller360.iam.service.IamService;
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
public class IamController {
  private final IamService iam;

  public IamController(IamService iam) {
    this.iam = iam;
  }

  @GetMapping("/users")
  @PreAuthorize("@perm.has(authentication, 'USUARIOS:VER')")
  List<UserResponse> users() {
    return iam.users();
  }

  @PostMapping("/users")
  @PreAuthorize("@perm.has(authentication, 'USUARIOS:CREAR')")
  UserResponse createUser(@RequestBody UserRequest request) {
    return iam.createUser(request);
  }

  @PutMapping("/users/{id}")
  @PreAuthorize("@perm.has(authentication, 'USUARIOS:EDITAR')")
  UserResponse updateUser(@PathVariable UUID id, @RequestBody UserRequest request) {
    return iam.updateUser(id, request);
  }

  @DeleteMapping("/users/{id}")
  @PreAuthorize("@perm.has(authentication, 'USUARIOS:ELIMINAR')")
  ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
    iam.deleteUser(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/roles")
  @PreAuthorize("@perm.has(authentication, 'ROLES:VER')")
  List<RoleResponse> roles() {
    return iam.roles();
  }

  @PostMapping("/roles")
  @PreAuthorize("@perm.has(authentication, 'ROLES:CREAR')")
  RoleResponse createRole(@RequestBody RoleRequest request) {
    return iam.createRole(request);
  }

  @PutMapping("/roles/{id}")
  @PreAuthorize("@perm.has(authentication, 'ROLES:EDITAR')")
  RoleResponse updateRole(@PathVariable UUID id, @RequestBody RoleRequest request) {
    return iam.updateRole(id, request);
  }

  @DeleteMapping("/roles/{id}")
  @PreAuthorize("@perm.has(authentication, 'ROLES:ELIMINAR')")
  ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
    iam.deleteRole(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/permissions")
  @PreAuthorize("@perm.has(authentication, 'PERMISOS:VER') or @perm.has(authentication, 'ROLES:VER') or @perm.has(authentication, 'CONFIGURACION:VER')")
  List<PermissionResponse> permissions() {
    return iam.permissions();
  }

  @GetMapping("/permissions/matrix")
  @PreAuthorize("@perm.has(authentication, 'PERMISOS:VER') or @perm.has(authentication, 'ROLES:VER')")
  PermissionMatrixResponse matrix() {
    return iam.matrix();
  }
}
