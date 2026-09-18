package com.taller360.quotations.repository;

import com.taller360.quotations.entity.QuotationPdf;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationPdfRepository extends JpaRepository<QuotationPdf, UUID> {
  Optional<QuotationPdf> findFirstByCotizacion_IdAndEmpresa_IdOrderByVersionDesc(UUID quotationId, UUID empresaId);
}
