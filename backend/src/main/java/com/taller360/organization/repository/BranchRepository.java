package com.taller360.organization.repository;

import com.taller360.organization.entity.Branch;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchRepository extends JpaRepository<Branch, UUID> {
  List<Branch> findByEmpresaId(UUID empresaId);
  boolean existsByIdAndEmpresaId(UUID id, UUID empresaId);
}
