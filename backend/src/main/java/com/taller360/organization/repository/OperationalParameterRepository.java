package com.taller360.organization.repository;

import com.taller360.organization.entity.OperationalParameter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationalParameterRepository extends JpaRepository<OperationalParameter, UUID> {
  List<OperationalParameter> findByEmpresaIdOrderByClaveAsc(UUID empresaId);
  Optional<OperationalParameter> findByIdAndEmpresaId(UUID id, UUID empresaId);
}
