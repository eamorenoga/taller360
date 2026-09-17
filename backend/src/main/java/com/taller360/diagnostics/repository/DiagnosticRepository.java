package com.taller360.diagnostics.repository;

import com.taller360.diagnostics.entity.Diagnostic;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DiagnosticRepository extends JpaRepository<Diagnostic, UUID> {
  Optional<Diagnostic> findByIdAndEmpresa_Id(UUID id, UUID empresaId);

  @Query("""
      select d from Diagnostic d
      where d.empresa.id = :empresaId
        and (:sucursalId is null or d.sucursal.id = :sucursalId)
        and (:q = '' or lower(d.vehiculo.placa) like lower(concat('%', :q, '%'))
          or lower(d.cliente.nombre) like lower(concat('%', :q, '%'))
          or lower(d.causaProbable) like lower(concat('%', :q, '%')))
      order by d.fechaCreacion desc
      """)
  List<Diagnostic> search(@Param("empresaId") UUID empresaId, @Param("sucursalId") UUID sucursalId,
      @Param("q") String q);
}
