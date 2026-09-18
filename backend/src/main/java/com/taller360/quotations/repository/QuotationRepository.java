package com.taller360.quotations.repository;

import com.taller360.quotations.entity.Quotation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface QuotationRepository extends JpaRepository<Quotation, UUID> {
  Optional<Quotation> findByIdAndEmpresa_Id(UUID id, UUID empresaId);
  Optional<Quotation> findByPublicToken(String publicToken);

  @Query("""
      select q from Quotation q
      where q.empresa.id = :empresaId
        and (:sucursalId is null or q.sucursal.id = :sucursalId)
        and (:text = '' or lower(q.cliente.nombre) like lower(concat('%', :text, '%'))
          or lower(q.vehiculo.placa) like lower(concat('%', :text, '%'))
          or lower(q.estado) like lower(concat('%', :text, '%')))
      order by q.fechaCreacion desc
      """)
  List<Quotation> search(@Param("empresaId") UUID empresaId, @Param("sucursalId") UUID sucursalId,
      @Param("text") String text);
}
