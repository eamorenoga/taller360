package com.taller360.organization.repository;

import com.taller360.organization.entity.Consecutive;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsecutiveRepository extends JpaRepository<Consecutive, UUID> {
  List<Consecutive> findByEmpresaIdOrderByDocumentoAsc(UUID empresaId);
  Optional<Consecutive> findByIdAndEmpresaId(UUID id, UUID empresaId);
}
