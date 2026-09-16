package com.taller360.iam.repository;

import com.taller360.iam.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {
  @EntityGraph(attributePaths = {"roles", "roles.permisos", "sucursales", "empresa"})
  Optional<User> findFirstByEmailIgnoreCase(String email);

  @EntityGraph(attributePaths = {"roles", "roles.permisos", "sucursales", "empresa"})
  Optional<User> findByIdAndEmpresaId(UUID id, UUID empresaId);

  List<User> findByEmpresaId(UUID empresaId);
}
