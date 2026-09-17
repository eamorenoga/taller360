package com.taller360.iam.repository;

import com.taller360.iam.entity.UserRoleBranch;
import com.taller360.iam.entity.UserRoleBranchId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleBranchRepository extends JpaRepository<UserRoleBranch, UserRoleBranchId> {
  List<UserRoleBranch> findByUsuario_Id(UUID usuarioId);
  void deleteByUsuario_Id(UUID usuarioId);
}
