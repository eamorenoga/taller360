package com.taller360.vehicles.web;

import com.taller360.vehicles.dto.VehicleDtos.*;
import com.taller360.vehicles.service.VehicleService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/vehicles")
public class VehicleController {
  private final VehicleService vehicles;

  public VehicleController(VehicleService vehicles) {
    this.vehicles = vehicles;
  }

  @GetMapping
  @PreAuthorize("@perm.has(authentication, 'VEHICULOS:VER')")
  List<VehicleResponse> search(@RequestParam(required = false) String q) {
    return vehicles.search(q);
  }

  @GetMapping("/{id}")
  @PreAuthorize("@perm.has(authentication, 'VEHICULOS:VER')")
  Vehicle360Response detail(@PathVariable UUID id) {
    return vehicles.detail(id);
  }

  @PostMapping
  @PreAuthorize("@perm.has(authentication, 'VEHICULOS:CREAR')")
  VehicleResponse create(@RequestBody VehicleRequest request) {
    return vehicles.create(request);
  }

  @PutMapping("/{id}")
  @PreAuthorize("@perm.has(authentication, 'VEHICULOS:EDITAR')")
  VehicleResponse update(@PathVariable UUID id, @RequestBody VehicleRequest request) {
    return vehicles.update(id, request);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("@perm.has(authentication, 'VEHICULOS:ELIMINAR')")
  ResponseEntity<Void> deactivate(@PathVariable UUID id) {
    vehicles.deactivate(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/documents")
  @PreAuthorize("@perm.has(authentication, 'VEHICULOS:EDITAR')")
  VehicleDocumentResponse addDocument(@PathVariable UUID id, @RequestBody VehicleDocumentRequest request) {
    return vehicles.addDocument(id, request);
  }

  @PostMapping("/{id}/photos")
  @PreAuthorize("@perm.has(authentication, 'VEHICULOS:EDITAR')")
  VehiclePhotoResponse addPhoto(@PathVariable UUID id, @RequestBody VehiclePhotoRequest request) {
    return vehicles.addPhoto(id, request);
  }

  @PostMapping("/{id}/warranties")
  @PreAuthorize("@perm.has(authentication, 'VEHICULOS:EDITAR')")
  VehicleWarrantyResponse addWarranty(@PathVariable UUID id, @RequestBody VehicleWarrantyRequest request) {
    return vehicles.addWarranty(id, request);
  }
}
