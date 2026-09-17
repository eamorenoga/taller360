package com.taller360.vehicles.repository;

import com.taller360.vehicles.entity.Vehicle;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {
  Optional<Vehicle> findByIdAndEmpresa_Id(UUID id, UUID empresaId);

  List<Vehicle> findByCliente_IdAndEmpresa_IdOrderByFechaModificacionDesc(UUID clienteId, UUID empresaId);

  @Query("""
      select v from Vehicle v
      where v.empresa.id = :empresaId
        and (:q is null or :q = ''
          or lower(v.placa) like lower(concat('%', :q, '%'))
          or lower(coalesce(v.vin, '')) like lower(concat('%', :q, '%'))
          or lower(v.marca) like lower(concat('%', :q, '%'))
          or lower(v.modelo) like lower(concat('%', :q, '%'))
          or lower(v.cliente.nombre) like lower(concat('%', :q, '%'))
          or lower(coalesce(v.cliente.identificacion, '')) like lower(concat('%', :q, '%')))
      order by v.fechaModificacion desc
      """)
  List<Vehicle> search(@Param("empresaId") UUID empresaId, @Param("q") String q);

  @Query("""
      select v.cliente.id from Vehicle v
      where v.empresa.id = :empresaId and lower(v.placa) = lower(:plate)
      """)
  List<UUID> findClientIdsByPlate(@Param("empresaId") UUID empresaId, @Param("plate") String plate);
}
