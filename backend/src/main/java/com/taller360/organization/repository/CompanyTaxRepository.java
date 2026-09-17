package com.taller360.organization.repository;

import com.taller360.organization.entity.CompanyTax;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyTaxRepository extends JpaRepository<CompanyTax, UUID> {
  List<CompanyTax> findByEmpresaIdOrderByCodigoAsc(UUID empresaId);
  Optional<CompanyTax> findByIdAndEmpresaId(UUID id, UUID empresaId);
}
