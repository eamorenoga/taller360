package com.taller360.iam.dto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class IamDtos {
  public record UserResponse(UUID id, UUID empresaId, String nombre, String apellido, String email, String telefono,
      String estado, List<RoleAssignmentResponse> roles, List<UUID> sucursales) {}

  public record UserRequest(String nombre, String apellido, String email, String telefono, String password,
      String estado, Set<UUID> branchIds, List<RoleAssignmentRequest> roles) {}

  public record RoleAssignmentRequest(UUID roleId, UUID branchId) {}

  public record RoleAssignmentResponse(UUID roleId, String rol, UUID branchId, String sucursal) {}

  public record RoleResponse(UUID id, UUID empresaId, String nombre, String descripcion, String estado,
      String alcance, boolean configurable, List<String> permisos) {}

  public record RoleRequest(String nombre, String descripcion, String estado, String alcance,
      boolean configurable, Set<UUID> permissionIds) {}

  public record PermissionResponse(UUID id, String modulo, String accion, String code, String descripcion) {}

  public record PermissionMatrixResponse(List<String> recursos, List<String> acciones, List<RoleMatrixResponse> roles) {}

  public record RoleMatrixResponse(UUID roleId, String nombre, String alcance, List<String> permisos) {}
}
