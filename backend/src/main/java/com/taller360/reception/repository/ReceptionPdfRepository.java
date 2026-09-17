package com.taller360.reception.repository;

import com.taller360.reception.entity.ReceptionPdf;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceptionPdfRepository extends JpaRepository<ReceptionPdf, UUID> {
  Optional<ReceptionPdf> findFirstByRecepcion_IdAndEmpresa_IdOrderByVersionDesc(UUID recepcionId, UUID empresaId);
}
