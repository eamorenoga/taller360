package com.taller360.reception.repository;

import com.taller360.reception.entity.ReceptionSignature;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceptionSignatureRepository extends JpaRepository<ReceptionSignature, UUID> {
  List<ReceptionSignature> findByRecepcion_IdAndEmpresa_IdOrderByVersionDesc(UUID recepcionId, UUID empresaId);
}
