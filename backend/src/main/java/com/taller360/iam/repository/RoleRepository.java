package com.taller360.iam.repository;

import com.taller360.iam.entity.Role;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, UUID> {
  List<Role> findByEmpresaIdOrEmpresaIdIsNull(UUID empresaId);
  List<Role> findByEmpresaId(UUID empresaId);
}
