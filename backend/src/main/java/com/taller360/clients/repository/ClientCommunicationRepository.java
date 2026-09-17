package com.taller360.clients.repository;

import com.taller360.clients.entity.ClientCommunication;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientCommunicationRepository extends JpaRepository<ClientCommunication, UUID> {
  List<ClientCommunication> findByCliente_IdAndEmpresa_IdOrderByFechaHoraDesc(UUID clienteId, UUID empresaId);
  Optional<ClientCommunication> findByIdAndEmpresa_Id(UUID id, UUID empresaId);
}
