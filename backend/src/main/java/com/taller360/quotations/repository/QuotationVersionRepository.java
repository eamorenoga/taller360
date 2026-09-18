package com.taller360.quotations.repository;

import com.taller360.quotations.entity.QuotationVersion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationVersionRepository extends JpaRepository<QuotationVersion, UUID> {
  List<QuotationVersion> findByCotizacion_IdAndEmpresa_IdOrderByVersionDesc(UUID quotationId, UUID empresaId);
}
