package com.taller360.clients.repository;

import com.taller360.clients.entity.ClientDocument;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientDocumentRepository extends JpaRepository<ClientDocument, UUID> {
  List<ClientDocument> findByCliente_IdAndEmpresa_IdOrderByFechaCreacionDesc(UUID clienteId, UUID empresaId);
  Optional<ClientDocument> findByIdAndEmpresa_Id(UUID id, UUID empresaId);
}
