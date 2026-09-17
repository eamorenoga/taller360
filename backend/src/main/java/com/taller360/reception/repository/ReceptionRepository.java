package com.taller360.reception.repository;

import com.taller360.reception.entity.Reception;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReceptionRepository extends JpaRepository<Reception, UUID> {
  Optional<Reception> findByIdAndEmpresa_Id(UUID id, UUID empresaId);
  Optional<Reception> findByCita_IdAndEmpresa_Id(UUID citaId, UUID empresaId);

  @Query("""
      select r from Reception r
      where r.empresa.id = :empresaId
        and (:sucursalId is null or r.sucursal.id = :sucursalId)
        and (:q = '' or lower(r.cliente.nombre) like lower(concat('%', :q, '%'))
          or lower(r.vehiculo.placa) like lower(concat('%', :q, '%'))
          or lower(r.motivo) like lower(concat('%', :q, '%')))
      order by r.fechaCreacion desc
      """)
  List<Reception> search(@Param("empresaId") UUID empresaId, @Param("sucursalId") UUID sucursalId,
      @Param("q") String q);
}
