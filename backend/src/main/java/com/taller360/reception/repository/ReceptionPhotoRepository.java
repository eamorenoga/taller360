package com.taller360.reception.repository;

import com.taller360.reception.entity.ReceptionPhoto;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceptionPhotoRepository extends JpaRepository<ReceptionPhoto, UUID> {
  List<ReceptionPhoto> findByRecepcion_IdAndEmpresa_IdOrderByFechaCreacionDesc(UUID recepcionId, UUID empresaId);
}
