package com.taller360.clients.web;

import com.taller360.clients.dto.ClientDtos.*;
import com.taller360.clients.service.ClientService;
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
@RequestMapping("/api/v1/clients")
public class ClientController {
  private final ClientService clients;

  public ClientController(ClientService clients) {
    this.clients = clients;
  }

  @GetMapping
  @PreAuthorize("@perm.has(authentication, 'CLIENTES:VER')")
  List<ClientResponse> search(@RequestParam(required = false) String q, @RequestParam(required = false) String plate) {
    return clients.search(q, plate);
  }

  @GetMapping("/{id}")
  @PreAuthorize("@perm.has(authentication, 'CLIENTES:VER')")
  Client360Response detail(@PathVariable UUID id) {
    return clients.detail(id);
  }

  @PostMapping
  @PreAuthorize("@perm.has(authentication, 'CLIENTES:CREAR')")
  ClientResponse create(@RequestBody ClientRequest request) {
    return clients.create(request);
  }

  @PutMapping("/{id}")
  @PreAuthorize("@perm.has(authentication, 'CLIENTES:EDITAR')")
  ClientResponse update(@PathVariable UUID id, @RequestBody ClientRequest request) {
    return clients.update(id, request);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("@perm.has(authentication, 'CLIENTES:ELIMINAR')")
  ResponseEntity<Void> deactivate(@PathVariable UUID id) {
    clients.deactivate(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/contacts")
  @PreAuthorize("@perm.has(authentication, 'CLIENTES:EDITAR')")
  ContactResponse addContact(@PathVariable UUID id, @RequestBody ContactRequest request) {
    return clients.addContact(id, request);
  }

  @PostMapping("/{id}/documents")
  @PreAuthorize("@perm.has(authentication, 'CLIENTES:EDITAR')")
  DocumentResponse addDocument(@PathVariable UUID id, @RequestBody DocumentRequest request) {
    return clients.addDocument(id, request);
  }

  @PostMapping("/{id}/communications")
  @PreAuthorize("@perm.has(authentication, 'CLIENTES:EDITAR')")
  CommunicationResponse addCommunication(@PathVariable UUID id, @RequestBody CommunicationRequest request) {
    return clients.addCommunication(id, request);
  }
}
