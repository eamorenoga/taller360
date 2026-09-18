package com.taller360.quotations.repository;

import com.taller360.quotations.entity.QuotationItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuotationItemRepository extends JpaRepository<QuotationItem, UUID> {
  List<QuotationItem> findByCotizacion_IdAndEmpresa_IdOrderByFechaCreacionAsc(UUID quotationId, UUID empresaId);
  Optional<QuotationItem> findByIdAndCotizacion_IdAndEmpresa_Id(UUID id, UUID quotationId, UUID empresaId);
  void deleteByCotizacion_IdAndEmpresa_Id(UUID quotationId, UUID empresaId);
}
