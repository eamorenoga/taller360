package com.taller360.clients.repository;

import com.taller360.clients.entity.Client;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClientRepository extends JpaRepository<Client, UUID> {
  Optional<Client> findByIdAndEmpresa_Id(UUID id, UUID empresaId);

  @Query("""
      select c from Client c
      where c.empresa.id = :empresaId
        and (:q is null or :q = ''
          or lower(c.nombre) like lower(concat('%', :q, '%'))
          or lower(coalesce(c.razonSocial, '')) like lower(concat('%', :q, '%'))
          or lower(coalesce(c.identificacion, '')) like lower(concat('%', :q, '%'))
          or lower(coalesce(c.telefonoPrincipal, '')) like lower(concat('%', :q, '%'))
          or lower(coalesce(c.telefonoSecundario, '')) like lower(concat('%', :q, '%'))
          or lower(coalesce(c.whatsapp, '')) like lower(concat('%', :q, '%'))
          or lower(coalesce(c.email, '')) like lower(concat('%', :q, '%')))
      order by c.fechaModificacion desc
      """)
  List<Client> search(@Param("empresaId") UUID empresaId, @Param("q") String q);
}
