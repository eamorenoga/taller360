package com.taller360.clients.repository;

import com.taller360.clients.entity.ClientContact;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientContactRepository extends JpaRepository<ClientContact, UUID> {
  List<ClientContact> findByCliente_IdAndEmpresa_IdOrderByPrincipalDescNombreAsc(UUID clienteId, UUID empresaId);
  Optional<ClientContact> findByIdAndEmpresa_Id(UUID id, UUID empresaId);
}
