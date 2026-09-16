package com.taller360.iam.repository;

import com.taller360.iam.entity.Permission;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
  List<Permission> findAllByOrderByModuloAscAccionAsc();
}
