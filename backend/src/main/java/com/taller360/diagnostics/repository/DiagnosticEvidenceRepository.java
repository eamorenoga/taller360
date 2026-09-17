package com.taller360.diagnostics.repository;

import com.taller360.diagnostics.entity.DiagnosticEvidence;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosticEvidenceRepository extends JpaRepository<DiagnosticEvidence, UUID> {
  List<DiagnosticEvidence> findByDiagnostico_IdAndEmpresa_IdOrderByFechaCreacionDesc(UUID diagnosticoId, UUID empresaId);
}
