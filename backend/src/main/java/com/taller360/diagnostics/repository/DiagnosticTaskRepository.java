package com.taller360.diagnostics.repository;

import com.taller360.diagnostics.entity.DiagnosticTask;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosticTaskRepository extends JpaRepository<DiagnosticTask, UUID> {
  List<DiagnosticTask> findByDiagnostico_IdAndEmpresa_IdOrderByFechaCreacionDesc(UUID diagnosticoId, UUID empresaId);
}
