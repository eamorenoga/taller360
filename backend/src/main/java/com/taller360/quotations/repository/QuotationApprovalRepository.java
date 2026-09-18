package com.taller360.quotations.repository;

import com.taller360.quotations.entity.QuotationApproval;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationApprovalRepository extends JpaRepository<QuotationApproval, UUID> {
  List<QuotationApproval> findByCotizacion_IdAndEmpresa_IdOrderByFechaCreacionDesc(UUID quotationId, UUID empresaId);
}
