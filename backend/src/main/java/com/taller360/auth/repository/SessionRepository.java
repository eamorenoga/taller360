package com.taller360.auth.repository;

import com.taller360.auth.entity.Session;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<Session, UUID> {
  List<Session> findByUsuarioIdAndActivaTrue(UUID usuarioId);
  Optional<Session> findByIdAndUsuarioId(UUID id, UUID usuarioId);
}
