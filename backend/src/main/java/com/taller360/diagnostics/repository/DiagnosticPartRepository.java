package com.taller360.diagnostics.repository;

import com.taller360.diagnostics.entity.DiagnosticPart;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosticPartRepository extends JpaRepository<DiagnosticPart, UUID> {
  List<DiagnosticPart> findByDiagnostico_IdAndEmpresa_IdOrderByFechaCreacionDesc(UUID diagnosticoId, UUID empresaId);
}
